package de.sfk.spicycurry.data;

import java.io.Closeable;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Abstract Persistor 
 * @author boris.schneider
 *
 */
public abstract class AbstractPersistor implements IPersistor {

	// constants
	public final static String PROPERTY_PERSISTOR_DATABASE_NAME = "Persistor.DatabaseName";
	public final static String PROPERTY_PERSISTOR_DATABASE_PATH = "Persistor.DatabasePath";
	public final static String PROPERTY_PERSISTOR_DATABASE_USERNAME = "Persistor.UserName";
	public final static String PROPERTY_PERSISTOR_DATABASE_PASSWORD = "Persistor.Password";
	
	// singleton
	public static IPersistor Default ;
	protected String persistenceProvider = null;
	protected EntityManagerFactory emf = null;
	protected EntityManager em = null;

	public AbstractPersistor() {
		super();
	}
	/**
	 * @return the persistenceProvider
	 */
	public String getPersistenceProvider() {
		return persistenceProvider;
	}
	/**
	 * @param persistenceProvider the persistenceProvider to set
	 */
	protected void setPersistenceProvider(String persistenceProvider) {
		this.persistenceProvider = persistenceProvider;
	}
	/**
	 * get the entity manager factor 
	 */
	public EntityManagerFactory getEmf() {
		return emf;
	}

	/**
	 * set the entity manager factory
	 * @param em
	 */
	protected void setEmf(EntityManagerFactory emf) {
		this.emf = emf;
	}
	/**
	 * get the entity manager - opens the persistor
	 */
	@Override
	public EntityManager getEm() {
		if (!isOpen()) this.Open();
		return em;
	}

	/**
	 * set the entity manager
	 * @param em
	 */
	protected void setEm(EntityManager em) {
		this.em = em;
	}
	
	@Override
	public boolean isOpen() {
		if (em != null) return em.isOpen();
		return false;
	}

	@Override
	public void close() throws IOException {
		em.close();
		emf.close();
		this.getLogger().info(persistenceProvider + " was closed");
	}

	@Override
	public boolean exists(Object o) {
		if (!isOpen()) this.Open();
		if (em.isOpen()) return em.contains(o);
		
		return false;
	}

	@Override
	public void persist(Object o) {
		if (!isOpen()) this.Open();
		
		if (em.isOpen()) em.persist(o);
		else if (this.getLogger() != null && this.getLogger().isDebugEnabled())
			this.getLogger().debug("EntityManager couldn't open - persist impossible of object " + o.getClass().getName() + " "+ o.toString());
		
	}

	@Override
	public void commit() {
		if (em != null)
			em.getTransaction().commit();
		else if (this.getLogger() != null && this.getLogger().isDebugEnabled()) 
						this.getLogger().debug("em is null");
	}

	/* (non-Javadoc)
	 * @see de.sfk.spicycurry.data.IPersistor#begin()
	 */
	@Override
	public void begin() {
		if (!isOpen()) this.Open();
		try {
			  
			em.getTransaction().begin();
			
		}catch (Exception e)
		{
			if (this.getLogger() != null) {
				this.getLogger().info(e.getMessage());
				if (this.getLogger().isDebugEnabled())
					this.getLogger().catching(e);
			}
		}
	}

	@Override
	public void rollback() {
		try {
				if (em != null)
					em.getTransaction().rollback();
				
			}catch (Exception e){
				if (this.getLogger() != null) {
					this.getLogger().info(e.getMessage());
					if (this.getLogger().isDebugEnabled())
						this.getLogger().error(e.getStackTrace());
				}
			}
			
	}

}