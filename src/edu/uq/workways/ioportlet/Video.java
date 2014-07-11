package edu.uq.workways.ioportlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.VerticalLayout;

import edu.uq.workways.commons.utils.Base64;

public class Video extends LargeImage{
	public static final int BUFFER_SIZE = 64*1024;

	
	/**
	 * constructor(s)
	 */
	public Video(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer){
		super(_id, uname, msgContainer, sourceSinkContainer);
		fileType = "ogv";
	}
	
	public Video(String _id, String uname, SQLContainer msgContainer, SQLContainer sourceSinkContainer, String _videoType){
		super(_id, uname, msgContainer, sourceSinkContainer, _videoType);
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
		if(fc != null && fos != null){
			try {
				fc.close();
				fos.close();
				//byteBuffer.clear();
				fc =null;
				fos = null;
			}
			catch (IOException e) {
				System.out.println("[update@Video] error:" + e.getMessage());
			}
		}

		((com.vaadin.ui.Video)component).setSource(new FileResource(new File(filePath)));
		((com.vaadin.ui.Video)component).setWidth(500, Unit.PIXELS);
		((com.vaadin.ui.Video)component).setHeight(400, Unit.PIXELS);
		((com.vaadin.ui.Video)component).setHtmlContentAllowed(true);
		((com.vaadin.ui.Video)component).setAltText("Can't play media");
		((com.vaadin.ui.Video)component).setAutoplay(true);
		filePath = "";
	}

	@Override
	public void createDisplayObject() {
		component = new com.vaadin.ui.Video();
		component.setSizeFull();
	}
	

}
