/*
 * Copyright 2009 Electronic Business Systems Ltd.
 */
package gr.ebs.gss.client.rest.resource;

import java.io.Serializable;


/**
 * @author kman
 *
 */
public abstract class RestResource implements Serializable{
	String path;


	/**
	 * @param path
	 */
	public RestResource(String path) {
		super();
		this.path = path;
	}




	/**
	 * Retrieve the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}




	/**
	 * Modify the path.
	 *
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}



	public abstract void createFromJSON(String text);



}
