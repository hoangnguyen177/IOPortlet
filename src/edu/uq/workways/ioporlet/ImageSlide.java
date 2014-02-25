package edu.uq.workways.ioporlet;
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

//workway utils
import edu.uq.workways.commons.utils.Base64;

//addon
import org.vaadin.tepi.imageviewer.ImageViewer;

//similar to image, but display several images at once
public class ImageSlide extends DisplayObject{
	/**
	 * constructor(s)
	 */
	public ImageSlide(){
		component = new ImageViewer();
	}
	
	public ImageSlide(String _id){
		this.setId(_id);
		component = new ImageViewer();
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
			ImageViewer _imgViewer = (ImageViewer)component;
			List<Resource> newList = new ArrayList<Resource>();
			List<? extends Resource> images = _imgViewer.images;
			newList.addAll(images);
			newList.add(new FileResource(newFile));
			_imgViewer.setImages(newList);			
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
