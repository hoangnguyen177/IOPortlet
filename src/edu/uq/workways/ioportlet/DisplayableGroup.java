package edu.uq.workways.ioportlet;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonElement;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
/**
 * This is a layout holding a list of Inputable and a submit button
 * @author hoangnguyen
 *
 */
public class DisplayableGroup extends Panel {
	private static final long serialVersionUID = 1L;
	
	/******************************************************/
	private List<Displayable> displayables 	= new LinkedList<Displayable>();
	private AbstractLayout 		layout		= null;
	private Button				submitButton= null;
	private String				groupName	= "";
	/******************************************************/
	public DisplayableGroup(String _groupName){
		super(_groupName);
		groupName = _groupName;
		this.setSizeUndefined();
		layout = new VerticalLayout();
		this.setContent(layout);
		submitButton = new Button("Submit");
		submitButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				for(Displayable _displayable: displayables){
					if(_displayable instanceof Inputable){
						Inputable _inputable = (Inputable)_displayable;
						JsonElement _inputsInGson = _inputable.getUserInput();
						if(_inputsInGson == null)
							continue;
						for(InputListener _listener: _inputable.getInputListeners())
							_listener.onUserInput(_inputsInGson);						
					}
				}
			}
		});
		layout.addComponent(submitButton);
	}
	/**
	 * add displayable
	 * @param _displayable
	 */
	public void addDisplayable(Displayable _displayable){
		displayables.add(_displayable);
		if(_displayable instanceof Inputable){
			Inputable _inputable = (Inputable)_displayable;
			_inputable.addToGroup(groupName);
		}
		_displayable.addToLayout(layout);
	}
	
	/**
	 * addTolayout
	 * @param _layout
	 */
	public void addToLayout(AbstractLayout _layout){
		_layout.addComponent(this);
	}
}
