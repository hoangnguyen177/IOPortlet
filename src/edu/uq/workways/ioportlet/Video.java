package edu.uq.workways.ioportlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;
import java.util.UUID;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.VerticalLayout;

import edu.uq.workways.commons.utils.Base64;

public class Video extends DisplayObject{
	public static final int BUFFER_SIZE = 64*1024;

	/******************************************/
	private String filePath 				= "";
	private FileOutputStream fos 			= null;
	private FileChannel fc 					= null;
	private ByteBuffer byteBuffer 			= ByteBuffer.allocateDirect(BUFFER_SIZE);
	private String		videoType			= "ogv"; //defalt
	/******************************************/

	/**
	 * constructor(s)
	 */
	public Video(){
	}
	
	public Video(String _id){
		this.setId(_id);
	}
	
	public Video(String _id, String _videoType){
		this.setId(_id);
		videoType = _videoType;
	}
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}
	
	
	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}
	
	/**
	 * updatemode is ignore: single image anyway
	 */
	@Override
	public void addData(String _data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		if(update && filePath.isEmpty())
			throw new InvalidDataException("Invalid Data, the file is not created yet");
		else if(update)
		{
			update();
			return;
		}
		try{
			if(filePath.isEmpty()){
				String tempDir = this.getTempDir() + "/";
				filePath = tempDir + UUID.randomUUID() + "." + videoType;
				fos = new FileOutputStream(filePath);
				fc = fos.getChannel();
			}			
			if(fos == null || fc == null)
				throw new InvalidDataException("[addData@LarImage] fileinputstream or filechannel cannot be null");
			byteBuffer.clear();
			byteBuffer.put(Base64.decode(_data));
			byteBuffer.flip();
			fc.write(byteBuffer);
		}
		catch(IOException e){
			throw new InvalidDataException("[addData@LarImage] failed to write file");
		}
		
		
	}
	
	@Override
	public void update() throws InvalidDataException {	
		if(fc == null || fos == null)
			throw new InvalidDataException("[update@LarImage] fileinputstream or filechannel cannot be null");
		try {
			fc.close();
			fos.close();
			//byteBuffer.clear();
			((com.vaadin.ui.Video)component).setSource(new FileResource(new File(filePath)));
			((com.vaadin.ui.Video)component).setWidth(500, Unit.PIXELS);
			((com.vaadin.ui.Video)component).setHeight(400, Unit.PIXELS);
			((com.vaadin.ui.Video)component).setHtmlContentAllowed(true);
			((com.vaadin.ui.Video)component).setAltText("Can't play media");
			((com.vaadin.ui.Video)component).setAutoplay(true);
		} catch (IOException e) {
			System.out.println("[update@Video] error:" + e.getMessage());
		}
		filePath = "";
	}

	@Override
	public void createDisplayObject() {
		component = new com.vaadin.ui.Video();
		component.setSizeFull();
	}
	

}
