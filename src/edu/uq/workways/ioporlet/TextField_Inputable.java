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
	/*************************************************************/
	public TextField_Inputable(String _id) {
		super(_id);
	}
	
	
	@Override
	public void addToLayout(AbstractLayout layout){
		Button submitButton = new Button("Submit");
		submitButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				if(component ==null || ((com.vaadin.ui.TextField)component).getValue().isEmpty())
					return;
				JsonElement _inputsInGson = new JsonPrimitive(((com.vaadin.ui.TextField)component).getValue());
				for(InputListener _listener: listeners){
					_listener.onUserInput(_inputsInGson);
				}
			}			
		});
		HorizontalLayout _buttonLayout = new HorizontalLayout();
		_buttonLayout.addComponent(component);
		_buttonLayout.addComponent(submitButton);
		layout.addComponent(_buttonLayout);
	}
	
	
	@Override
	public void addInputListener(InputListener _listener) {
		listeners.add(_listener);
	}

}
