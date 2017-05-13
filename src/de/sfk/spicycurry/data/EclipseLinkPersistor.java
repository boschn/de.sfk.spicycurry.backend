/**
 * 
 */
package de.sfk.spicycurry.data;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import de.sfk.spicycurry.Setting;
/**
 * persistor class for eclipseLink
 * 
 * @author boris.schneider
 *
 */
public class EclipseLinkPersistor extends AbstractPersistor implements IPersistor {

	// initialize
	static {
		Default = new EclipseLinkPersistor();
	}
	// Properties
	
	// statics
	private static final String PERSISTENCE_PROVIDER_DEFAULTNAME = "H2LOCAL";
	// Logger
	protected Logger logger = LogManager.getLogger(EclipseLinkPersistor.class);
	
	/**
	 * constructor
	 */
	public EclipseLinkPersistor(){
		super();
		persistenceProvider =  Setting.Default.get(PROPERTY_PERSISTOR_DATABASE_NAME, PERSISTENCE_PROVIDER_DEFAULTNAME);
	}
	
	public EclipseLinkPersistor(String name) {
		super();
		persistenceProvider = name;
	}
	/* (non-Javadoc)
	 * @see de.sfk.spicycurry.data.IPersistor#Open()
	 */
	@Override
	public void Open() {
		// if we started earlier an server to reset properties to reach the own server
		// maybe that in persistence.xml is something else specified
		if ((emf == null) || !emf.isOpen()){
			if (Globals.DBServer.isServerRunning()){
				Map props = new HashMap();
				// set the correct properties
				
				props.put(PersistenceUnitProperties.JDBC_URL, Globals.DBServer.getJDBCUrl());
				props.put(PersistenceUnitProperties.JDBC_USER, "sa");
				props.put(PersistenceUnitProperties.JDBC_PASSWORD, "");

				emf = Persistence.createEntityManagerFactory("pu-name", props);
			} else {
				// default entity manager factory by persistence.xml
				emf = Persistence.createEntityManagerFactory(persistenceProvider);
			}

		}
		// get the entity manager
		if ((em == null) || !em.isOpen()) 
			em = emf.createEntityManager();
		
		logger.info(persistenceProvider + " was opened");

	}

	/* (non-Javadoc)
	 * @see de.sfk.spicycurry.data.IPersistor#getLog()
	 */
	@Override
	public Logger getLogger() {
		// TODO Auto-generated method stub
		return this.logger;
	}

}
