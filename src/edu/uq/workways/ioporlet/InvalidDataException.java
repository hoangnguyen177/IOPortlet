package edu.uq.workways.ioporlet;

public class InvalidDataException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constructor
	 */
	public InvalidDataException(){
		super();
	}
	
	public InvalidDataException(String message){
		super(message);
	}

}
