package edu.uq.workways.ioportlet;
//java
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
//io library
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;

/**
 * gui.textarea
 * OUTPUT
 * @author hoangnguyen
 *
 */
public class TextArea extends DisplayObject{
	/**
	 * constructor(s)
	 */
	public TextArea(){
	}
	
	public TextArea(String _id){
		this.setId(_id);
	}
	
	@Override
	public int getNumberOfSeries() {
		return this.getDataSeriesIds().size();
	}

	@Override
	public void addData(String _data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		if(data.containsKey(serieId)){
			if(updateMode == UpdateMode.OVERWRITE){
				List<String> _newDataList = new LinkedList<String>();
				_newDataList.add(_data);
				data.get(serieId).clear();
				data.get(serieId).addAll(_newDataList);
			}
			else if(updateMode == UpdateMode.APPEND){
				data.get(serieId).add(_data);
			}
		}
		else{
			seriesOrder.add(serieId);
			List<String> _newDataList = new LinkedList<String>();
			_newDataList.add(_data);
			data.put(serieId, _newDataList);
		}//end else		
		if(update)
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
				stringToDisplay += "[" + serieId + "]" + _dat + "\n";
			}
		}//end for
		((com.vaadin.ui.TextArea)component).setValue(stringToDisplay);
	}

	@Override
	public Set<String> getDataSeriesIds() {
		return data.keySet();
	}
	
	@Override
	public void createDisplayObject() {
		component = new com.vaadin.ui.TextArea();
	}
	
	/*************************************************************/
	private Map<String, List<String>>			data	= new HashMap<String, List<String>>();
	private List<String>					seriesOrder	= new LinkedList<String>();
	
	
}
