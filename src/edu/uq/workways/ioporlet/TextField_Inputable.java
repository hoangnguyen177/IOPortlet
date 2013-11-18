package edu.uq.workways.ioporlet;
//java
import java.util.LinkedList;
import java.util.List;



//gson
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
//vaadin
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * gui.textfield
 * INPUT
 * @author hoangnguyen
 *
 */
public class TextField_Inputable extends TextField implements Inputable{
	/*************************************************************/
	private List<InputListener> listeners = new LinkedList<InputListener>();
	private boolean belongToGroup = false;
	private String groupName = "";
	/*************************************************************/
	public TextField_Inputable(String _id) {
		super(_id);
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
					if(_inputsInGson == null)
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
	public void addInputListener(InputListener _listener) {
		listeners.add(_listener);
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
		if(component ==null || ((com.vaadin.ui.TextField)component).getValue().isEmpty())
			return null;
		return new JsonPrimitive(((com.vaadin.ui.TextField)component).getValue());
	}

}
