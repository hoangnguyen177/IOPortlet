package edu.uq.workways.ioporlet;
//java
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import javax.portlet.PortletPreferences;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;



//icepush
import org.vaadin.artur.icepush.ICEPush;


//vaadin
import com.vaadin.addon.ipcforliferay.LiferayIPC;
import com.vaadin.addon.ipcforliferay.event.LiferayIPCEvent;
import com.vaadin.addon.ipcforliferay.event.LiferayIPCEventListener;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
//gson
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;



//socket io
import edu.monash.io.socketio.connection.SinkConnection;
import edu.monash.io.socketio.connection.SinkListener;
import edu.monash.io.socketio.connection.ConnectionConsts;
import edu.monash.io.socketio.exceptions.ConnectionFailException;
import edu.monash.io.socketio.exceptions.InvalidMessageException;
import edu.monash.io.socketio.exceptions.InvalidSourceException;
import edu.monash.io.socketio.exceptions.UnauthcatedClientException;
/**
 * class IoportletUI
 * @author hoangnguyen
 */
@PreserveOnRefresh
@Widgetset("edu.uq.workways.ioporlet.widgetset.IoporletWidgetset")
public class IoporletUI extends UI{
	@WebServlet(value = "/*", asyncSupported = true, initParams = {
			@WebInitParam(name = "ui", value = "edu.uq.workways.ioporlet.IoporletUI"),
			@WebInitParam(name = "productionMode", value = "false") })
	public static class Servlet extends VaadinServlet {
	}
	
	
	
	/********************************************************/	
	private LiferayIPC 					liferayipc 					= new LiferayIPC();
	private String						liferayScreenName			= "";
	private SinkConnection 				sink						= null;
	
	private VerticalLayout 				layout						= null;
	private ThemeDisplay 				themeDisplay				= null;
	private Label						statusLabel					= null;
	private ICEPush 					pusher 						= null;
	//parameters 
	private String 						host 						= "localhost";
	private String 						protocol					= "http";
	private int							timeout						= 10;
	private int							port						= 9090;	
	private String						nsp							= "/";
	private String						sourceId					= "";
	
	private Map<String, Outputable>		outputs						= new HashMap<String, Outputable>();
	
	/********************************************************/
	@Override
	public void init(VaadinRequest request) {
		layout= new VerticalLayout();
		this.setContent(layout);
		statusLabel = new Label("Status..........");
		layout.addComponent(statusLabel);
		
		pusher = new ICEPush();
		this.addExtension(pusher);

		
		themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
		if(themeDisplay==null || !themeDisplay.isSignedIn()){
			statusLabel.setValue("ThemeDisplay is null or not signed in");
			return;
		}
		else{
			liferayScreenName = themeDisplay.getUser().getScreenName();
		}
		
		long companyId = themeDisplay.getCompanyId();
		long ownerId = PortletKeys.PREFS_OWNER_ID_DEFAULT;
		int ownerType = PortletKeys.PREFS_OWNER_TYPE_LAYOUT;
		PortletDisplay portletDisplay= themeDisplay.getPortletDisplay();
		String portletId= portletDisplay.getId();
		PortletPreferences prefs;
		try {
			prefs = PortletPreferencesLocalServiceUtil.getPreferences(companyId, ownerId, ownerType, themeDisplay.getLayout().getPlid(), portletId);
			host = prefs.getValue("host", "localhost");
			protocol = prefs.getValue("protocol", "http");
			timeout = Integer.parseInt(prefs.getValue("timeout", "10"));
			port = Integer.parseInt(prefs.getValue("port", "9090"));
			nsp = prefs.getValue("nsp", "/");
			sourceId = prefs.getValue("sourceid", "");
			statusLabel.setValue("host:" + host + "::protocol:" + protocol + "::timeout:" + timeout + "::port:" + port + "::source:" + sourceId);
		} catch (SystemException e1) {
			e1.printStackTrace();
		}
		
//		liferayScreenName = "test";
//		host = "localhost";
//		protocol = "http";
//		timeout = 10;
//		port = 9090;
//		nsp = "/";
//		sourceId = "SLU1-E5bKKE8VYP8AAAF";
		
		this.setupIpc();
		if(sink==null)
			this.createConnection();
	}
	

	
	/**
	 * set up ipc with other portlet
	 */
	private void setupIpc(){
		liferayipc.extend(getUI());
		liferayipc.addLiferayIPCEventListener("Workflow", new LiferayIPCEventListener() {
		    public void eventReceived(LiferayIPCEvent event) {
		        String data = event.getData();
		        if(data.equals("terminated")){
		        	statusLabel.setValue("Terminate signal from Workflow portlet!");
		        	if(sink!=null && sink.isConnected())
						try {
							sink.disconnect();
							sink = null;
						} catch (ConnectionFailException e) {
							e.printStackTrace();
						}
		        }
		    }
		});
	}
	
