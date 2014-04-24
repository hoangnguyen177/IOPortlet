package edu.uq.workways.ioportlet;

//gson
import com.google.gson.JsonElement;

/**
 * InputableListener
 * @author hoangnguyen
 *
 */
public interface InputListener {
	/**
	 * this method is called when user inputs is received
	 * @param userInput
	 */
	public void onUserInput(JsonElement userInput);
}
