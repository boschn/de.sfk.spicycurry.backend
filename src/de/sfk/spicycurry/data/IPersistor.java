package de.sfk.spicycurry.data;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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
	void persist(Bean bean);

	/**
	 * start transaction
	 */
	EntityTransaction begin();

	/**
	 * commit transaction
	 */
	void commit(EntityTransaction t);

	/**
	 * rollback transaction
	 */
	void rollback(EntityTransaction t);

	/**
	 * getLogger
	 * 
	 * @param logger
	 */
	Logger getLogger();

	void update(Bean bean);

	void refresh(Bean bean);

	
}