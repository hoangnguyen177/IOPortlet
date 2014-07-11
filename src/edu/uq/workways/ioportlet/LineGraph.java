package edu.uq.workways.ioportlet;

//dchart
import org.dussan.vaadin.dcharts.DCharts;
import org.dussan.vaadin.dcharts.base.elements.XYseries;
import org.dussan.vaadin.dcharts.base.renderers.MarkerRenderer;
import org.dussan.vaadin.dcharts.data.DataSeries;
import org.dussan.vaadin.dcharts.metadata.TooltipAxes;
import org.dussan.vaadin.dcharts.metadata.locations.TooltipLocations;
import org.dussan.vaadin.dcharts.metadata.styles.MarkerStyles;
import org.dussan.vaadin.dcharts.options.Highlighter;
import org.dussan.vaadin.dcharts.options.Options;
import org.dussan.vaadin.dcharts.options.Series;

import com.google.gson.JsonObject;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.ui.AbstractLayout;






//io stuff
import edu.monash.io.iolibrary.ConfigurationConsts;
import edu.monash.io.iolibrary.ConfigurationConsts.DataType;
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.monash.io.iolibrary.exceptions.InvalidDataTypeException;









import edu.uq.workways.ioportlet.IoportletUI.MessageType;

import java.sql.SQLException;
import java.sql.Timestamp;
//java
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/**
 * graph.line
 * Line graph
 * can accept 9 series at a time, more than that, the new serie is not going to be dislayed
 * @author hoangnguyen
 *
 */
public class LineGraph extends DisplayObject{

	/**
	 * constructor: create a normal LineGraph
	 */	
	public LineGraph(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		this.setId(_id);
		this.setUserName(uname);
		this.setMessageContainer(msgContainer);
		this.setSourceSinkId(sourceSinkContainer);
	}
	
	@Override
	public void addData(JsonObject message)	throws UpperLimitNumberOfSeriesException, InvalidDataException {
		boolean _append = message.get("append").getAsBoolean();
		String _path = message.get("path").getAsString();//act as SerieID
		boolean isRecordedMessage = false;
		if(message.has("recorded"))
			isRecordedMessage = message.get("recorded").getAsBoolean();
		String _data = message.get("data").getAsString();
		if(data.containsKey(_path)){
			if(updateMode == UpdateMode.OVERWRITE){
				List<String> _newDataList = new ArrayList<String>();
				_newDataList.add(_data);
				data.get(_path).clear();
				data.get(_path).addAll(_newDataList);
			}
			else if(updateMode == UpdateMode.APPEND)
				data.get(_path).add(_data);
		}
		else{
			//all the styles
			MarkerStyles[] styles = MarkerStyles.values();
			//enough series, no more data
			if(this.getNumberOfSeries() >= styles.length)
				throw new UpperLimitNumberOfSeriesException("Can only accept:" + styles.length + " number of series. ignore now.");
			seriesOrder.add(_path);
			
			List<String> _newDataList = new ArrayList<String>();
			_newDataList.add(_data);
			data.put(_path, _newDataList);
		}
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
	 * update graph
	 * @throws Exception 
	 */
	public void update() throws InvalidDataException{
		DataSeries dataSeries = new DataSeries();
		Series series = new Series();
		MarkerStyles[] styles = MarkerStyles.values();
		for(int i=0; i< this.getNumberOfSeries(); i++){
			dataSeries.newSeries();
			String serieId = seriesOrder.get(i);
			if(!data.containsKey(serieId))
				throw new InvalidDataException(serieId + " is not in the data. Invalid");
			List<String> _data = data.get(serieId);
			//now concatenate _data and put it in the series
			Iterator<String> _dataIterator = _data.iterator();
			while(_dataIterator.hasNext()){
				String _dat = _dataIterator.next().trim();
				String[] _line = _dat.split(",");
				for(int _count = 0; _count< _line.length; _count++){
					String _aLine = _line[_count].trim();
					if(_aLine.isEmpty())
						continue;
					String[] _values = _aLine.split("\\s+");
					if(_values.length<2)
						throw new InvalidDataException("Invalid data");
					Double _firstVal = Double.parseDouble(_values[0].trim());
					Double _secondVal = Double.parseDouble(_values[1].trim());
					dataSeries.add(_firstVal, _secondVal);					
				}				
			}
			series.addSeries(new XYseries().setLabel(serieId)
							.setShowLine(false)
							.setShowLabel(true)	
							.setLineWidth(i+1)
							.setMarkerOptions(
								new MarkerRenderer()
									.setStyle(styles[i]))
					);
		}//end for
		Options options = new Options().setSeries(series);
		((DCharts)component).setOptions(options).setDataSeries(dataSeries).markAsDirty();
		((DCharts)component).show();
	}
	
	
	@Override
	public int getNumberOfSeries() {
		return this.getDataSeriesIds().size();
	}


	@Override
	public Set<String> getDataSeriesIds() {
		return data.keySet();
	}
	
	@Override
	public void createDisplayObject() {
		component = new DCharts();
		((DCharts)component).show();
	}
	
	/******************************************************/
	//private variables
	private Map<String, List<String>>			data	= new HashMap<String, List<String>>();
	private List<String>					seriesOrder	= new ArrayList<String>(MarkerStyles.values().length);
	
	
	
	
}
