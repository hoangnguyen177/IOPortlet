package edu.uq.workways.ioporlet.parcoords;

//java
import java.util.List;
import java.util.Map;



//vaadin
import com.vaadin.shared.ui.JavaScriptComponentState;

public class ParCoordsState extends JavaScriptComponentState{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**************************************************************/
	public List<Map<String, Object>> 				data			= null;
    public String[]						dimensions	 	= null;
    public String[] 					dimensionTitles = {};
    public Map<String, String>			types 			= null;
    public boolean 						brushed 		= true;
    public String 						mode 			= "default";
    public int 							rate			= 20;
    public Map<String, Integer> 		margin 			= null; //top - right - bottom - left
    public String 						color			= "#069";
    public String 						composite		= "source-over";
    public double 						alpha			= 0.7;
    public int							pcWidth			= 800;
    public int							pcHeight		= 400;
    
    public String						containerId		="";
    
}
