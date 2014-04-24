package edu.uq.workways.ioportlet;
//java
import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.UUID;
//vaadin
import com.vaadin.server.FileResource;
//workway utils
import edu.uq.workways.commons.utils.Base64;

/**
 * This class is to display images sent from the clients
 * If append then display in tiled form
 * If overwrite then display in single form
 * @author hoangnguyen
 *
 */
public class Image extends DisplayObject{
	/**
	 * constructor(s)
	 */
	public Image(){
		component = new com.vaadin.ui.Image();
	}
	
	public Image(String _id){
		this.setId(_id);
		component = new com.vaadin.ui.Image();
	}
	
	@Override
	public int getNumberOfSeries() {
		return 1;
	}

	/**
	 * updatemode is ignore: single image anyway
	 */
	@Override
	public void addData(String _data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		//decode the data
		byte[] imgContents = Base64.decode(_data);
		//now write it
		try{
			File newFile = new File("/tmp/" + UUID.randomUUID());
			FileOutputStream fos = new FileOutputStream(newFile);
			fos.write(imgContents);
			fos.close();
			((com.vaadin.ui.Image)component).setSource(new FileResource(newFile));
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
