package edu.uq.workways.ioportlet;
//java
import java.util.Set;


//vaadin
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractLayout;


//io library
import edu.monash.io.iolibrary.ConfigurationConsts.DataType;
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.monash.io.iolibrary.exceptions.InvalidDataTypeException;
import edu.uq.workways.commons.utils.PropsUtil;

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
}
