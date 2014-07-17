package edu.uq.workways.ioportlet;
//java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.Sizeable.Unit;
//vaadin
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;

import edu.uq.workways.commons.utils.Base64;
import edu.uq.workways.ioportlet.IoportletUI.MessageType;
import edu.uq.workways.ioportlet.misc.ImagesDisplayWindow;
//pc
import edu.uq.workways.ioportlet.parcoords.ParCoords;


public class ParallelCoordinate  extends DisplayObject{
	
	
	private int 							counter 		= 1;	/*for id stuff*/
	private Map<Integer, List<String>>			idPathMap		= null;
	private Map<Integer, ImagesDisplayWindow>	idWindowMap		= null;
	/************************************************************/
	/**
	 * constructor
	 * @param _id
	 */
	public ParallelCoordinate(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		this.setId(_id);
		this.setUserName(uname);
		this.setMessageContainer(msgContainer);
		this.setSourceSinkId(sourceSinkContainer);
		idPathMap = new HashMap<Integer, List<String>>();
		idWindowMap = new HashMap<Integer, ImagesDisplayWindow>();
	}
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void addToLayout(AbstractLayout layout) {
		CheckBox _autoscale = new CheckBox("Autoscale");
		_autoscale.setValue(((ParCoords)component).isAutoscale());
		_autoscale.addValueChangeListener(new ValueChangeListener() {
		   @Override
			public void valueChange(
					com.vaadin.data.Property.ValueChangeEvent event) {
			  boolean value = (Boolean)event.getProperty().getValue();
			  ((ParCoords)component).setAutoscale(value);
			}
		});
		layout.addComponent(_autoscale);
		layout.addComponent(component);
	}
	
