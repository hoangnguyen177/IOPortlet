package edu.uq.workways.ioportlet;

import java.util.Set;

import com.google.gson.JsonObject;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.UI;

/**
 * interface that can display objects
 * @author hoangnguyen
 *
 */
public interface Displayable {
	
	/**
	 * returns the number of data series
	 * @return
	 */
	public int getNumberOfSeries();
	
	
	/**
	 * set unique name for this outputable
	 * @param name: must be unique
	 */
	public void setId(String id);
	
	/**
	 * get id
	 * @return
	 */
	public String getId();
	/**
	 * set caption
	 * @param caption
	 */
	public void setCaption(String caption);
	
	/**
	 * get caption
	 * @return
	 */
	public String getCaption();
	
	/**
	 * set update mode
	 * default = overwrite
	 * @param update_mode
	 */
	public void setUpdateMode(String update_mode);
	
	/**
	 * @return
	 */
	public String getUpdateMode();
	
	/**
	 * addData
	 * @param data
	 * @throws UpperLimitNumberOfSeriesException
	 * @throws InvalidDataException
	 */
	public void addData(JsonObject message) throws UpperLimitNumberOfSeriesException, InvalidDataException;
	/**
	 * 
	 * @return
	 */
	public Set<String> getDataSeriesIds();
	
	/**
	 * set type of the gui element
	 * @param _guiElement
	 */
	public void setGuiType(String _guiElement);
	
	/**
	 * get GUI type
	 * @return
	 */
	public String getGuiType();
	
	/**
	 * compare this Outputable with otehr type
	 * @param _otherId
	 * @param _otherType
	 * @return
	 */
	public boolean isEqual(String _otherId, String _otherGuiType, String _updateMode);
	
	/**
	 * add to layout
	 * @param layout
	 */
	public void addToLayout(AbstractLayout layout);
	
	/**
	 * bring the update to the GUI, called by addData when update is true
	 */
	public void update() throws InvalidDataException;
	
	/**
	 * setMessageContainer
	 * @param container SQLContaienr
	 */
	public void setMessageContainer(SQLContainer container);
	
	/**
	 * getMessageContainer
	 * @return SQLContainer
	 */
	public SQLContainer getMessageContainer();
	
	/**
	 * setSourceSinkContainer
	 * @param container
	 */
	public void setSourceSinkId(SQLContainer container);
	
	/**
	 * getSourceSinkContainer
	 * @return
	 */
	public SQLContainer getSourceSinkContainer();
	
	/**
	 * setUserName
	 * @param username
	 */
	public void setUserName(String username);
	
	/**
	 * getUserName
	 * @return
	 */
	public String getUserName();
	
	/**
	 * get the location to store the files
	 * @return
	 */
	public String getStorePath();
	
	/**
	 * 
	 * @param _ui
	 */
	public void setParentUI(UI _ui);
	/**
	 * 
	 * @return
	 */
	public UI getParentUI();
}
