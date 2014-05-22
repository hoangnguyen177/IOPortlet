package edu.uq.workways.ioportlet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import com.vaadin.server.FileResource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.uq.workways.commons.utils.Base64;
import edu.uq.workways.commons.utils.PropsUtil;

public class LargeImage extends Image{
	public static final int BUFFER_SIZE = 64*1024;

	/******************************************/
	private String filePath 				= "";
	private FileOutputStream fos 			= null;
	private FileChannel fc 					= null;
	private ByteBuffer byteBuffer 			= ByteBuffer.allocateDirect(BUFFER_SIZE);
	private String 		imageType			= "png";
	/******************************************/
	/**
	 * constructor(s)
	 */
	public LargeImage(){
		super();
	}
	
	public LargeImage(String _id){
		super(_id);
	}
	
	public LargeImage(String _id, String _imageType){
		super(_id);
		imageType = _imageType;
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
				filePath = tempDir + UUID.randomUUID() + "." +imageType;
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
			byteBuffer.clear();
			fc.close();
			fos.close();
			((com.vaadin.ui.Image)component).setSource(new FileResource(new File(filePath)));
		} catch (IOException e) {
			//e.printStackTrace();
		}
		filePath = "";
	}
	
}
