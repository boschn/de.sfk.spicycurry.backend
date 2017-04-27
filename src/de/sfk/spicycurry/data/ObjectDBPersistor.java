/**
 * 
 */
package de.sfk.spicycurry.data;

import java.io.Closeable;
import java.util.Properties;
import javax.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	protected Logger logger = LogManager.getLogger(ObjectDBPersistor.class);
	
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
		
		logger.info(persistenceProvider + " was opened");
	}

	/**
	 * gets the log
	 */
	@Override
	public Logger getLogger() {
		return logger;
	}
}
