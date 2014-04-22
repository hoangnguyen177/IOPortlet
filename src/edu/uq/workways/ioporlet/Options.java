package edu.uq.workways.ioporlet;

import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class Options extends DisplayObject{

	public Options(){
		component = new Label();
		((Label)component).setContentMode(ContentMode.HTML);
	}
	
	public Options(String _id){
		this.setId(_id);
		component = new Label();
		((Label)component).setContentMode(ContentMode.HTML);
	}
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	@Override
	public void addData(String _data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		//_data supposed to be array json
		JsonParser parser = new JsonParser();
		JsonArray _arrayData = null;
		try{
			JsonElement _element = parser.parse(_data);
			if(_element.isJsonArray())
				_arrayData = _element.getAsJsonArray();
			else
				throw new InvalidDataException("Invalid data:" + _data + ". Not JsonArray");
			String _contents = "";
			for(int i=0; i< _arrayData.size(); i++){
				_contents += "<i>" + _arrayData.get(i).toString() + "</i>";
			}
			((Label)component).setValue(_contents);
		}
		catch(JsonSyntaxException e)
		{
			throw new InvalidDataException(e.getMessage());
		}
	}

	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void update() throws InvalidDataException {
		
	}

}
