package edu.uq.workways.ioportlet;
//java
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;








//gson
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
//vaadin
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Button.ClickEvent;

import edu.uq.workways.ioportlet.IoportletUI.MessageType;

public class BooleanInput_Inputable extends DisplayObject implements Inputable {
	/*************************************************************/
	private List<InputListener> listeners = new LinkedList<InputListener>();
	private boolean belongToGroup = false;
	private String groupName = "";
	/*************************************************************/
	public BooleanInput_Inputable(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
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
	public void addData(JsonObject message)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		boolean isRecordedMessage = false;
		if(message.has("recorded"))
			isRecordedMessage = message.get("recorded").getAsBoolean();
		String _path = message.get("path").getAsString();
		String _data = message.get("data").getAsString();
		if(!isRecordedMessage){
			Long _timeStampLong = message.get("timestamp").getAsLong();
			Timestamp _timeStamp = new Timestamp(_timeStampLong);
			Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
			try {
				this.saveMessage(message.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, "");
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		//only get data from message and set the caption
		((CheckBox)component).setCaption(_data);		
				
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
	public void addToLayout(AbstractLayout layout){
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
				}			
			});
			layout.addComponent(submitButton);
		}
	}
	

	@Override
	public JsonElement getUserInput() {
		if(component ==null)
			return null;
		return new JsonPrimitive(((CheckBox)component).getValue());
	}

	@Override
	public void createDisplayObject() {
		component = new CheckBox();
		((CheckBox)component).setValue(true);
	}
	
	
	

}
