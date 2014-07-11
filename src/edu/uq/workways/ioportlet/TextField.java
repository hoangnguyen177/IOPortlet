package edu.uq.workways.ioportlet;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.vaadin.data.util.sqlcontainer.SQLContainer;

import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.uq.workways.ioportlet.IoportletUI.MessageType;

/**
 * gui.textfield
 * OUTPUT
 * @author hoangnguyen
 *
 */
public class TextField extends DisplayObject{

	public TextField(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		this.setId(_id);
		this.setUserName(uname);
		this.setMessageContainer(msgContainer);
		this.setSourceSinkId(sourceSinkContainer);
	}
	
	@Override
	public int getNumberOfSeries() {
		return this.getDataSeriesIds().size();
	}

	@Override
	public void addData(JsonObject message)	throws UpperLimitNumberOfSeriesException, InvalidDataException {
		boolean _append = message.get("append").getAsBoolean();
		String _path = message.get("path").getAsString();
		String _data = message.get("data").getAsString();
		boolean isRecordedMessage = false;
		if(message.has("recorded"))
			isRecordedMessage = message.get("recorded").getAsBoolean();
		if(data.containsKey(_path)){
			if(updateMode == UpdateMode.OVERWRITE){
				List<String> _newDataList = new LinkedList<String>();
				_newDataList.add(_data);
				data.get(_path).clear();
				data.get(_path).addAll(_newDataList);
			}
			else if(updateMode == UpdateMode.APPEND){
				data.get(_path).add(_data);
			}
		}
		else{
			seriesOrder.add(_path);
			List<String> _newDataList = new LinkedList<String>();
			_newDataList.add(_data);
			data.put(_path, _newDataList);
		}//end else		
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
		if(!_append)
			update();
	}
	
	
	/**
	 * bring the update to the users
	 */
	public void update() throws InvalidDataException{
		String stringToDisplay = "";
		for(int i=0; i< this.getNumberOfSeries(); i++){
			String serieId = seriesOrder.get(i);
			if(!data.containsKey(serieId))
				throw new InvalidDataException(serieId + " is not in the data. Invalid");
			List<String> _data = data.get(serieId);
			//now concatenate _data and put it in the series
			Iterator<String> _dataIterator = _data.iterator();
			while(_dataIterator.hasNext()){
				String _dat = _dataIterator.next();
				stringToDisplay += _dat + "\t";
			}
		}//end for
		((com.vaadin.ui.TextField)component).setValue(stringToDisplay);
	}

	@Override
	public Set<String> getDataSeriesIds() {
		return data.keySet();
	}

	@Override
	public void createDisplayObject() {
		component = new com.vaadin.ui.TextField();
	}

	
	/*************************************************************/
	private Map<String, List<String>>			data	= new HashMap<String, List<String>>();
	private List<String>					seriesOrder	= new LinkedList<String>();

}