	@Override
	public void addData(JsonObject message) throws UpperLimitNumberOfSeriesException, InvalidDataException{
		boolean _append = message.get("append").getAsBoolean();
		String _path = message.get("path").getAsString();
		boolean isRecordedMessage = false;
		if(message.has("recorded"))
			isRecordedMessage = message.get("recorded").getAsBoolean();
		JsonObject _data = null;
		if(message.get("data").isJsonObject())
			_data = message.get("data").getAsJsonObject();
		else if(message.get("data").isJsonPrimitive()) //string
		{
			JsonParser parser = new JsonParser();
			_data = parser.parse(message.get("data").getAsString().trim()).getAsJsonObject();
		}
		Map<String, Object> _addedData = new HashMap<String, Object>();
		Set<Map.Entry<String,JsonElement>> entries = _data.entrySet();
		JsonObject _dataToBeStored = new JsonObject();
		//put id
		int _id = counter++;
		_addedData.put("id", _id);
		for(Map.Entry<String, JsonElement> _item: entries)
		{
			String _key = _item.getKey();
			JsonElement _value = _item.getValue();
			if(_value.isJsonPrimitive()){
				JsonPrimitive _primitiveValue = _value.getAsJsonPrimitive();
				if(_primitiveValue.isBoolean())
					_addedData.put(_key, _primitiveValue.getAsBoolean());
				else if(_primitiveValue.isString())
					_addedData.put(_key, _primitiveValue.getAsString());
				else if(_primitiveValue.isNumber())
					_addedData.put(_key, _primitiveValue.getAsDouble());//DOUBLE at the moment	
				_dataToBeStored.add(_key, _value);
			}
			//note that complicated objects are not get added to the par coords, since they are just showing primitives anyway
			else if(_value.isJsonObject())
			{
				//image1={type:image, data:asdasdasd, extension:jpeg}
				JsonObject _valueObject = _value.getAsJsonObject();
				//image
				if( _valueObject.has("type") &&  "image".equals(_valueObject.get("type").getAsString())){
					//recoreded messages
					String _filePath = "";
					if(isRecordedMessage){
						if(_valueObject.has("path") && !_valueObject.get("path").getAsString().trim().isEmpty()){
							_filePath = _valueObject.get("path").getAsString().trim();
						}
					}
					else{
						if(_valueObject.get("data").getAsString() == null || _valueObject.get("data").getAsString().isEmpty())
							continue;
						byte[] imgContents = Base64.decode(_valueObject.get("data").getAsString().trim());
						if(_valueObject.has("extension") && !_valueObject.get("extension").getAsString().isEmpty())
							_filePath = this.getStorePath() + "/" + UUID.randomUUID() + "." + _valueObject.get("extension").getAsString();
						else
							_filePath = this.getStorePath() + "/" + UUID.randomUUID();
							
						File newFile = new File(_filePath);
						boolean _writeSuccess = false;
						try {
							FileOutputStream fos = new FileOutputStream(newFile);
							fos.write(imgContents);
							fos.close();
							_writeSuccess = true;
						} catch (FileNotFoundException e) {
							System.out.println("Smth wrong writing:" + _filePath + ":::" + e.getMessage());
							_filePath = "";
						} catch (IOException e) {
							System.out.println("Smth wrong writing:" + _filePath+ ":::" + e.getMessage());
							_filePath = "";
						}catch(Exception e){
							System.out.println("Smth wrong writing:" + _filePath+ ":::" + e.getMessage());
							_filePath = "";
						}
						JsonObject _obj = new JsonObject();
						if(_writeSuccess)
							_obj.addProperty("path", newFile.getPath());
						Set<Map.Entry<String, JsonElement>> _entrySet = _valueObject.entrySet();
						for(Map.Entry<String, JsonElement> _entry: _entrySet){
							if(!_entry.getKey().equals("data")){
								_obj.add(_entry.getKey(), _entry.getValue());
							}
						}
						//not sure whether _data overwrites so remove before adding back
						_dataToBeStored.add(_key, _obj);
					}
					if(_filePath!=null && !_filePath.isEmpty()){
						if(idPathMap.containsKey(_id)){
							idPathMap.get(_id).add(_filePath);
						}
						else{
							List<String> _newList = new LinkedList<String>();
							_newList.add(_filePath);
							idPathMap.put(_id, _newList);
						}						
					}
				}
				else
					Notification.show("Parallel Cooridantes only support image at the moment", Type.ERROR_MESSAGE);
				
			}
			else
				System.out.println("ParCoord does not accept other data yet");
		}
		if(!isRecordedMessage){
			//now save
			JsonObject _messageToStore = new JsonObject();
			Set<Map.Entry<String, JsonElement>> _messageEntries =  message.entrySet();
			for(Map.Entry<String, JsonElement> _messageEntry: _messageEntries){
				if(_messageEntry.getKey()!="data"){
					_messageToStore.add(_messageEntry.getKey(), _messageEntry.getValue());
				}
			}
			_messageToStore.add("data", _dataToBeStored);
			Long _timeStampLong = message.get("timestamp").getAsLong();
			Timestamp _timeStamp = new Timestamp(_timeStampLong);
			Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
			try {
				this.saveMessage(_messageToStore.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, "");
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}	
		}
		((ParCoords)component).addData(_addedData);
	}
	
	
	
	//not needed, update ParCoords in every addition
	@Override
	public void update() throws InvalidDataException{
		
	}

	@Override
	public void createDisplayObject() {
		component = new ParCoords(this.id);
		((ParCoords)component).setProperties("pcwidth", 800);
		((ParCoords)component).setProperties("pcheight", 500);
		((ParCoords)component).setProperties("brushed", true);
		((ParCoords)component).setWidth("800px");
		((ParCoords)component).setHeight("600px");
		((ParCoords)component).addValueSelectionListener(new ParCoords.ValueSelectionListener() {
			@Override
			public void valueChange(JSONObject value) {
				try {
					final int _id = value.getInt("id");
					if(idWindowMap.containsKey(_id)){
						idWindowMap.get(_id).focus();//get the focus
					}
					else{
						ImagesDisplayWindow _imagesWindow = new ImagesDisplayWindow(_id, idPathMap.get(_id));
						_imagesWindow.addCloseListener(new Window.CloseListener() {
							@Override
							public void windowClose(CloseEvent e) {
								idWindowMap.remove(_id);
							}
						});
						getParentUI().addWindow(_imagesWindow);
						idWindowMap.put(_id, _imagesWindow);
						_imagesWindow.focus();
						
					}

				} catch (JSONException e) {}
				
			}
		});
	}
	
	
	
	
}
