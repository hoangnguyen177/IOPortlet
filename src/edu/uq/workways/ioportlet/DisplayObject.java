package edu.uq.workways.ioportlet;

//java
import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.vaadin.data.util.sqlcontainer.SQLContainer;
//vaadin
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractLayout;








import com.vaadin.ui.UI;

//io library
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.monash.io.iolibrary.exceptions.InvalidDataTypeException;
import edu.uq.workways.commons.utils.PropsUtil;
import edu.uq.workways.commons.utils.VaadinHelper;
import edu.uq.workways.ioportlet.IoportletUI.MessageType;

public abstract class DisplayObject implements Displayable{
	/*****************************************************/
	protected AbstractComponent 			component		= null;
	//private variables
	protected UpdateMode 					updateMode 		= UpdateMode.APPEND; 
	protected String 							id			="";
	protected String							caption		="";
	protected String							guiType		="";
	private   PropsUtil						propsUtil 		= new PropsUtil("resource/ioportlet.properties");
	private String tempfile									= propsUtil.get("tempfile");
	private String linkName									= propsUtil.get("linkname");
	private SQLContainer 					messageContainer= null;
	private SQLContainer				sourceSinkContainer = null;
	
	private String							userName		= "";
	private UI								parentUI		= null;
	
	/*****************************************************/
	
	public AbstractComponent getComponent(){
		return component;
	}
	
	@Override
	public void setUpdateMode(String update_mode) {
		try {
			updateMode = UpdateMode.fromString(update_mode);
		} catch (InvalidDataTypeException e) {
			e.printStackTrace();
		}
		createDisplayObject();
	}
	

	@Override
	public String getUpdateMode() {
		return updateMode.toString();
	}

	@Override
	public void setId(String _id) {
		id = _id;
	}


	@Override
	public String getId() {
		return id;
	}


	@Override
	public void setCaption(String _caption) {
		caption = _caption;
		if(component!=null)
			component.setCaption(_caption);
	}


	@Override
	public String getCaption() {
		return caption;
	}


	@Override
	public void setGuiType(String _guiElement) {
		guiType = _guiElement;
	}


	@Override
	public boolean isEqual(String _otherId, String _otherGuiType, String _updateMode) {
		return id.equals(_otherId)&& guiType.equals(_otherGuiType)&& updateMode.toString().equals(_updateMode);
	}


	@Override
	public String getGuiType() {
		return guiType;
	}

	@Override
	public void addToLayout(AbstractLayout layout) {
		if(component!=null)
			layout.addComponent(component);
	}

	/**
	 * returns the tempDir
	 * @return
	 */
	public String getTempDir(){
		return tempfile;
	}
	
	/**
	 * return the location of the link to tempDir
	 * @return
	 */
	public String linkName(){
		return linkName;
	}
	
	/**
	 * createDisplayObject
	 */
	public abstract void createDisplayObject();
	
	/**
	 * setMessageContainer
	 * @param _con
	 */
	public void setMessageContainer(SQLContainer _con){
		messageContainer = _con;
	}
	
	/**
	 * get message container
	 * @return
	 */
	public SQLContainer getMessageContainer(){
		return messageContainer;
	}
	
	/**
	 * setSourceSinkContainer
	 * @param container
	 */
	public void setSourceSinkId(SQLContainer container){
		sourceSinkContainer = container;
	}
	
	/**
	 * getSourceSinkContainer
	 * @return
	 */
	public SQLContainer getSourceSinkContainer(){
		return sourceSinkContainer;
	}
	
	@Override
	public void setUserName(String _username) {
		userName = _username;
	}

	@Override
	public String getUserName() {
		return userName;
	}
	
	/**
	 * get the location to store the files
	 * @return
	 */
	public String getStorePath(){
		String storepath = this.getTempDir() +"/"+ this.getUserName() +"/"+ this.getClass().getName();
		//create if not exists
		File directory = new File(storepath);
		if (!directory.exists())
	    	directory.mkdirs();
	    return storepath;
	}
	
	/**
	 * saveMessage
	 * @param message
	 * @param type
	 * @param path
	 * @param tstamp
	 * @param sourcesinkid
	 * @param filepath
	 * @throws SQLException 
	 * @throws UnsupportedOperationException 
	 */
	public void saveMessage(String message, String type, String path, Timestamp tstamp, long sourcesinkid, String filepath) throws UnsupportedOperationException, SQLException{
		getMessageContainer().removeAllContainerFilters();
		Object _newItemId = getMessageContainer().addItem();
		getMessageContainer().getContainerProperty(_newItemId, "message").setValue(message);//nothing, since file is stored
		getMessageContainer().getContainerProperty(_newItemId, "type").setValue(type);
		getMessageContainer().getContainerProperty(_newItemId, "path").setValue(path);
		getMessageContainer().getContainerProperty(_newItemId, "tstamp").setValue(tstamp);
		getMessageContainer().getContainerProperty(_newItemId, "sourcesink_id").setValue(sourcesinkid);
		getMessageContainer().getContainerProperty(_newItemId, "filepath").setValue(filepath);
		
		Object _connectionRowId = VaadinHelper.findRowWithValue(getSourceSinkContainer(), "id", sourcesinkid);
		boolean _needToUpdateLatestTimeStamp = false;
		if(_connectionRowId != null){
			//what to do if this one is null?assume its not null
			if(getSourceSinkContainer().getContainerProperty(_connectionRowId, "lastmessagestamp")!= null &&
					getSourceSinkContainer().getContainerProperty(_connectionRowId, "lastmessagestamp").getValue()!= null){
				
				Timestamp _lastTimeStampRecorded = (Timestamp)getSourceSinkContainer().getContainerProperty(_connectionRowId, "lastmessagestamp").getValue();
				if(_lastTimeStampRecorded.before(tstamp)){
					_needToUpdateLatestTimeStamp = true;
				}
			}
			else
				_needToUpdateLatestTimeStamp = true;
			
			if(_needToUpdateLatestTimeStamp == true)
				getSourceSinkContainer().getContainerProperty(_connectionRowId, "lastmessagestamp").setValue(tstamp);
		}
		getMessageContainer().commit();
		if(_needToUpdateLatestTimeStamp)
			getSourceSinkContainer().commit();
	}
	
	/**
	 * 
	 * @param _ui
	 */
	public void setParentUI(UI _ui){
		parentUI = _ui;
	}
	/**
	 * 
	 * @return
	 */
	public UI getParentUI(){
		return parentUI;
	}
	
}
