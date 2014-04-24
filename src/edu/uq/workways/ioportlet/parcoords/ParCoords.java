package edu.uq.workways.ioportlet.parcoords;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
/**
 * parallel.coordinates
 * OUTPUT
 * @author hoangnguyen
 *
 */
@JavaScript({"js/d3.v3.js", "js/d3.parcoords.js","js/ParCoords.js"})
@StyleSheet("js/parcoords.css")
public class ParCoords extends AbstractJavaScriptComponent{
	private static final long serialVersionUID = 1L;

	/**
	 * constructors
	 * @param _containerId
	 */
	public ParCoords(String _containerId){
		getState().containerId = _containerId;
		this.setStyleName("parcoords");		
		getState().data = new ArrayList<Map<String, Object>>();
		this.setId(_containerId);
		this.addJavascriptFunctions();
	}
	
	public ParCoords(String _containerId, List<Map<String, Object>> _data){
		getState().containerId = _containerId;
		getState().data = _data;
		this.setStyleName("parcoords");
		this.setId(_containerId);
		this.addJavascriptFunctions();
	}

	/**
	 * add javascript functions called from javascripts
	 */
	private void addJavascriptFunctions(){
		this.addFunction("setBrushValue", new JavaScriptFunction() {
	        private static final long serialVersionUID = 1L;
			@Override
	        public void call(JSONArray arguments) throws JSONException {
	        	setBrushedData(arguments);
	        }			
	    });
	}
	
	/**
	 * setBrushedData
	 * @param _dat
	 */
	public void setBrushedData(JSONArray _dat){
		getState().brushedData = _dat;
	}
	
	/**
	 * getBrushedData
	 * @return
	 */
	public JSONArray getBrushedData(){
		return getState().brushedData;
	}
	
	/**
	 * setDimension
	 * @param _dimentions
	 */
	public void setDimension(String[] _dimentions){
		getState().dimensions = _dimentions;
	}
	
	/**
	 * setDimensionTitles
	 * @param _dimensionTitles
	 */
	public void setDimensionTitles(String[] _dimensionTitles){
		getState().dimensionTitles = _dimensionTitles;
	}
	
	/**
	 * setTypes
	 * @param _types
	 */
	public void setTypes(Map<String, String> _types){
		getState().types = _types;
	}
	
	/**
	 * setMargin
	 * @param _margin
	 */
	public void setMargin(Map<String, Integer> _margin){
		getState().margin = _margin;
	}
    
	/**
	 *  public boolean 						brushed 		= false;
	    public String 						mode 			= "default";
	    public int 							rate			= 20;
	    public int 							width			= 600;
	    public int 							height			= 300;
	    public String 						color			= "#069";
	    public String 						composite		= "source-over";
	    public double 						alpha			= 0.7;
	 */
	public void setProperties(String prop, Object value){
	   if (prop.equalsIgnoreCase("brushed")) {
	    getState().brushed=(Boolean)value;
	   } else if (prop.equalsIgnoreCase("mode")) {
	    getState().mode=(String)value;
	   } else if (prop.equalsIgnoreCase("rate")) {
	    getState().rate=(Integer)value;
	   } else if (prop.equalsIgnoreCase("pcwidth")) {
		getState().pcWidth=(Integer)value;
	   } else if (prop.equalsIgnoreCase("pcheight")) {
	    getState().pcHeight=(Integer)value;
	   } else if (prop.equalsIgnoreCase("color")) {
	    getState().color=(String)value;
	   } else if (prop.equalsIgnoreCase("composite")) {
		    getState().composite=(String)value;
	   } else if (prop.equalsIgnoreCase("alpha")) {
		    getState().alpha=(Double)value;
	   }
	   
	}
	
	/**
	 * add data
	 * @param _dat
	 */
	public void addData(Map<String, Object> _dat){
		getState().data.add(_dat);
	}
	
	/**
	 * get state
	 */
	@Override
	protected ParCoordsState getState() {
		return (ParCoordsState) super.getState();
	}
	
}
