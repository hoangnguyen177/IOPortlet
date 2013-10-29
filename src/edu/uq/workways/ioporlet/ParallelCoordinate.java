package edu.uq.workways.ioporlet;
//java
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
//vaadin
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.VerticalLayout;

import edu.monash.io.iolibrary.ConfigurationConsts.DataType;
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.monash.io.iolibrary.exceptions.InvalidDataTypeException;
//pc
import edu.uq.workways.ioporlet.parcoords.ParCoords;

public class ParallelCoordinate  implements Outputable {
	public ParallelCoordinate(String _id){
		this.setId(_id);
		parCoords = new ParCoords(this.id);
		parCoords.setProperties("pcwidth", 1000);
		parCoords.setProperties("pcheight", 600);
		parCoords.setProperties("brushed", true);
		parCoords.setWidth("1000px");
		parCoords.setHeight("600px");
	}

	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	@Override
	public void setOutputDataType(String out_datatype) {
		try {
			outputDataType = DataType.fromString(out_datatype);
		} catch (InvalidDataTypeException e) {
		}
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
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public String getOutputDataType() {
		return outputDataType.toString();
	}

	@Override
	public void setUpdateMode(String update_mode) {
		try {
			updateMode = UpdateMode.fromString(update_mode);
		} catch (InvalidDataTypeException e) {
		}
	}

	@Override
	public String getUpdateMode() {
		return updateMode.toString();
	}


	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void setGuiType(String _guiElement) {
		guiType = _guiElement;
	}

	@Override
	public String getGuiType() {
		return guiType;
	}

	@Override
	public boolean isEqual(String _otherId, String _otherGuiType,
			String _outputDataType, String _updateMode) {
		return id.equals(_otherId)&& guiType.equals(_otherGuiType)&& 
				outputDataType.toString().equals(_outputDataType)&& updateMode.toString().equals(_updateMode);
	}

	@Override
	public void addToLayout(AbstractLayout layout) {
		VerticalLayout _layout = new VerticalLayout();
		_layout.setHeight("700px");
		_layout.addComponent(parCoords);
		layout.addComponent(_layout);
	}
	
	
	@Override
	public void addData(String data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		//only accept Json formatted for now
		JsonParser parser = new JsonParser();
		JsonObject _data = null;
		try{
			JsonElement _element = parser.parse(data);
			if(_element.isJsonObject())
				_data = _element.getAsJsonObject();
			else
				throw new InvalidDataException("Invalid data:" + data);
		}
		catch(JsonSyntaxException e)
		{
			throw new InvalidDataException(e.getMessage());
		}
		Map<String, Object> _addedData = new HashMap<String, Object>();
		Set<Map.Entry<String,JsonElement>> entries = _data.entrySet();
		Iterator<Map.Entry<String, JsonElement>> _iterator = entries.iterator();
		while(_iterator.hasNext()){
			Map.Entry<String, JsonElement> _item = _iterator.next();
			String _key = _item.getKey();
			JsonElement _value = _item.getValue();
			if(_value.isJsonPrimitive()){
				JsonPrimitive _primitiveValue = _value.getAsJsonPrimitive();
				if(_primitiveValue.isBoolean())
					_addedData.put(_key, _primitiveValue.getAsBoolean());
				else if(_primitiveValue.isString())
					_addedData.put(_key, _primitiveValue.getAsString());
				else if(_primitiveValue.isNumber())
					_addedData.put(_key, _primitiveValue.getAsDouble());//DOUBLE at the moment				
			}
			else
				throw new InvalidDataException("Parallel Coordinates currently only accepts one level Json object");
		}
		System.out.println("adding data:" + _addedData);
		parCoords.addData(_addedData);
	}
	
	
	/**
	 * returns parallel coordinate object
	 */
	public ParCoords getParCoords(){
		return parCoords;
	}

	
	/******************************************************/
	private ParCoords 		parCoords 							= null;
	//private variables
	private UpdateMode 					updateMode 		= UpdateMode.APPEND; //does not make sense to overwrite here
	private DataType					outputDataType	= DataType.STRING;
	private String 							id			="";
	private String							caption		="";
	private String							guiType		="";

}
