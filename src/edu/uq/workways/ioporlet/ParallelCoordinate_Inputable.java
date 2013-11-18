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
	private boolean belongToGroup = false;
	private String groupName = "";
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
		if(!belongToGroup){
			Button submitButton = new Button("Submit");
			layout.addComponent(submitButton);
			submitButton.addClickListener(new Button.ClickListener(){
				@Override
				public void buttonClick(ClickEvent event) {
					JsonElement _inputsInGson = getUserInput();
					if(_inputsInGson==null)
						return;
					for(InputListener _listener: listeners){
						_listener.onUserInput(_inputsInGson);
					}
				}			
			});			
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
		if(component ==null || ((ParCoords)component).getBrushedData()==null || ((ParCoords)component).getBrushedData().length()==0)
			return null;
		JSONArray _inputs = ((ParCoords)component).getBrushedData();
		JsonParser parser = new JsonParser();
		return parser.parse(_inputs.toString());
	}
	
	

}
