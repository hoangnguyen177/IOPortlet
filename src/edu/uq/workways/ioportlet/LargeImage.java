package edu.uq.workways.ioportlet;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.server.FileResource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.uq.workways.commons.utils.Base64;
import edu.uq.workways.ioportlet.IoportletUI.MessageType;

public class LargeImage extends Image{
	public static final int BUFFER_SIZE = 64*1024;

	/******************************************/
	protected String filePath 				= "";
	protected FileOutputStream fos 			= null;
	protected FileChannel fc 					= null;
	protected ByteBuffer byteBuffer 			= ByteBuffer.allocateDirect(BUFFER_SIZE);
	protected String 		fileType			= "png";
	/******************************************/
	/**
	 * constructor(s)
	 */
	public LargeImage(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		super(_id, uname, msgContainer, sourceSinkContainer);
	}
	
	public LargeImage(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer, String _fileType){
		super(_id, uname, msgContainer, sourceSinkContainer);
		fileType = _fileType;
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
		
		if(isRecordedMessage){
			if(message.has("filepath") && !message.get("filepath").getAsString().trim().isEmpty()){
				filePath = message.get("filepath").getAsString();			
				update();
			}
		}
		else{
			if(!_append && filePath.isEmpty())
				throw new InvalidDataException("Invalid Data, the file is not created yet");
			else if(!_append)
			{
				Long _timeStampLong = message.get("timestamp").getAsLong();
				Timestamp _timeStamp = new Timestamp(_timeStampLong);
				Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
				try {
					message.remove("data");
					this.saveMessage(message.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, filePath);
				} catch (UnsupportedOperationException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}		
				update();
				return;
			}
			
			//now save data part
			try{
				if(filePath.isEmpty()){
					filePath = this.getStorePath() + "/" + UUID.randomUUID() + "." +fileType;
					fos = new FileOutputStream(filePath);
					fc = fos.getChannel();
				}			
				if(fos == null || fc == null)
					throw new InvalidDataException("[addData@LarImage] fileinputstream or filechannel cannot be null");
				byteBuffer.clear();
				String _data = message.get("data").getAsString();
				byteBuffer.put(Base64.decode(_data));
				byteBuffer.flip();
				fc.write(byteBuffer);
			}
			catch(IOException e){
				throw new InvalidDataException("[addData@LarImage] failed to write file");
			}			
		}				
	}
		
	@Override
	public void update() throws InvalidDataException {	
		if(fc != null && fos != null)
		{
			try {
				byteBuffer.clear();
				fc.close();
				fos.close();
				fc =null;
				fos = null;
			} catch (IOException e) {}
		}
		((com.vaadin.ui.Image)component).setSource(new FileResource(new File(filePath)));
		filePath = "";
	}	
}
