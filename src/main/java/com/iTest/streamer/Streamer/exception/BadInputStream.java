package com.iTest.streamer.Streamer.exception;

public class BadInputStream extends Exception{

	
	private static final long serialVersionUID = 1L;
	
	public BadInputStream () {
		super();
	}
	
	public BadInputStream ( String error) {
		super(error); 
	}

}
