package edu.uq.workways.ioportlet;

//java
import static edu.uq.workways.commons.Constants.DB_DRIVER;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

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
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
//gson
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;

import edu.monash.io.iolibrary.ConfigurationConsts.DataType;
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.monash.io.iolibrary.exceptions.InvalidDataTypeException;
//socket io
import edu.monash.io.socketio.connection.SinkConnection;
import edu.monash.io.socketio.connection.SinkListener;
import edu.monash.io.socketio.connection.ConnectionConsts;
import edu.monash.io.socketio.exceptions.ConnectionFailException;
import edu.monash.io.socketio.exceptions.InvalidMessageException;
import edu.monash.io.socketio.exceptions.InvalidSourceException;
import edu.monash.io.socketio.exceptions.UnauthcatedClientException;
import edu.uq.workways.commons.CommonProperties;
import edu.uq.workways.commons.utils.DatabaseHelper;
import edu.uq.workways.commons.utils.PropsUtil;
import edu.uq.workways.commons.utils.VaadinHelper;

import com.vaadin.server.DeploymentConfiguration;

@PreserveOnRefresh
@Widgetset("edu.uq.workways.ioportlet.widgetset.IoportletWidgetset")
public class IoportletUI extends UI{
	private static final long serialVersionUID = 1L;

	@WebServlet(value = "/*", asyncSupported = true, initParams = {
            @WebInitParam(name = "ui", value = "edu.uq.workways.ioportlet.IoportletUI"),
            @WebInitParam(name = "productionMode", value = "false") })
	public static class Servlet extends VaadinServlet {
	}
	
	public static enum MessageType {
		//definition: for declaring what types of UIs
		//source: messsages from source
		//sink: messages from sink
		//modification: if there is any changes in the definition, and its date
		//TODO: implement modification later		
        definition, source, sink, modification ;
        public static MessageType fromString(String s) {
            if (s != null) {
            	MessageType[] vs = values();
                for (int i = 0; i < vs.length; i++) {
                    if (vs[i].toString().equalsIgnoreCase(s)) {
                        return vs[i];
                    }
                }
            }
            return null;
        }
        public static String[] stringValues() {
        	MessageType[] vs = values();
            String[] ss = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                ss[i] = vs[i].toString();
            }
            return ss;
        }
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
	//messages parameter
	private SQLContainer 				messageContainer			= null;
	private SQLContainer 				sourceSinkContainer			= null;
	private JDBCConnectionPool			connectionPool				= null;
	
	//parameters 
	private String 						host 						= "localhost";
	private String 						protocol					= "http";
	private int							timeout						= 10;
	private int							port						= 9090;	
	private String						nsp							= "/";
	private String						sourceId					= "";
	private Long						sourcesinkId				= -1L;
	
	private Map<String, Displayable>		outputs					= new HashMap<String, Displayable>();
	//groups of inputtables
	private Map<String, DisplayableGroup>	displayablesGroup		= new HashMap<String, DisplayableGroup>();	
	//message queue
	private boolean							portletGuiInitialised	= false;
	private JsonObject						sourceDefinition		= null;
	
