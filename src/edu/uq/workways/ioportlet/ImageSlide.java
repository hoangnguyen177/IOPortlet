package edu.uq.workways.ioportlet;
//java
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.io.File;
import java.io.FileOutputStream;


import com.google.gson.JsonObject;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
//file resource
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.VerticalLayout;

//workway utils
import edu.uq.workways.commons.utils.Base64;
import edu.uq.workways.ioportlet.IoportletUI.MessageType;
//addon
import org.vaadin.tepi.imageviewer.ImageViewer;

//similar to image, but display several images at once
public class ImageSlide extends DisplayObject{
	private List<Resource> resourceList = new ArrayList<Resource>();
	private ImageViewer		imageViewer	= null;
	/**
	 * constructor
	 */
	public ImageSlide(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		this.setId(_id);
		this.setUserName(uname);
		this.setMessageContainer(msgContainer);
		this.setSourceSinkId(sourceSinkContainer);
	}
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	
	@Override
	public void addData(JsonObject message)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		boolean _append = message.get("append").getAsBoolean();
		String _path = message.get("path").getAsString();
		boolean isRecordedMessage = false;
		if(message.has("recorded"))
			isRecordedMessage = message.get("recorded").getAsBoolean();
		File newFile = null;
		if(isRecordedMessage){
			if(!message.has("filepath"))
				return;
			newFile = new File(message.get("filepath").getAsString());
		}
		else{
			String _data = message.get("data").getAsString();
			byte[] imgContents = Base64.decode(_data);
			//now write it
			try{
				newFile = new File(this.getStorePath() + "/" + UUID.randomUUID());
				FileOutputStream fos = new FileOutputStream(newFile);
				fos.write(imgContents);
				fos.close();
			}
			catch(Exception e){}
		}
		if(newFile!=null)
			resourceList.add(new FileResource(newFile));
		if(!isRecordedMessage){
			Long _timeStampLong = message.get("timestamp").getAsLong();
			Timestamp _timeStamp = new Timestamp(_timeStampLong);
			Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
			try {
				message.remove("data");
				this.saveMessage(message.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, newFile.getAbsolutePath());
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		if(!_append)
			update();
	}


	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void update() throws InvalidDataException {
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

	@Override
	public void createDisplayObject() {
		component = new VerticalLayout();
		component.setSizeFull();
	}

}
