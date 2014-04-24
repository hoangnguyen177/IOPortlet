package edu.uq.workways.ioportlet;

import java.util.Set;

import com.vaadin.ui.AbstractLayout;

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
	 * add data: data is in string --> convert to appropriate mode
	 * the graph needs to add to appripriate series
	 * if update is true, update, otherwise just update data
	 * @param data
	 * @param serieId
	 * @param update
	 * @throws UpperLimitNumberOfSeriesException
	 */
	public void addData(String data, String serieId, boolean update) throws UpperLimitNumberOfSeriesException, InvalidDataException;

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
}
