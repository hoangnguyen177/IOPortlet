package edu.uq.workways.ioportlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vaadin.data.util.sqlcontainer.SQLContainer;

import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.uq.workways.commons.utils.Base64;
import edu.uq.workways.ioportlet.IoportletUI.MessageType;
import edu.uq.workways.ioportlet.mincviewer.MincClient;

public class MincViewer extends DisplayObject{
	public static final int BUFFER_SIZE = 64*1024;
	/********************************************************/
	private boolean sendFiles = true;
	private JSONArray fileList = new JSONArray();
	private String headerFile = "";
	private String rawFile	= "";
	
	private FileOutputStream fos 			= null;
	private FileChannel fc 					= null;
	private ByteBuffer byteBuffer 			= ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	/********************************************************/
	
	public MincViewer(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer, boolean _sendFiles){
		this.setId(_id);
		this.setUserName(uname);
		this.setMessageContainer(msgContainer);
		this.setSourceSinkId(sourceSinkContainer);
		sendFiles = _sendFiles;
	}	
	
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	
	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	
	@Override
	public void update() throws InvalidDataException {
		if(sendFiles){
			if(fc == null || fos == null)
				throw new InvalidDataException("[update@MincViewer] fileinputstream or filechannel cannot be null");
			try {
				byteBuffer.clear();
				fc.close();
				fos.close();
			} catch (IOException e) {return;}
		}
		
		Map<String, Object> _aFile = new HashMap<String, Object>();
		_aFile.put("type", "minc");
		_aFile.put("header_url", headerFile);
		_aFile.put("raw_data_url", rawFile);
		Map<String, String> _template = new HashMap<String,String>();
		_template.put("element_id", "volume-ui-template");
		_template.put("viewer_insert_class", "volume-viewer-display");
		_aFile.put("template", _template);
		System.out.println("finish receiving files:" + _aFile);
		if(this.updateMode == UpdateMode.OVERWRITE)
			fileList = new JSONArray();
		fileList.put(_aFile);
		//set the filelist
		((MincClient)component).setVolumeList(fileList);		
		//clear headerfile and rawfile
		headerFile 		= "";
		rawFile 		= "";
	}

	/**
	 * addData
	 */
	@Override
	public void addData(JsonObject message)	throws UpperLimitNumberOfSeriesException, InvalidDataException {
		boolean _append = message.get("append").getAsBoolean();
		String _path = message.get("path").getAsString();
		String _data = message.get("data").getAsString();
		boolean isRecordedMessage = false;
		if(message.has("recorded"))
			isRecordedMessage = message.get("recorded").getAsBoolean();
		//if not sending files
		if(!sendFiles){			
			JsonParser _parser = new JsonParser();
			try{
				JsonObject _contents = _parser.parse(_data).getAsJsonObject();
				headerFile = _contents.get("header").getAsString();
				rawFile	   = _contents.get("raw").getAsString();
				//update
				if(!_append && !headerFile.isEmpty() && !rawFile.isEmpty())
					update();
				if(!isRecordedMessage){
					Long _timeStampLong = message.get("timestamp").getAsLong();
					Timestamp _timeStamp = new Timestamp(_timeStampLong);
					Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
					message.remove("data");
					this.saveMessage(message.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, "");
				}
			}
			catch(JsonSyntaxException e){
				return;
			} catch (UnsupportedOperationException e) {
				return;
			} catch (SQLException e) {
				return;
			}
		}
		//if sending files
		else{
			try{
				if(isRecordedMessage){
					if(message.has("filepath") && !message.get("filepath").getAsString().trim().isEmpty()){
						String _filepath = message.get("filepath").getAsString().trim();
						String [] _filePathSplitted = _filepath.split("::");
						headerFile = _filePathSplitted[0];
						rawFile = _filePathSplitted[1];						
					}
				}
				if(!headerFile.isEmpty() && !rawFile.isEmpty()){
					if(!isRecordedMessage){
						//save here
						Long _timeStampLong = message.get("timestamp").getAsLong();
						Timestamp _timeStamp = new Timestamp(_timeStampLong);
						Long _sourceSinkId = message.get("sourcesinkid").getAsLong();
						this.saveMessage(message.toString(), MessageType.source.toString(), _path, _timeStamp, _sourceSinkId, headerFile + "::" + rawFile);								
						byteBuffer.clear();
						byteBuffer.put(Base64.decode(_data));
						byteBuffer.flip();
						fc.write(byteBuffer);
					}
					if(!_append)
					{
						update();
						return;
					}
					return;
				}
				String randomName = UUID.randomUUID().toString();
				File newFile = new File(this.getStorePath() + "/" + randomName);		
				fos = new FileOutputStream(newFile);
				fc = fos.getChannel();
				byteBuffer.clear();
				if(headerFile.isEmpty()){
					headerFile = linkName() +"/"+ this.getUserName() +"/"+ this.getClass().getName() +"/" + randomName;
					byteBuffer.put(_data.getBytes(Charset.forName("UTF-8")));
					byteBuffer.flip();
					fc.write(byteBuffer);
					fc.close();
					fos.close();				
				}
				else if(rawFile.isEmpty()){
					rawFile = linkName() + "/" + this.getUserName() +"/"+ this.getClass().getName() +"/" +randomName;
					byteBuffer.put(Base64.decode(_data));
					byteBuffer.flip();
					fc.write(byteBuffer);
				}			
			}
			catch(IOException e){
				return;
			} catch (UnsupportedOperationException e) {
				return;
			} catch (SQLException e) {
				return;
			}
		}
	}
	
	
	@Override
	public void createDisplayObject() {
		component = new MincClient();
	}
	
	

}
