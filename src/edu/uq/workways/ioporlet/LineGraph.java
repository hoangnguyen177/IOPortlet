package edu.uq.workways.ioporlet;

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
import com.vaadin.ui.AbstractLayout;

//io stuff
import edu.monash.io.iolibrary.ConfigurationConsts;
import edu.monash.io.iolibrary.ConfigurationConsts.DataType;
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.monash.io.iolibrary.exceptions.InvalidDataTypeException;





//java
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/**
 * Line graph
 * can accept 9 series at a time, more than that, the new serie is not going to be dislayed
 * TODO: make it another level: the chart can be AbstractComponent
 * @author hoangnguyen
 *
 */
public class LineGraph implements Outputable{

	/**
	 * constructor: create a normal LineGraph
	 */
	public LineGraph(){
		chart = new DCharts();
		chart.show();
	}
	
	
	@Override
	public void setOutputDataType(String out_datatype) {
		try {
			outputDataType = DataType.fromString(out_datatype);
		} catch (InvalidDataTypeException e) {}
	}

	@Override
	public void setUpdateMode(String update_mode) {
		try {
			updateMode = UpdateMode.fromString(update_mode);
		} catch (InvalidDataTypeException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void addData(String _data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		if(data.containsKey(serieId)){
			if(updateMode == UpdateMode.OVERWRITE){
				List<String> _newDataList = new ArrayList<String>();
				_newDataList.add(_data);
				data.get(serieId).clear();
				data.get(serieId).addAll(_newDataList);
			}
			else if(updateMode == UpdateMode.APPEND){
				data.get(serieId).add(_data);
			}
		}
		else{
			//all the styles
			MarkerStyles[] styles = MarkerStyles.values();
			//enough series, no more data
			if(this.getNumberOfSeries() >= styles.length)
				throw new UpperLimitNumberOfSeriesException("Can only accept:" + styles.length + " number of series. ignore now.");
			seriesOrder.add(serieId);
			
			List<String> _newDataList = new ArrayList<String>();
			_newDataList.add(_data);
			data.put(serieId, _newDataList);
		}//end else		
		if(update)
			updateGraph();
	}
	
	
	@Override
	public int getNumberOfSeries() {
		return this.getDataSeriesIds().size();
	}


	@Override
	public String getOutputDataType() {
		return outputDataType.toString();
	}


	@Override
	public String getUpdateMode() {
		return updateMode.toString();
	}


	@Override
	public Set<String> getDataSeriesIds() {
		return data.keySet();
	}
	
	/**
	 * update graph
	 * @throws Exception 
	 */
	private void updateGraph() throws InvalidDataException{
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
		chart.setOptions(options).setDataSeries(dataSeries).markAsDirty();
		chart.show();
	}
	
	
	
	@Override
	public void addToLayout(AbstractLayout _layout) {
		_layout.addComponent(chart);
	}
	
	
	@Override
	public void setId(String _id) {
		id = _id;
	}


	@Override
	public String getId() {
		return id;
	}


	@Override
	public void setCaption(String _caption) {
		caption = _caption;
		chart.setCaption(caption);
	}


	@Override
	public String getCaption() {
		return caption;
	}


	@Override
	public void setGuiType(String _guiElement) {
		guiType = _guiElement;
	}


	@Override
	public boolean isEqual(String _otherId, String _otherGuiType,
			String _outputDataType, String _updateMode) {
		return id.equals(_otherId)&& guiType.equals(_otherGuiType)&& 
				outputDataType.toString().equals(_outputDataType)&& updateMode.toString().equals(_updateMode);
	}


	@Override
	public String getGuiType() {
		return guiType;
	}
	
	
	/******************************************************/
	//dchart stuff
	private DCharts 		chart 			= null;
	//private variables
	private UpdateMode 					updateMode 		= UpdateMode.OVERWRITE;
	private DataType					outputDataType	= DataType.STRING;
	private Map<String, List<String>>			data	= new HashMap<String, List<String>>();
	private List<String>					seriesOrder	= new ArrayList<String>(MarkerStyles.values().length);
	private String 							id			="";
	private String							caption		="";
	private String							guiType		="";
	
	
	
	
	
	
	
}