	private String 							portletId				= "";
	
	
		
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
			portletId= portletDisplay.getId();
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
			liferayScreenName = "hoangnguyen";
			host = "localhost";
			protocol = "http";
			timeout = 10;
			port = 9090;
			nsp = "/";
			sourceId =  "WdFfEEU70Ddd8KQ_AAAA";			
		}
		try {
			initSQLContainer();
			//if local testing, and there is no entry with source id, then create a new one
			if(isLocalTesting){
				sourceSinkContainer.addContainerFilter(new Equal("sourceid", sourceId));
				if(sourceSinkContainer.size() == 0){
					sourceSinkContainer.removeAllContainerFilters();
					Object _newItemId = sourceSinkContainer.addItem();
					sourceSinkContainer.getContainerProperty(_newItemId, "portletid").setValue("portletid");
					sourceSinkContainer.getContainerProperty(_newItemId, "sourceid").setValue(sourceId);
					sourceSinkContainer.getContainerProperty(_newItemId, "workflowrun_id").setValue(1L);
				}
				sourceSinkContainer.commit();
			}
		} catch (SQLException e) {
			Notification.show("Error connecting to the database:" + e.getMessage(), Type.ERROR_MESSAGE);
			return;
		}
		/*reconstrut IO portlet*/
		reconstructIOPortletFromDatabase();
		Refresher refresher = new Refresher();
	    refresher.setRefreshInterval(500);
	    addExtension(refresher);
        
		if(sink==null)
			this.createConnection();
		
		//detach listener
		addDetachListener(new DetachListener() {
            @Override
            public void detach(DetachEvent event) {
            	releaseResource();
            }
        });
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
	 * initialise ioportlet from database
	 */
	private void reconstructIOPortletFromDatabase(){
		JsonParser _parser = new JsonParser();
		//get the sourcesink id
		Object _itemId = VaadinHelper.findRowWithValue(sourceSinkContainer, "sourceid", sourceId);
		if(_itemId == null)
		{
			Notification.show("Cannot find source id:" + sourceId, Type.WARNING_MESSAGE);
			return;
		}
		sourcesinkId = (Long)sourceSinkContainer.getContainerProperty(_itemId, "id").getValue();
		//using the filter
		messageContainer.removeAllContainerFilters();
		messageContainer.addContainerFilter(new And(new Equal("sourcesink_id", sourcesinkId), new Equal("type", MessageType.definition.toString())));
		Collection<?> _messagesIds = messageContainer.getItemIds();
		if(_messagesIds.size() == 0)
			return; // no definition yet
		//init the GUI elements
		for(Object _messageId: _messagesIds){
			JsonParser parser = new JsonParser();
			if(messageContainer.getContainerProperty(_messageId, "message") == null|| 
					messageContainer.getContainerProperty(_messageId, "message").getValue() == null)
				continue;
			try{
				if(!portletGuiInitialised){
					sourceDefinition = parser.parse((String)messageContainer.getContainerProperty(_messageId, "message").getValue()).getAsJsonObject();
					parseSourceDefinition(sourceDefinition);
					portletGuiInitialised = true;
				}
			}
			catch(JsonSyntaxException e){
				Notification.show("Invalid definition:" + messageContainer.getContainerProperty(_messageId, "message").getValue());
				continue;
			}
			break;	
		}
		//get the latest time stamp
		//this is to deal with cases when lastMessageStamp is not updated even when some messages are saved
		Timestamp _lastRecordedTimeStamp = null;
		boolean  _lastMessageTimeStampChanged = false;
		if(sourceSinkContainer.getContainerProperty(_itemId, "lastmessagestamp")!= null &&
				sourceSinkContainer.getContainerProperty(_itemId, "lastmessagestamp").getValue()!= null){
			_lastRecordedTimeStamp = (Timestamp)sourceSinkContainer.getContainerProperty(_itemId, "lastmessagestamp").getValue();
		}
		
		
		//map of path and update modes for this sourceid
		Map<String, String> pathUpdateModes = getPathUpdateMode(sourceDefinition);
		Set<String> paths = pathUpdateModes.keySet();
		//populate the messages based on the path
		for(String path: paths){
			UpdateMode updateMode = null;
			messageContainer.removeAllContainerFilters();
			//only worry about messages from source atm
			messageContainer.addContainerFilter(new And(new Equal("sourcesink_id", sourcesinkId),new Equal("path", path), new Equal("type", MessageType.source.toString())));
			messageContainer.sort(new Object[]{"tstamp"}, new boolean[]{true});
			
			//get the one with the latest tstamp
			Object _lastMessageId = messageContainer.lastItemId();
			if(messageContainer.getContainerProperty(_lastMessageId, "tstamp")!=null &&  
					messageContainer.getContainerProperty(_lastMessageId, "tstamp").getValue()!=null){
				Timestamp _lastMessageTimeStamp = (Timestamp) messageContainer.getContainerProperty(_lastMessageId, "tstamp").getValue();
				if(_lastRecordedTimeStamp ==null || _lastRecordedTimeStamp.before(_lastMessageTimeStamp))
				{
					_lastRecordedTimeStamp = _lastMessageTimeStamp;
					_lastMessageTimeStampChanged = true;
				}
			}
			
			try {
				updateMode = UpdateMode.fromString(pathUpdateModes.get(path));
			} catch (InvalidDataTypeException e) {
				updateMode = UpdateMode.OVERWRITE;
			}
			if(updateMode == UpdateMode.APPEND){
				//get all messages
				Collection<?> _messageIds = messageContainer.getItemIds();
				for(Object _messageId: _messageIds){
					String _msgString = (String)messageContainer.getContainerProperty(_messageId, "message").getValue();
					try{
						JsonObject _msg = _parser.parse(_msgString).getAsJsonObject();
						_msg.addProperty("recorded", true);
						if(!_msg.has("sourcesinkid"))
							_msg.addProperty("sourcesinkid", sourcesinkId);
						_msg.addProperty("filepath", (String)messageContainer.getContainerProperty(_messageId, "filepath").getValue());
						_msg.addProperty("append", false);
						
						try {
							outputs.get(path).addData(_msg);
						} catch (UpperLimitNumberOfSeriesException e) {
							e.printStackTrace();
						} catch (InvalidDataException e) {
							e.printStackTrace();
						}						
					}
					catch(JsonSyntaxException e){ continue;}
				}
			}
			else if(updateMode == UpdateMode.OVERWRITE){
				//only worry about the last message
				Object _lastItemId = messageContainer.lastItemId();
				String _lastMessageStr = (String)messageContainer.getContainerProperty(_lastItemId, "message").getValue();
				try{
					JsonObject _lastMessage = _parser.parse(_lastMessageStr).getAsJsonObject();
					_lastMessage.addProperty("recorded", true);
					if(!_lastMessage.has("sourcesinkid"))
						_lastMessage.addProperty("sourcesinkid", sourcesinkId);
					_lastMessage.addProperty("filepath", (String)messageContainer.getContainerProperty(_lastItemId, "filepath").getValue());
					_lastMessage.addProperty("append", false);

					if(!outputs.containsKey(path)){
						return;
					}
					try {
						outputs.get(path).addData(_lastMessage);
					} catch (UpperLimitNumberOfSeriesException e) {
						e.printStackTrace();
					} catch (InvalidDataException e) {
						e.printStackTrace();
					}
				}
				catch(JsonSyntaxException e){}
			}
		}
		
		//updated
		if(_lastMessageTimeStampChanged){
			sourceSinkContainer.getContainerProperty(_itemId, "lastmessagestamp").setValue(_lastRecordedTimeStamp);
			try{
				sourceSinkContainer.commit();
			}
			catch(Exception e){/*ignore*/}
		}
	
		
		
	}
	/**
	 * create the connection
	 */
	private void createConnection(){
		System.out.println("Creating connection");
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
//				try {
//					sink.disconnect();
//				} catch (ConnectionFailException e) {
//					e.printStackTrace();
//				}
			}
			public void onAuthResponse(JsonObject authResponse){
				boolean authResult = Boolean.parseBoolean(authResponse.get("authresult").getAsString());
				JsonObject sourcesList = authResponse.getAsJsonObject("sourcelist");
				if(sourcesList.has(sourceId)){
					try {
						statusLabel.setValue("Select source:" + sourceId);
						//now get the timestamp/if null then return 
						Object _connectionRowId = VaadinHelper.findRowWithValue(sourceSinkContainer, "id", sourcesinkId);
						if(_connectionRowId != null){
							Timestamp _lastMessageFromServer = null;
							if(sourceSinkContainer.getContainerProperty(_connectionRowId, "lastmessagestamp")!= null &&
									sourceSinkContainer.getContainerProperty(_connectionRowId, "lastmessagestamp").getValue()!= null){
								_lastMessageFromServer = (Timestamp)sourceSinkContainer.getContainerProperty(_connectionRowId, "lastmessagestamp").getValue();
							}
							sink.selectSource(sourceId, _lastMessageFromServer);
						}
					} catch (UnauthcatedClientException e) {
						statusLabel.setValue("Error@selecting source:" + e.getMessage()+ ".Exit!");
						return;
					} catch (InvalidSourceException e) {
						statusLabel.setValue("Error@selecting source:" + e.getMessage()+ ".Exit!");
						return;
					}
					//selecting source successfully, now creating items if they are not created
					//TODO: now just simplify it, later I need to check whether its different
					sourceDefinition = sourcesList.getAsJsonObject(sourceId);
					if(!portletGuiInitialised){
						parseSourceDefinition(sourceDefinition);
						portletGuiInitialised = true;
						//save the sourceDefinition
						Long _timeStampLong = authResponse.get("timestamp").getAsLong();
						Timestamp _timeStamp = new Timestamp(_timeStampLong);
						//save this message
						messageContainer.removeAllContainerFilters();
						Object _newItemId = messageContainer.addItem();
						messageContainer.getContainerProperty(_newItemId, "message").setValue(sourceDefinition.toString());
						messageContainer.getContainerProperty(_newItemId, "type").setValue(MessageType.definition.toString());
						messageContainer.getContainerProperty(_newItemId, "path").setValue(sourceId+"");//definition needs no path
						messageContainer.getContainerProperty(_newItemId, "sourcesink_id").setValue(sourcesinkId);
						messageContainer.getContainerProperty(_newItemId, "tstamp").setValue(_timeStamp);						
					}
				}
				else{
					statusLabel.setValue("source:" + sourceId + " is not in sourcelist:" + sourcesList + ". Exit!");
					return;
				}
				
			}
			public void onMessage(JsonObject aMessage) throws InvalidMessageException{
				String path = aMessage.get("path").getAsString();
				aMessage.addProperty("sourcesinkid", sourcesinkId);
				if(!outputs.containsKey(path)){
					return;
				}
				try {
					outputs.get(path).addData(aMessage);
				} catch (UpperLimitNumberOfSeriesException e) {
					e.printStackTrace();
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
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
				System.out.println("Permission  changed:" + newPermission);
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
	 * init SQL container, create a table just for messages of the user
	 * @throws SQLException
	 */
	private void initSQLContainer() throws SQLException{
		connectionPool 				= DatabaseHelper.getCommonConnectionPool();
		String databaseDriver = CommonProperties.get(DB_DRIVER);
        if(databaseDriver == null || databaseDriver.isEmpty())
            databaseDriver = "org.postgresql.Driver";
        String initStatement = "CREATE TABLE IF NOT EXISTS "+ liferayScreenName +"(" +
								"id BIGSERIAL NOT NULL," +
								"type varchar(10), " +
								"message text," + 
								"tstamp TIMESTAMP WITH TIME ZONE,"+
								"sourcesink_id BIGINT NOT NULL references sourcesink(id) ON DELETE CASCADE,"+
								"path text,"+
								"filepath text,"+
								"CONSTRAINT message_key PRIMARY KEY (id));";
        try{
            DatabaseHelper.initTable(connectionPool, databaseDriver, liferayScreenName, initStatement);        
        }
        catch(SQLException e){}//in case the table is already there
        messageContainer 			= DatabaseHelper.initContainers(connectionPool, liferayScreenName);     
        sourceSinkContainer 			= DatabaseHelper.initContainers(connectionPool, "sourcesink");    
    }
	
	/**
	 * get the update mode from the definition
	 * @param definition
	 * @return
	 */
	private Map<String, String> getPathUpdateMode(JsonObject definition){
		Map<String, String> pathUpdateModes = new HashMap<String, String>();
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
					pathUpdateModes.put(_outputPath, update_mode);
				}
			}
		}		
		return pathUpdateModes;
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
									_output = new LineGraph(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);
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
									_output = new ParallelCoordinate_Inputable(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);
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
									_output = new ParallelCoordinate(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
								}
								statusLabel.setValue("Adding parallel coordinates id:" + gui_id);
							}
							
							//////////text area
							else if(gui_element.equals("gui.textarea")){
								if(_channelItemType.equals("INPUT")){
									_output = new TextArea_Inputable(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);

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
									_output = new TextArea(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);

								}
								statusLabel.setValue("Adding textarea:" + gui_id);
							}
							//////////text field
							else if(gui_element.equals("gui.textfield")){
								if(_channelItemType.equals("INPUT")){
									_output = new TextField_Inputable(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);

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
									_output = new TextField(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);

								}
								statusLabel.setValue("Adding textfield:" + gui_id);
							}
							///// boolean input
							else if(gui_element.equals("gui.booleaninput")){
								if(_channelItemType.equals("INPUT")){
									_output = new BooleanInput_Inputable(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);

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
									_output = new Image(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);

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
									_output = new ImageSlide(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);

									statusLabel.setValue("Adding imageslide:" + gui_id);
								}
								
							}
							
							///////large image
							else if(gui_element.equals("gui.largeimage")){
								if(_channelItemType.equals("INPUT")){
									statusLabel.setValue("gui.largeimage does not have INPUT option yet");
									return;
								}
								else if(_channelItemType.equals("OUTPUT")){
									
									if(_channelItemAsObject.has("image_type"))
									{
										String imgType = (String)_channelItemAsObject.get("image_type").getAsString();
										_output = new LargeImage(gui_id, liferayScreenName, messageContainer, sourceSinkContainer, imgType);
									}
									else
										_output = new LargeImage(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);
									statusLabel.setValue("Adding gui.largeimage:" + gui_id);
								}
								
							}
							
							///////video
							else if(gui_element.equals("gui.video")){
								if(_channelItemType.equals("INPUT")){
									statusLabel.setValue("gui.video does not have INPUT option yet");
									return;
								}
								else if(_channelItemType.equals("OUTPUT")){
									if(_channelItemAsObject.has("video_type"))
									{
										String videoType = (String)_channelItemAsObject.get("video_type").getAsString();
										_output = new Video(gui_id, liferayScreenName, messageContainer, sourceSinkContainer, videoType);	
									}
									else
										_output = new Video(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);
									statusLabel.setValue("Adding gui.video:" + gui_id);
								}
								
							}
		
							///minc viewer
							else if(gui_element.equals("mincviewer.file")){
								if(_channelItemType.equals("INPUT")){
									statusLabel.setValue("mincviewer.file does not have INPUT option yet");
									return;
								}
								else if(_channelItemType.equals("OUTPUT")){
									statusLabel.setValue("Adding mincviewer.file:" + gui_id);
									_output = new MincViewer(gui_id, liferayScreenName, messageContainer, sourceSinkContainer, true);
									_output.setMessageContainer(messageContainer);
								}
							}
							else if(gui_element.equals("mincviewer.url")){
								if(_channelItemType.equals("INPUT")){
									statusLabel.setValue("mincviewer.url does not have INPUT option yet");
									return;
								}
								else if(_channelItemType.equals("OUTPUT")){
									_output = new MincViewer(gui_id, liferayScreenName, messageContainer, sourceSinkContainer, false);
									_output.setMessageContainer(messageContainer);
									statusLabel.setValue("Adding mincviewer.url:" + gui_id);
								}
							}							
							///// selection
							else if(gui_element.equals("gui.selection")){
								if(_channelItemType.equals("OUTPUT")){
									_output = new Options(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);
								}
								else if(_channelItemType.equals("INPUT")){
									_output = new Options_Inputable(gui_id, liferayScreenName, messageContainer, sourceSinkContainer);
									_output.setMessageContainer(messageContainer);
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
							_output.setParentUI(this);
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
	
	
	public void releaseResource() {
		System.out.println("---------------detaching-----------------");
		if(sink!= null && sink.isConnected())
		{
			try {
				sink.disconnect();
				sink = null;
			} catch (ConnectionFailException e) {}			
		}
	}
	
}