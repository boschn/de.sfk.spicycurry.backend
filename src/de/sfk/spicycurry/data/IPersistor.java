package de.sfk.spicycurry.data;

import java.io.IOException;

import javax.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface IPersistor {

	
	
	/**
	 * @return the em
	 */
	EntityManager getEm();

	/**
	 * start the persistence manager
	 */
	void Open();

	/**
	 * returns true if the PersistenceManager is open and ready
	 * @return
	 */
	boolean isOpen();

	/**
	 * close the persistence manager
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * return true if the object exists
	 * 
	 * @param o
	 * @return
	 */
	boolean exists(Object o);

	/**
	 * persist
	 * @param o
	 */
	void persist(Object o);

	/**
	 * start transaction
	 */
	void begin();

	/**
	 * commit transaction
	 */
	void commit();

	/**
	 * rollback transaction
	 */
	void rollback();

	/**
	 * getLogger
	 * 
	 * @param logger
	 */
	Logger getLogger();
}