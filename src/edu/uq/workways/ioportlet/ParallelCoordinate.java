package edu.uq.workways.ioportlet;
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
import edu.uq.workways.ioportlet.parcoords.ParCoords;

public class ParallelCoordinate  extends DisplayObject{
	/**
	 * constructor
	 * @param _id
	 */
	public ParallelCoordinate(String _id){
		this.setId(_id);
		component = new ParCoords(this.id);
		((ParCoords)component).setProperties("pcwidth", 1000);
		((ParCoords)component).setProperties("pcheight", 600);
		((ParCoords)component).setProperties("brushed", true);
		((ParCoords)component).setWidth("1000px");
		((ParCoords)component).setHeight("600px");
	}

	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void addToLayout(AbstractLayout layout) {
		VerticalLayout _layout = new VerticalLayout();
		_layout.setHeight("700px");
		_layout.addComponent(component);
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
		((ParCoords)component).addData(_addedData);
	}
	
	//not needed, update ParCoords in every addition
	@Override
	public void update() throws InvalidDataException{
		
	}
	
}
