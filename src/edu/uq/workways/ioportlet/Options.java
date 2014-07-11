package edu.uq.workways.ioportlet;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

import edu.uq.workways.ioportlet.IoportletUI.MessageType;

public class Options extends DisplayObject{

	public Options(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		this.setId(_id);
		this.setUserName(uname);
		this.setMessageContainer(msgContainer);
		this.setSourceSinkId(sourceSinkContainer);
	}
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	@Override
	public void addData(JsonObject message)	throws UpperLimitNumberOfSeriesException, InvalidDataException {
		String _path = message.get("path").getAsString();
		String _data = message.get("data").getAsString();
		boolean isRecordedMessage = false;
		if(message.has("recorded"))
			isRecordedMessage = message.get("recorded").getAsBoolean();
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
			if(!isRecordedMessage){
				Long _timeStampLong = message.get("timestamp").getAsLong();
				Timestamp _timeStamp = new Timestamp(_timeStampLong);
				Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
				try {
					this.saveMessage(message.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, "");
				} catch (UnsupportedOperationException e) {
				} catch (SQLException e) {
				}			
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

	@Override
	public void createDisplayObject() {
		component = new Label();
		((Label)component).setContentMode(ContentMode.HTML);
	}

}
