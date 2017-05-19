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

import de.sfk.spicycurry.Globals;
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
		Default = new EclipseLinkPersistor("Default");
	}
	// Properties
	
	// statics
	private static final String PROPERTY_PERSISTOR_PROVIDER_NAME = "Persistor.ProviderName";
	private static final String PERSISTENCE_PROVIDER_DEFAULTNAME = "SpicyCurry";
	// Logger
	protected static Logger logger = LogManager.getLogger(EclipseLinkPersistor.class);
	
	/**
	 * constructor
	 */
	public EclipseLinkPersistor(String id){
		super(id);
		persistenceProvider =  Setting.Default.get(PROPERTY_PERSISTOR_PROVIDER_NAME, PERSISTENCE_PROVIDER_DEFAULTNAME);
	}
	
	public EclipseLinkPersistor(String id, String providerName) {
		super(id);
		persistenceProvider = providerName;
	}
	/**
	 * create the entityManager factory
	 * @param persistenceProvider
	 * @param properties
	 */
	private synchronized static void createEMF(String persistenceProvider, Map<String,String> properties){
		
		if (properties != null) entityManagerFactory = Persistence.createEntityManagerFactory(persistenceProvider, properties);
		else entityManagerFactory = Persistence.createEntityManagerFactory(persistenceProvider);
		
		logger.info( "entitiyManagerFactor ["+ persistenceProvider+ "]" + " was created");
	}
	/* (non-Javadoc)
	 * @see de.sfk.spicycurry.data.IPersistor#Open()
	 */
	@Override
	public  synchronized void  Open() {
		// if we started earlier an server to reset properties to reach the own server
		// maybe that in persistence.xml is something else specified
		if ((entityManagerFactory == null) || !entityManagerFactory.isOpen()){
			if (Globals.DBServer.isServerRunning()){
				Map<String,String> props = new HashMap<String,String>();
				// set the correct properties
				
				props.put(PersistenceUnitProperties.JDBC_URL, Globals.DBServer.getJDBCUrl());
				props.put(PersistenceUnitProperties.JDBC_USER, Globals.DBServer.getUserId());
				props.put(PersistenceUnitProperties.JDBC_PASSWORD, Globals.DBServer.getPassWord());
				props.put(PersistenceUnitProperties.JDBC_DRIVER, Globals.DBServer.getJDBCDriverName());
				if (logger.isDebugEnabled()) 
					logger.info("JDBC settings to local embedded TCP server " + Globals.DBServer.getAddress());

				// create the emf
				createEMF(persistenceProvider, props);
				
			} else {
				// create the emf
				createEMF(persistenceProvider, null);
			}
		}
		

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
