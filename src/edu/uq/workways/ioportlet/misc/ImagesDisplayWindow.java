package edu.uq.workways.ioportlet.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.tepi.imageviewer.ImageViewer;

import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Image;
public class ImagesDisplayWindow extends Window {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*******************************************************/
	private List<String> filePaths = null;
	private Layout mainLayout  	   = null;
	private int 		windowId	= -1;
	private ImageViewer		imageViewer	= null;
	private List<Resource> resourceList = null;
	/*******************************************************/
	public ImagesDisplayWindow(int _windowId, List<String> _fileList){
		super();
		filePaths = _fileList;
		mainLayout = new HorizontalLayout();
		resourceList = new ArrayList<Resource>();
		for(String _file: _fileList){
			this.addImage(_file);
			resourceList.add(new FileResource(new File(_file)));
		}
		this.setContent(mainLayout);
		this.setWindowId(_windowId);
		imageViewer = new ImageViewer();
		imageViewer.setWidth(800, Unit.PIXELS);
		imageViewer.setHeight(600, Unit.PIXELS);
		imageViewer.setAnimationEnabled(true);
		mainLayout.addComponent(imageViewer);
	}
	
	public void addImage(String _path){
		if(!filePaths.contains(_path)){
			filePaths.add(_path);
			resourceList.add(new FileResource(new File(_path)));			
		}
	}
	
	public void setWindowId(int _id){
		windowId = _id;
	}
	
	public int getWindowId(){
		return windowId;
	}
	
}
