package edu.uq.workways.ioportlet.parcoords;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
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
@JavaScript({"js/d3/d3.v3.js", "js/d3.parcoords.js","js/ParCoords.js",
	"js/slickgrid/jquery-1.7.min.js", "js/slickgrid/jquery.event.drag-2.0.min.js",
	"js/slickgrid/slick.core.js", "js/slickgrid/slick.grid.js",
	"js/slickgrid/slick.pager.js", "js/slickgrid/slick.dataview.js",
	"js/underscore.js","js/divgrid.js"
	})
@StyleSheet({
	"theme://../parcoords/css/parcoords.css",
	"theme://../slickgrid/css/examples.css",
	"theme://../slickgrid/css/grid.css",
	"theme://../slickgrid/css/jquery-ui-1.8.16.custom.css",
	"theme://../slickgrid/css/slick-default-theme.css",
	"theme://../slickgrid/css/slick.grid.css",	
	"theme://../slickgrid/css/slick.pager.css"
})
public class ParCoords extends AbstractJavaScriptComponent{
	private static final long serialVersionUID = 1L;

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
		this.addFunction("setBrushRange", new JavaScriptFunction() {
	        private static final long serialVersionUID = 1L;

			@Override
	        public void call(JSONArray arguments)
	                throws JSONException {
		    	setBrushedData(arguments);
	        }			
	    });
		
		this.addFunction("onRowSelected", new JavaScriptFunction() {
	        private static final long serialVersionUID = 1L;

			@Override
	        public void call(JSONArray arguments)
	                throws JSONException {
				JSONObject _selectedValue = arguments.getJSONObject(0);
				for(ValueSelectionListener list: listeners){
					list.valueChange(_selectedValue);
				}
	        }			
	    });
	}


	
	public void setDimentions(String[] _dimentions){
		getState().dimensions = _dimentions;
	}
	
	public void setDimensionTitles(String[] _dimensionTitles){
		getState().dimensionTitles = _dimensionTitles;
	}
	
	public void setTypes(Map<String, String> _types){
		getState().types = _types;
	}
	
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
	
	public void addData(Map<String, Object> _dat){
		getState().data.add(_dat);
	}
	
	@Override
	  protected ParCoordsState getState() {
	    return (ParCoordsState) super.getState();
	  }
	
	public JSONArray getBrushedData(){
		return getState().brushedData;
	}
	
	public void setBrushedData(JSONArray _dat){
		getState().brushedData = _dat;
	}
	
	
	public interface ValueSelectionListener extends Serializable {
        void valueChange(JSONObject value);
    }
	
	public void addValueSelectionListener(ValueSelectionListener listener) {
        listeners.add(listener);
    }
	
	public boolean isAutoscale(){
		return this.getState().autoscale;
	}
	
	public void setAutoscale(boolean _autoscale){
		this.getState().autoscale = _autoscale;
	}
	/*******************************************/
	ArrayList<ValueSelectionListener> listeners =
            new ArrayList<ValueSelectionListener>();
   
	
}
