package edu.uq.workways.ioporlet.parcoords;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
@JavaScript({"js/d3.v3.js", "js/d3.parcoords.js","js/ParCoords.js"})
@StyleSheet("js/parcoords.css")
public class ParCoords extends AbstractJavaScriptComponent{
	private static final long serialVersionUID = 1L;

	public ParCoords(String _containerId){
		getState().containerId = _containerId;
		this.setStyleName("parcoords");		
		getState().data = new ArrayList<Map<String, Object>>();
		this.setId(_containerId);
	}
	
	public ParCoords(String _containerId, List<Map<String, Object>> _data){
		getState().containerId = _containerId;
		getState().data = _data;
		this.setStyleName("parcoords");
		this.setId(_containerId);
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
	
}