	/**
	 * create the connection
	 */
	private void createConnection(){
		statusLabel.setValue("Creating connection");
		sink = new SinkConnection();
		sink.setHost(host);
		sink.setPort(port);
		sink.setProtocol(protocol);
		sink.setTimeout(timeout);
		sink.setNsp(nsp);
		JsonObject authInfo = new JsonObject();
		authInfo.addProperty(ConnectionConsts.CONNECTION_C_USERNAME, liferayScreenName);
		authInfo.addProperty(ConnectionConsts.CONNECTION_C_PASSWORD , ""); //no pass atm
		authInfo.addProperty("appname" , "IOPortlet");
		sink.setAuthInfo(authInfo);
		sink.addListener(new SinkListener(){
			public void onConnectionEstablished(){
			}
			public void onDisconnect(){
			}
			public void onAuthResponse(JsonObject authResponse){
				boolean authResult = Boolean.parseBoolean(authResponse.get("authresult").getAsString());
				JsonObject sourcesList = authResponse.getAsJsonObject("sourcelist");
				if(sourcesList.has(sourceId)){
					try {
						statusLabel.setValue("Select source:" + sourceId);
						sink.selectSource(sourceId);
					} catch (UnauthcatedClientException e) {
						statusLabel.setValue("Error@selecting source:" + e.getMessage()+ ".Exit!");
						return;
					} catch (InvalidSourceException e) {
						statusLabel.setValue("Error@selecting source:" + e.getMessage()+ ".Exit!");
						return;
					}
					//selecting source successfully, now creating items accordingly
					JsonObject sourceDefinition = sourcesList.getAsJsonObject(sourceId);
					parseSourceDefinition(sourceDefinition);
				}
				else{
					statusLabel.setValue("source:" + sourceId + " is not in sourcelist:" + sourcesList + ". Exit!");
					return;
				}
				
			}
			public void onMessage(JsonObject aMessage) throws InvalidMessageException{
				String path = aMessage.get("path").getAsString();
				String data = aMessage.get("data").getAsString();
				if(!outputs.containsKey(path)){
					return;
				}
				try {
					outputs.get(path).addData(data, path, true);
				} catch (UpperLimitNumberOfSeriesException e) {
					e.printStackTrace();
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
				pusher.push();
			}	
			public void onSourceDisconnect(){
				if(sink!=null && sink.isConnected()){
					try {
						sink.disconnect();
						sink = null;
					} catch (ConnectionFailException e) {e.printStackTrace();}
					statusLabel.setValue("The source is disconnected!");
				}
			}
			public void onSourceConnect(JsonObject sourceList){
			}
			public void onPermissionChanged(String newPermission){
			}
			public void onLinkEstablished(String allowedOperations){
			}
		});
		try {
			sink.connect();
		} catch (ConnectionFailException e) {
			statusLabel.setValue(e.getMessage());
		}
	}
	
	
	
	/**
	 * source definition
	 * @param definition
	 */
	private void parseSourceDefinition(JsonObject definition){
		Set<Map.Entry<String,JsonElement>> entries = definition.entrySet();
		Iterator<Map.Entry<String, JsonElement>> iterator = entries.iterator();
		while(iterator.hasNext()){
			Map.Entry<String, JsonElement> _entry = iterator.next();
			String _key = _entry.getKey();
			if(!_entry.getValue().isJsonObject())
				continue;
			JsonObject _value = _entry.getValue().getAsJsonObject();
			if(_value.has("type") && _value.get("type").getAsString().equals("channel")){
				//now get the input/output/inout type within this channel
				JsonObject _channel = _value;
				String containerId = _channel.get("containerid").getAsString();
				String _path = containerId + "." + _key;
				Set<Map.Entry<String,JsonElement>> _channelItems = _channel.entrySet();
				Iterator<Map.Entry<String, JsonElement>> _channelItemsIterator = _channelItems.iterator();
				while(_channelItemsIterator.hasNext()){
					Map.Entry<String, JsonElement> _channelItem = _channelItemsIterator.next();
					String _channelItemKey = _channelItem.getKey();
					JsonElement _channelItemValue = _channelItem.getValue();
					if(!_channelItemValue.isJsonObject())
						continue;
					JsonObject _channelItemAsObject = _channelItemValue.getAsJsonObject();
					if(!_channelItemAsObject.has("type"))
						continue;
					String _channelItemType = _channelItemAsObject.get("type").getAsString();
					//not interested in other cases
					if(!_channelItemType.equals("INPUT") && !_channelItemType.equals("OUTPUT")) //INOUT is not used anymore
						continue;
					//OUTPUT and INPUT both have these
					String _outputPath = _path + "." + _channelItemKey;
					String out_datatype = _channelItemAsObject.get("out_datatype").getAsString();
					String out_data     = _channelItemAsObject.get("out_data").getAsString();
					String gui_element	= _channelItemAsObject.get("guielement").getAsString();
					String update_mode	= _channelItemAsObject.get("update_mode").getAsString();
					String gui_id = "";
					if(_channelItemAsObject.has("gui_id"))
						gui_id = _channelItemAsObject.get("gui_id").getAsString();
					String caption = "";
					if(_channelItemAsObject.has("caption"))
						caption = _channelItemAsObject.get("caption").getAsString();
					if(_channelItemType.equals("OUTPUT")){
						boolean _createNewGui = true;
						Collection<Outputable> outputables = outputs.values();
						Iterator<Outputable> outputableIterator = outputables.iterator();
						while(outputableIterator.hasNext()){
							Outputable _anOutput = outputableIterator.next();
							//do not need to  create another gui element
							if(_anOutput.isEqual(gui_id, gui_element, out_datatype, update_mode)){
								outputs.put(_outputPath, _anOutput);
								_createNewGui = false;
								break;
							}
						}				
						if(_createNewGui){
							Outputable _output = null;
							if(gui_element.equals("graph.line")){
								_output = new LineGraph();
								_output.setId(gui_id);
							}
							else if(gui_element.equals("parallel.coordinates")){
								if(gui_id.equals(""))
									gui_id = gui_element;
								_output = new ParallelCoordinate(gui_id);
								statusLabel.setValue("Adding parallel coordinates id:" + gui_id);
							}
							else
							{
								this.setHeight("700px");
								statusLabel.setValue("gui_element not supported:" + gui_element);
								return;
							}
							_output.setCaption(caption);
							_output.setGuiType(gui_element);
							_output.setOutputDataType(out_datatype);
							_output.setUpdateMode(update_mode);
							outputs.put(_outputPath, _output);
							_output.addToLayout(layout);
							pusher.push();
						}
					}
					
				}
				
			}
		}
		
	}


}
