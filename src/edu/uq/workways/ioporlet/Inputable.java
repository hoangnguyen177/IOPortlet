package edu.uq.workways.ioporlet;
/**
 * Inputable type of GUI that can take user inputs and transfer back to the user
 * There are two ways here: 
 * 1. call back mechanism: call back when something happens
 * 2. pass sinkclient to each inputable, the inputable will decide what happens
 * The second approach seems to be more flexible, but then it does not conform to the outputable
 * So the first approach. Each inputable should just be subclass of outputable. With listeners. 
 * Note that data passing must be in JSON format. 
 * @author hoangnguyen
 *
 */
public interface Inputable extends Outputable{
	/**
	 * addInputListener
	 * @param _listener
	 */
	public void addInputListener(InputListener _listener);
}
