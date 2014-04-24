package edu.uq.workways.ioportlet;

//java
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.portlet.PortletPreferences;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;



//refresher
import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;

//vaadin
import com.vaadin.addon.ipcforliferay.LiferayIPC;
import com.vaadin.addon.ipcforliferay.event.LiferayIPCEvent;
import com.vaadin.addon.ipcforliferay.event.LiferayIPCEventListener;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification.Type;
//gson
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;

import edu.monash.io.iolibrary.ConfigurationConsts.DataType;
//socket io
import edu.monash.io.socketio.connection.SinkConnection;
import edu.monash.io.socketio.connection.SinkListener;
import edu.monash.io.socketio.connection.ConnectionConsts;
import edu.monash.io.socketio.exceptions.ConnectionFailException;
import edu.monash.io.socketio.exceptions.InvalidMessageException;
import edu.monash.io.socketio.exceptions.InvalidSourceException;
import edu.monash.io.socketio.exceptions.UnauthcatedClientException;
import edu.uq.workways.commons.utils.PropsUtil;

@PreserveOnRefresh
@Widgetset("edu.uq.workways.ioportlet.widgetset.IoportletWidgetset")
public class IoportletUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true, initParams = {
            @WebInitParam(name = "ui", value = "edu.uq.workways.ioportlet.IoportletUI"),
            @WebInitParam(name = "productionMode", value = "false") })
	public static class Servlet extends VaadinServlet {
	}
	/********************************************************/	
	private VerticalLayout 				layout 						= null;
	
	private LiferayIPC 					liferayipc 					= null;
	private String						liferayScreenName			= "";
	private SinkConnection 				sink						= null;
	
	private ThemeDisplay 				themeDisplay				= null;
	private Label						statusLabel					= null;
	
	private PropsUtil					propsUtil 					= new PropsUtil("resource/ioportlet.properties");
	private boolean 					isDebugging					= Boolean.parseBoolean(propsUtil.get("debug"));
	private boolean 					isLocalTesting				= Boolean.parseBoolean(propsUtil.get("localtesting"));

	
	//parameters 
	private String 						host 						= "localhost";
	private String 						protocol					= "http";
	private int							timeout						= 10;
	private int							port						= 9090;	
	private String						nsp							= "/";
	private String						sourceId					= "";
	
	private Map<String, Displayable>		outputs					= new HashMap<String, Displayable>();
	//groups of inputtables
	private Map<String, DisplayableGroup>	displayablesGroup		= new HashMap<String, DisplayableGroup>();	
	//message queue
	private Queue<JsonObject> 				messageQueue 			= new ConcurrentLinkedQueue<JsonObject>();
			
	private boolean							portletGuiInitialised			= false;
	private JsonObject						sourceDefinition		= null;
	/********************************************************/
	
	@Override
	protected void init(VaadinRequest request) {
		layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);

		statusLabel = new Label("Status..........");
		layout.addComponent(statusLabel);
		
		if(!isLocalTesting){
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
			} catch (Exception e) {
				Notification.show("Error at initialising IOPortlet: " + e.getMessage(), Type.ERROR_MESSAGE);
				return;
			}
			this.setupIpc();
		}
		else{
			liferayScreenName = "test";
			host = "localhost";
			protocol = "http";
			timeout = 10;
			port = 9090;
			nsp = "/";
			sourceId = "ekk_qf0uA5VzKnyLAAAA";			
		}
		
		Refresher refresher = new Refresher();
	    refresher.setRefreshInterval(500);
        addExtension(refresher);
        
		if(sink==null)
			this.createConnection();
	}
	
	
	
	/**
	 * set up ipc with other portlet
	 */
	private void setupIpc(){
		liferayipc = new LiferayIPC();
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
				try {
					sink.disconnect();
				} catch (ConnectionFailException e) {
					e.printStackTrace();
				}
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
					sourceDefinition = sourcesList.getAsJsonObject(sourceId);
					parseSourceDefinition(sourceDefinition);
					portletGuiInitialised = true;
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
					layout.markAsDirtyRecursive();
				} catch (UpperLimitNumberOfSeriesException e) {
					e.printStackTrace();
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
//				messageQueue.offer(aMessage);
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
					final String _outputPath = _path + "." + _channelItemKey;
					String gui_element	= _channelItemAsObject.get("guielement").getAsString();
					String update_mode	= _channelItemAsObject.get("update_mode").getAsString();
					String gui_id = "";
					if(_channelItemAsObject.has("gui_id"))
						gui_id = _channelItemAsObject.get("gui_id").getAsString();
					String caption = "";
					if(_channelItemAsObject.has("caption"))
						caption = _channelItemAsObject.get("caption").getAsString();
					//grouping
					boolean belongToGroup = false; 
					String groupName = "";
					if(_channelItemAsObject.has("group")){
						groupName = _channelItemAsObject.get("group").getAsString().trim();
						if(!groupName.isEmpty())
							belongToGroup = true;						
					}
					//if(_channelItemType.equals("OUTPUT")){
					
						boolean _createNewGui = true;
						Collection<Displayable> outputables = outputs.values();
						Iterator<Displayable> outputableIterator = outputables.iterator();
						while(outputableIterator.hasNext()){
							Displayable _anOutput = outputableIterator.next();
							//do not need to  create another gui element
							if(_anOutput.isEqual(gui_id, gui_element, update_mode)){
								outputs.put(_outputPath, _anOutput);
								_createNewGui = false;
								break;
							}
						}				
						if(_createNewGui){
							if(gui_id.equals(""))
								gui_id = gui_element;
							Displayable _output = null;
							//////////graph line
							if(gui_element.equals("graph.line")){
								if(_channelItemType.equals("INPUT")){
									statusLabel.setValue("INPUT graph.line not supported yet:" + gui_element);
									return;
								}
								else {
									_output = new LineGraph();
									_output.setId(gui_id);
								}
							}
							/////////////graph bar
							else if(gui_element.equals("graph.bar")){
								statusLabel.setValue("graph.bar not supported:" + gui_element);
								return;
							}							
							////////////graph pie
							else if(gui_element.equals("graph.pie")){
								statusLabel.setValue("graph.pie not supported:" + gui_element);
								return;
							}
							//////////parallel coordinates
							else if(gui_element.equals("parallel.coordinates")){
								if(_channelItemType.equals("INPUT")){
									//inputaction: later on. Now only points selection
//									String _inputaction = "";
//									if(_channelItemAsObject.has("inputaction"))
//										_inputaction = _channelItemAsObject.get("inputaction").getAsString();
									_output = new ParallelCoordinate_Inputable(gui_id);
									((ParallelCoordinate_Inputable)_output).addInputListener(new InputListener(){
										@Override
										public void onUserInput(JsonElement userInput){
											//this userInput is an JsonArray
											String _input = userInput.toString();
											JsonObject _returnMessage = new JsonObject();
											_returnMessage.addProperty("messagetype", ConnectionConsts.CLIENT_C_MESSAGE);
											_returnMessage.addProperty("path" , _outputPath);	
											_returnMessage.addProperty("datatype" , DataType.JSON_STRING.toString());
											_returnMessage.addProperty("data" , _input);
											try {
												sink.send(_returnMessage);
											} 
											catch (ConnectionFailException e) {} 
											catch (UnauthcatedClientException e) {}	
											
										}
										
									});
									
								}
								else if(_channelItemType.equals("OUTPUT")){
									_output = new ParallelCoordinate(gui_id);
								}
								statusLabel.setValue("Adding parallel coordinates id:" + gui_id);
							}
							
							//////////text area
							else if(gui_element.equals("gui.textarea")){
								if(_channelItemType.equals("INPUT")){
									_output = new TextArea_Inputable(gui_id);
									((TextArea_Inputable)_output).addInputListener(new InputListener(){
										@Override
										public void onUserInput(JsonElement userInput){
											String _input = userInput.getAsString();
											JsonObject _returnMessage = new JsonObject();
											_returnMessage.addProperty("messagetype", ConnectionConsts.CLIENT_C_MESSAGE);
											_returnMessage.addProperty("path" , _outputPath);	
											_returnMessage.addProperty("datatype" , DataType.STRING.toString());
											_returnMessage.addProperty("data" , _input);
											try {
												sink.send(_returnMessage);
											} 
											catch (ConnectionFailException e) {} 
											catch (UnauthcatedClientException e) {}	
										}										
									});									
								}
								else if(_channelItemType.equals("OUTPUT")){
									_output = new TextArea(gui_id);
								}
								statusLabel.setValue("Adding textarea:" + gui_id);
							}
							//////////text field
							else if(gui_element.equals("gui.textfield")){
								if(_channelItemType.equals("INPUT")){
									_output = new TextField_Inputable(gui_id);
									((TextField_Inputable)_output).addInputListener(new InputListener(){
										@Override
										public void onUserInput(JsonElement userInput){
											String _input = userInput.getAsString();
											JsonObject _returnMessage = new JsonObject();
											_returnMessage.addProperty("messagetype", ConnectionConsts.CLIENT_C_MESSAGE);
											_returnMessage.addProperty("path" , _outputPath);	
											_returnMessage.addProperty("datatype" , DataType.STRING.toString());
											_returnMessage.addProperty("data" , _input);
											try {
												sink.send(_returnMessage);
											} 
											catch (ConnectionFailException e) {} 
											catch (UnauthcatedClientException e) {}	
										}										
									});									
								}
								else if(_channelItemType.equals("OUTPUT")){
									_output = new TextField(gui_id);
								}
								statusLabel.setValue("Adding textfield:" + gui_id);
							}
							///// boolean input
							else if(gui_element.equals("gui.booleaninput")){
								if(_channelItemType.equals("INPUT")){
									_output = new BooleanInput_Inputable(gui_id);
									((BooleanInput_Inputable)_output).addInputListener(new InputListener(){
										@Override
										public void onUserInput(JsonElement userInput){
											String _input = userInput.getAsString();
											JsonObject _returnMessage = new JsonObject();
											_returnMessage.addProperty("messagetype", ConnectionConsts.CLIENT_C_MESSAGE);
											_returnMessage.addProperty("path" , _outputPath);	
											_returnMessage.addProperty("datatype" , DataType.BOOL.toString());
											_returnMessage.addProperty("data" , _input);
											try {
												sink.send(_returnMessage);
											} 
											catch (ConnectionFailException e) {} 
											catch (UnauthcatedClientException e) {}	
										}										
									});				
								}
								else if(_channelItemType.equals("OUTPUT")){
									statusLabel.setValue("gui.booleaninput does not have OUTPUT option");
									return;
								}
								statusLabel.setValue("Adding boolean input:" + gui_id);
							}							
							
							///// image
							else if(gui_element.equals("gui.image")){
								if(_channelItemType.equals("INPUT")){
									statusLabel.setValue("gui.image does not have INPUT option");
									return;
								}
								else if(_channelItemType.equals("OUTPUT")){
									_output = new Image(gui_id);
									statusLabel.setValue("Adding image output:" + gui_id);
								}
								
							}
							
							////// image slide
							else if(gui_element.equals("gui.imageslide")){
								if(_channelItemType.equals("INPUT")){
									statusLabel.setValue("gui.image does not have INPUT option yet");
									return;
								}
								else if(_channelItemType.equals("OUTPUT")){
									_output = new ImageSlide(gui_id);
									statusLabel.setValue("Adding imageslide:" + gui_id);
								}
								
							}
							
							///// selection
							else if(gui_element.equals("gui.selection")){
								if(_channelItemType.equals("OUTPUT")){
									_output = new Options(gui_id);
								}
								else if(_channelItemType.equals("INPUT")){
									_output = new Options_Inputable(gui_id);
									((Options_Inputable)_output).addInputListener(new InputListener(){
										@Override
										public void onUserInput(JsonElement userInput){
											JsonObject _returnMessage = new JsonObject();
											_returnMessage.addProperty("messagetype", ConnectionConsts.CLIENT_C_MESSAGE);
											_returnMessage.addProperty("path" , _outputPath);	
											_returnMessage.addProperty("datatype" , DataType.STRING.toString());// JSON_STRING as STRING
											_returnMessage.addProperty("data" , userInput.toString());
											try {
												sink.send(_returnMessage);
											} 
											catch (ConnectionFailException e) {} 
											catch (UnauthcatedClientException e) {}	
										}										
									});		
								}
								statusLabel.setValue("Adding gui.selection:" + gui_id);
							}		

							
							/*******************************TO BE ADDED***********************************/
							///// int input
							else if(gui_element.equals("gui.intinput")){
								statusLabel.setValue("gui.intinput not supported:" + gui_element);
								return;
							}
							///// double input
							else if(gui_element.equals("gui.doubleinput")){
								statusLabel.setValue("gui.doubleinput not supported:" + gui_element);
								return;
							}							
							
							
							else
							{
								statusLabel.setValue("gui_element not supported:" + gui_element);
								return;
							}
							_output.setCaption(caption);
							_output.setGuiType(gui_element);
							_output.setUpdateMode(update_mode);
							outputs.put(_outputPath, _output);
							if(belongToGroup){
								if(displayablesGroup.containsKey(groupName))
									displayablesGroup.get(groupName).addDisplayable(_output);
								else{
									DisplayableGroup _displayGrp = new DisplayableGroup(groupName);
									_displayGrp.addDisplayable(_output);
									_displayGrp.addToLayout(layout);
									displayablesGroup.put(groupName, _displayGrp);
								}									
							}
							else
								_output.addToLayout(layout);
							
						}
					//}
					
				}
				
			}
		}
		
	}
	
	
}