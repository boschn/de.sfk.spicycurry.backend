/**
 * 
 */
package de.sfk.spicycurry.data;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	protected Log log = LogFactory.getLog(EclipseLinkPersistor.class);
	
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
		if ((emf == null) || !emf.isOpen()) 
			emf = Persistence.createEntityManagerFactory(persistenceProvider);
		if ((em == null) || !em.isOpen()) 
			em = emf.createEntityManager();
		
		log.info(persistenceProvider + " was opened");

	}

	/* (non-Javadoc)
	 * @see de.sfk.spicycurry.data.IPersistor#getLog()
	 */
	@Override
	public Log getLog() {
		// TODO Auto-generated method stub
		return null;
	}

}
