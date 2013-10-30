package edu.uq.workways.ioporlet;
//java
import java.util.Set;

//vaadin
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractLayout;

//io library
import edu.monash.io.iolibrary.ConfigurationConsts.DataType;
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.monash.io.iolibrary.exceptions.InvalidDataTypeException;

public abstract class DisplayObject implements Displayable{
	/*****************************************************/
	protected AbstractComponent 			component		= null;
	//private variables
	protected UpdateMode 					updateMode 		= UpdateMode.APPEND; 
	protected DataType					outputDataType	= DataType.STRING;
	protected String 							id			="";
	protected String							caption		="";
	protected String							guiType		="";
	/*****************************************************/
	
	public AbstractComponent getComponent(){
		return component;
	}
	
	@Override
	public void setOutputDataType(String out_datatype) {
		try {
			outputDataType = DataType.fromString(out_datatype);
		} catch (InvalidDataTypeException e) {}
	}

	@Override
	public void setUpdateMode(String update_mode) {
		try {
			updateMode = UpdateMode.fromString(update_mode);
		} catch (InvalidDataTypeException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public String getOutputDataType() {
		return outputDataType.toString();
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
	public boolean isEqual(String _otherId, String _otherGuiType,
			String _outputDataType, String _updateMode) {
		return id.equals(_otherId)&& guiType.equals(_otherGuiType)&& 
				outputDataType.toString().equals(_outputDataType)&& updateMode.toString().equals(_updateMode);
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

}
