/**
 * 
 */
package de.sfk.spicycurry.data;

import java.io.Closeable;
import java.util.Properties;

import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author boris.schneider
 *
 */
public class ObjectDBPersistor extends AbstractPersistor implements Closeable, IPersistor {
	
	
	// initialize
	static {
			Default = new ObjectDBPersistor();
	}
		
	// statics
	private static final String PERSISTENCE_PROVIDER_DEFAULTPATH = "$objectdb/db/";
	private static final String PERSISTENCE_PROVIDER_DEFAULTNAME = "SpicyCurry";
	
	// Logger
	protected Log log = LogFactory.getLog(ObjectDBPersistor.class);
	
	/**
	 * constructor
	 */
	public ObjectDBPersistor(){
		super();
		String name = Setting.Default.get(PROPERTY_PERSISTOR_DATABASE_NAME, PERSISTENCE_PROVIDER_DEFAULTNAME);
		String path = Setting.Default.get(PROPERTY_PERSISTOR_DATABASE_PATH, PERSISTENCE_PROVIDER_DEFAULTPATH);
		persistenceProvider = path + name + ".odb";
	}
	
	public ObjectDBPersistor(String path, String name) {
		super();
		persistenceProvider = path + name + ".odb";
	}

	/* (non-Javadoc)
	 * @see de.sfk.spicycurry.data.IPersistor#Open()
	 */
	@Override
	public void Open() {
		if ((emf == null) || !emf.isOpen()) 
			emf = Persistence.createEntityManagerFactory(persistenceProvider);
		if ((em == null) || !em.isOpen()) 
			em = emf.createEntityManager();
		
		log.info(persistenceProvider + " was opened");
	}

	/**
	 * gets the log
	 */
	@Override
	public Log getLog() {
		return log;
	}
}
