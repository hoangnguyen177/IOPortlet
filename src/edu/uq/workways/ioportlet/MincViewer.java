package edu.uq.workways.ioportlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import edu.monash.io.iolibrary.ConfigurationConsts.UpdateMode;
import edu.uq.workways.commons.utils.Base64;
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
	
	public MincViewer(boolean _sendFiles){
		sendFiles = _sendFiles;
	}
	
	public MincViewer(String _id, boolean _sendFiles){
		this.setId(_id);
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

	@Override
	public void addData(String data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		if(!sendFiles){			
			JsonParser _parser = new JsonParser();
			try{
				JsonObject _contents = _parser.parse(data).getAsJsonObject();
				headerFile = _contents.get("header").getAsString();
				rawFile	   = _contents.get("raw").getAsString();
				//update
				if(update && !headerFile.isEmpty() && !rawFile.isEmpty())
					update();

			}
			catch(JsonSyntaxException e){
				return;
			}
		}
		else{
			try{
				if(!headerFile.isEmpty() && !rawFile.isEmpty()){
					if(update)
					{
						update();
						return;
					}
					byteBuffer.clear();
					byteBuffer.put(Base64.decode(data));
					byteBuffer.flip();
					fc.write(byteBuffer);
					return;
				}
				String tempDir = this.getTempDir();
				String randomName = UUID.randomUUID().toString();
				File newFile = new File(tempDir + "/" + randomName);		
				fos = new FileOutputStream(newFile);
				fc = fos.getChannel();
				byteBuffer.clear();
				if(headerFile.isEmpty()){
					headerFile = linkName() + "/" + randomName;
					byteBuffer.put(data.getBytes(Charset.forName("UTF-8")));
					byteBuffer.flip();
					fc.write(byteBuffer);
					fc.close();
					fos.close();				
				}
				else if(rawFile.isEmpty()){
					rawFile = linkName() + "/" + randomName;
					byteBuffer.put(Base64.decode(data));
					byteBuffer.flip();
					fc.write(byteBuffer);
				}			
			}
			catch(IOException e){
				return;
			}
		}
	}

	@Override
	public void createDisplayObject() {
		component = new MincClient();
	}

}
