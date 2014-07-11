package edu.uq.workways.ioportlet;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Button.ClickEvent;

import edu.uq.workways.ioportlet.IoportletUI.MessageType;

public class Options_Inputable extends DisplayObject implements Inputable {

	/***************************************************/
	private List<InputListener> listeners = new LinkedList<InputListener>();
	private boolean belongToGroup = false;
	private String groupName = "";
	private JsonArray arrayData  = null;
	private JsonArray arrayReturn = new JsonArray();
	/***************************************************/
	public Options_Inputable(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
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
		try{
			JsonElement _element = parser.parse(_data);
			if(_element.isJsonArray())
				arrayData = _element.getAsJsonArray();
			else
				throw new InvalidDataException("Invalid data:" + _data + ". Not JsonArray");
			
			
			((OptionGroup)component).removeAllItems();
			for(int i=0; i< arrayData.size(); i++){
				((OptionGroup)component).addItem(i);
				JsonObject _elem = arrayData.get(i).getAsJsonObject();
				Set<Map.Entry<String, JsonElement>> _entries = _elem.entrySet();
				String _caption = "";
				for(Map.Entry<String, JsonElement> _entry: _entries){
					String _key = _entry.getKey();
					String _value = _entry.getValue().toString();
					_caption += _value +" ";
				}
				((OptionGroup)component).setItemCaption(i, _caption);
			}
			if(!isRecordedMessage){
				Long _timeStampLong = message.get("timestamp").getAsLong();
				Timestamp _timeStamp = new Timestamp(_timeStampLong);
				Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
				try {
					this.saveMessage(message.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, "");
				} 
				catch (UnsupportedOperationException e) {} 
				catch (SQLException e) {}			
			}
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
	public void addInputListener(InputListener _listener) {
		listeners.add(_listener);
	}

	@Override
	public List<InputListener> getInputListeners() {
		return listeners;
	}

	@Override
	public void addToGroup(String groupname) {
		String _groupname = groupname.trim();
		if(_groupname.isEmpty())
			return;
		groupName = _groupname;
		belongToGroup = true;
		
	}

	@Override
	public JsonElement getUserInput() {
		if(component ==null)
			return null;
		Set<Integer> _chosenIndices = (Set)((OptionGroup)component).getValue();
    	for(Integer _index: _chosenIndices){
    		arrayReturn.add(arrayData.get(_index));
    	}
    	return arrayReturn;
	}
	
	
	@Override
	public void addToLayout(final AbstractLayout layout){
		super.addToLayout(layout);
		if(!belongToGroup){
			Button submitButton = new Button("Submit");
			submitButton.addClickListener(new Button.ClickListener(){
				@Override
				public void buttonClick(ClickEvent event) {
					JsonElement _inputsInGson = getUserInput();
					if(_inputsInGson ==null)
						return;
					for(InputListener _listener: listeners){
						_listener.onUserInput(_inputsInGson);
					}
					layout.setEnabled(false);
				}			
			});
			layout.addComponent(submitButton);
		}
	}

	@Override
	public void createDisplayObject() {
		component = new OptionGroup();
		((OptionGroup)component).setMultiSelect(true);
		((OptionGroup)component).setNullSelectionAllowed(false);
		((OptionGroup)component).setImmediate(true);
	}
	
	

}
