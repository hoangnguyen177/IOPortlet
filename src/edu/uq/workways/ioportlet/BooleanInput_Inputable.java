package edu.uq.workways.ioportlet;
//java
import java.util.LinkedList;
import java.util.List;
import java.util.Set;



//gson
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
//vaadin
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Button.ClickEvent;

public class BooleanInput_Inputable extends DisplayObject implements Inputable {
	/*************************************************************/
	private List<InputListener> listeners = new LinkedList<InputListener>();
	private boolean belongToGroup = false;
	private String groupName = "";
	/*************************************************************/
	public BooleanInput_Inputable(String _id){
		this.setId(_id);
	}
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	@Override
	public void addData(String data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		//no need
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
