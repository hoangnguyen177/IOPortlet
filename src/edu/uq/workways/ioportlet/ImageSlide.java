package edu.uq.workways.ioportlet;
//java
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.io.File;
import java.io.FileOutputStream;

//file resource
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.VerticalLayout;
//workway utils
import edu.uq.workways.commons.utils.Base64;
//addon
import org.vaadin.tepi.imageviewer.ImageViewer;

//similar to image, but display several images at once
public class ImageSlide extends DisplayObject{
	private List<Resource> resourceList = new ArrayList<Resource>();
	private ImageViewer		imageViewer	= null;
	/**
	 * constructor(s)
	 */
	public ImageSlide(){
		component = new VerticalLayout();
		component.setSizeFull();
	}
	
	public ImageSlide(String _id){
		this.setId(_id);
		component = new VerticalLayout();
		component.setSizeFull();
	}
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	@Override
	public void addData(String data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		//decode the data
		byte[] imgContents = Base64.decode(data);
		//now write it
		try{
			File newFile = new File("/tmp/" + UUID.randomUUID());
			FileOutputStream fos = new FileOutputStream(newFile);
			fos.write(imgContents);
			fos.close();
			//update viewer
			resourceList.add(new FileResource(newFile));
			if(resourceList.isEmpty())
				return;
			if(resourceList.size()==1){
				imageViewer = new ImageViewer(resourceList);
				imageViewer.setImmediate(true);
				imageViewer.setWidth(800, Unit.PIXELS);
				imageViewer.setHeight(600, Unit.PIXELS);
				imageViewer.setAnimationEnabled(true);
				((VerticalLayout)component).addComponent(imageViewer);
			}
			else
				imageViewer.setImages(resourceList);
		}
		catch(Exception e){}
	}

	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void update() throws InvalidDataException {
		
	}

}
