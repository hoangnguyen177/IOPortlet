package edu.uq.workways.ioportlet;
//java
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//vaadin
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import org.vaadin.tepi.imageviewer.ImageViewer;

//workway utils
import edu.uq.workways.commons.utils.Base64;
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
	public Image(){
	}
	
	public Image(String _id){
		this.setId(_id);
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
	 * updatemode is ignore: single image anyway
	 */
	@Override
	public void addData(String _data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
		//decode the data
		byte[] imgContents = Base64.decode(_data);
		//now write it
		try{
			String tempDir = this.getTempDir();
			File newFile = new File(tempDir + "/" + UUID.randomUUID());
			FileOutputStream fos = new FileOutputStream(newFile);
			fos.write(imgContents);
			fos.close();
			if(this.updateMode == UpdateMode.OVERWRITE){
				resourceList.clear();
				resourceList.add(new FileResource(newFile));
			}
			else if(this.updateMode == UpdateMode.APPEND){
				resourceList.add(new FileResource(newFile));
			}
			if(update)
				update();
		}
		catch(Exception e){}
		
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
