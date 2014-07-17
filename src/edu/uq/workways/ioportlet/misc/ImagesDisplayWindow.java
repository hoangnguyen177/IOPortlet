package edu.uq.workways.ioportlet.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.tepi.imageviewer.ImageViewer;

import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
public class ImagesDisplayWindow extends Window {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*******************************************************/
	private Layout mainLayout  	   = null;
	private int 		windowId	= -1;
	private ImageViewer		imageViewer	= null;
	/*******************************************************/
	public ImagesDisplayWindow(int _windowId, List<String> fileList){
		super();
		mainLayout = new VerticalLayout();
		this.setWindowId(_windowId);
		this.setContent(mainLayout);
		if(fileList!= null && fileList.size()>0){
			imageViewer = new ImageViewer(getResourceList(fileList));
			imageViewer.setImmediate(true);
			imageViewer.setWidth(800, Unit.PIXELS);
			imageViewer.setHeight(600, Unit.PIXELS);
			imageViewer.setAnimationEnabled(true);
			mainLayout.addComponent(imageViewer);	
		}
	}
	
	public void setWindowId(int _id){
		windowId = _id;
	}
	
	public int getWindowId(){
		return windowId;
	}
	
	private List<Resource> getResourceList(List<String> _imagesPaths){
		List<Resource> _imageResources = new ArrayList<Resource>();
		for(String _imagesPath: _imagesPaths){
			_imageResources.add(new FileResource(new File(_imagesPath)));
		}
		return _imageResources;
	}
	
}
