package de.sfk.spicycurry.data;

import java.util.Set;

/**
 * data store for storing, setting and retrieving beans
 * @author boris
 *
 */
public interface IStore<T> {

	/**
	 * returns the object by id
	 * @param id
	 * @return
	 */
	T getById(String id);
	/**
	 * adds the object to the store
	 * @param o
	 * @param force true if adding on any case
	 * @return the added object
	 */
	T add(T o, boolean force);
	/** 
	 * 
	 * @param o of Type T
	 * @return true if in the store
	 */
	boolean has(T o);
	/**
	 * true if object by unique id is in store
	 * @param id
	 * @return
	 */
	public boolean has(String id);
	/**
	 * @return the size of the store
	 */
	long count();
	/**
	 * close all sessions
	 */
	void close();

	/**
	 * get the set of keys
	 * @return
	 */
	Set<String> keySet();

	/** 
	 * persist them all
	 * @return
	 */
	boolean persist();

	/**
	 * @return persistor
	 */
	IPersistor getPersistor();

}