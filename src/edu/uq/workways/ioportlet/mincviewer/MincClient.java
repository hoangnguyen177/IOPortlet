package edu.uq.workways.ioportlet.mincviewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
@JavaScript({
	"js/jquery/jquery-1.7.2.min.js",
	"js/lib/jquery-ui-1.8.10.custom.min.js",
	"js/ui/common.js",
	"js/brainbrowser/brainbrowser.volume-viewer.min.js",
	"js/brainbrowser.config.js",
	"js/MincClient.js"
	})

@StyleSheet({
	"theme://../../mincviewer/css/common.css",  
	"theme://../../mincviewer/css/ui-darkness/jquery-ui-1.8.10.custom.css",
})
public class MincClient extends AbstractJavaScriptComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public interface ValueChangeListener extends Serializable {
        void valueChange();
    }
	
	
	public void addValueChangeListener(ValueChangeListener listener) {
        listeners.add(listener);
    }
	
	
	public MincClient(){
		this.setStyleName("MincClient");
	}
	
	public MincClient(JSONArray _volumeList){
		this.setStyleName("MincClient");
		this.getState().volumeList = _volumeList;
	}
	

	@Override
	protected MincClientState getState() {
		return (MincClientState) super.getState();
	}
	
	public void setVolumeList(JSONArray _volumeList){
		this.getState().volumeList = _volumeList;
	}
	
	/*******************************************/
	ArrayList<ValueChangeListener> listeners =
            new ArrayList<ValueChangeListener>();
	
}
