package edu.uq.workways.ioporlet;
//java
import java.util.LinkedList;
import java.util.List;





//org json
import org.json.JSONArray;


//gson
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

//vaadin

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import edu.uq.workways.ioporlet.parcoords.ParCoords;


/**
 * parallel.coordinates
 * INPUT
 * @author hoangnguyen
 *
 */
public class ParallelCoordinate_Inputable extends ParallelCoordinate implements Inputable{
	/*************************************************************/
	private List<InputListener> listeners = new LinkedList<InputListener>();
	/*************************************************************/
	public ParallelCoordinate_Inputable(String _id) {
		super(_id);
	}

	/**
	 * add to layout
	 * @param layout
	 */
	@Override
	public void addToLayout(AbstractLayout layout){
		super.addToLayout(layout);
		Button submitButton = new Button("Submit");
		layout.addComponent(submitButton);
		submitButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				if(component ==null || ((ParCoords)component).getBrushedData()==null || ((ParCoords)component).getBrushedData().length()==0)
					return;
				JSONArray _inputs = ((ParCoords)component).getBrushedData();
				JsonParser parser = new JsonParser();
				JsonElement _inputsInGson = parser.parse(_inputs.toString());
				for(InputListener _listener: listeners){
					_listener.onUserInput(_inputsInGson);
				}
			}			
		});
	}
	
	@Override
	public void addInputListener(InputListener _listener) {
		listeners.add(_listener);
	}
	
	

}
