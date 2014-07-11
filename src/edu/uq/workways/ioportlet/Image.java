package edu.uq.workways.ioportlet;
//java
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
//vaadin
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;

import org.vaadin.tepi.imageviewer.ImageViewer;


//workway utils
import edu.uq.workways.commons.utils.Base64;
import edu.uq.workways.ioportlet.IoportletUI.MessageType;
import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
/**
 * This class is to display images sent from the clients
 * If append then display in tiled form
 * If overwrite then display in single form
 * @author hoangnguyen
 *
 */
public class Image extends DisplayObject{
	/******************************************************/
	private List<Resource> resourceList = new ArrayList<Resource>();
	/*******************************************************/
	/**
	 * constructor(s)
	 */
	public Image(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		this.setId(_id);
		this.setUserName(uname);
		this.setMessageContainer(msgContainer);
		this.setSourceSinkId(sourceSinkContainer);
	}
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}
	
	public void createDisplayObject(){
		if(this.updateMode == UpdateMode.OVERWRITE){
			component = new com.vaadin.ui.Image();
		}
		else if(this.updateMode == UpdateMode.APPEND){
			component = new ImageViewer();
			((ImageViewer)component).setImmediate(true);
			((ImageViewer)component).setWidth(800, Unit.PIXELS);
			((ImageViewer)component).setHeight(600, Unit.PIXELS);
			((ImageViewer)component).setAnimationEnabled(true);
		}
	}

	
	/**
	 * addData
	 */
	@Override
	public void addData(JsonObject message)	throws UpperLimitNumberOfSeriesException, InvalidDataException {
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
			message.remove("data");
			String _data = message.get("data").getAsString();
			byte[] imgContents = Base64.decode(_data);
			try{
				newFile = new File(this.getStorePath() + "/" + UUID.randomUUID());
				FileOutputStream fos = new FileOutputStream(newFile);
				fos.write(imgContents);
				fos.close();
			}
			catch(Exception e){}
		}
		if(newFile==null)
			return;
		if(this.updateMode == UpdateMode.OVERWRITE){
			resourceList.clear();
			resourceList.add(new FileResource(newFile));
		}
		else if(this.updateMode == UpdateMode.APPEND){
			resourceList.add(new FileResource(newFile));
		}
		//now save the message into the database
		if(!isRecordedMessage){
			Long _timeStampLong = message.get("timestamp").getAsLong();
			Timestamp _timeStamp = new Timestamp(_timeStampLong);
			Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
			try {
				this.saveMessage("", MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, newFile.getAbsolutePath());
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		//update or not
		if(!_append)
			update();
		
	}
		

	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void update() throws InvalidDataException {
		if(resourceList == null || resourceList.isEmpty())
			return;
		//update
		if(this.updateMode == UpdateMode.OVERWRITE){
			((com.vaadin.ui.Image)component).setSource(resourceList.get(0));
		}
		else if(this.updateMode == UpdateMode.APPEND){
			((ImageViewer)component).setImages(resourceList);
		}
	}
	
}
