package edu.uq.workways.ioporlet;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * gui.textarea
 * INPUT
 * @author hoangnguyen
 *
 */
public class TextArea_Inputable extends TextArea implements Inputable{
	/*************************************************************/
	private List<InputListener> listeners = new LinkedList<InputListener>();
	/*************************************************************/
	
	public TextArea_Inputable(String _id) {
		super(_id);
	}
	
	
	@Override
	public void addInputListener(InputListener _listener) {
		listeners.add(_listener);
	}
	
	@Override
	public void addToLayout(AbstractLayout layout){
		super.addToLayout(layout);
		Button submitButton = new Button("Submit");
		submitButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				if(component ==null || ((com.vaadin.ui.TextArea)component).getValue().isEmpty())
					return;
				JsonElement _inputsInGson = new JsonPrimitive(((com.vaadin.ui.TextField)component).getValue());
				for(InputListener _listener: listeners){
					_listener.onUserInput(_inputsInGson);
				}
			}			
		});
		layout.addComponent(submitButton);
	}

}
