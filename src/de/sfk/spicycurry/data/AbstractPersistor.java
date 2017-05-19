package de.sfk.spicycurry.data;

import java.io.Closeable;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

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
	
	private String id ;
	// singleton
	public static IPersistor Default ;
	protected String persistenceProvider = null;
	protected static EntityManagerFactory entityManagerFactory = null;
	protected EntityManager entityManager = null;

	
	public AbstractPersistor(String id) {
		super();
		setId(id);
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
		return entityManagerFactory;
	}

	/**
	 * set the entity manager factory
	 * @param entityManager
	 */
	protected void setEmf(EntityManagerFactory emf) {
		this.entityManagerFactory = emf;
	}
	/**
	 * get the entity manager - opens the persistor
	 */
	@Override
	public EntityManager getEm() {
		if (!isOpen()) this.Open();
		// get the entity manager
		if ((entityManager == null) || !entityManager.isOpen()) 
			entityManager = entityManagerFactory.createEntityManager();
		return entityManager;
	}

	/**
	 * set the entity manager
	 * @param em
	 */
	protected void setEm(EntityManager em) {
		this.entityManager = em;
	}
	
	@Override
	public boolean isOpen() {
		if (entityManager != null) return entityManager.isOpen();
		return false;
	}

	@Override
	public synchronized void close() throws IOException {
		if (entityManager != null) entityManager.close();
		if (entityManagerFactory != null) entityManagerFactory.close();
		this.getLogger().info(getId() + " " + persistenceProvider + " was closed");
	}

	@Override
	public synchronized boolean exists(Object o) {
		if (!isOpen()) this.Open();
		if (entityManager.isOpen()) return entityManager.contains(o);
		
		return false;
	}

	@Override
	public synchronized void persist(Bean o) {
		if (!isOpen()) this.Open();
		
		if (entityManager.isOpen()) entityManager.persist(o);
		else if (this.getLogger() != null && this.getLogger().isDebugEnabled())
			this.getLogger().debug(getId() + " " +"EntityManager couldn't open - persist impossible of object " + o.getClass().getName() + " "+ o.toString());
		
	}
	@Override
	public void refresh(Bean o){
		if (!isOpen()) this.Open();
		
		if (entityManager.isOpen()) entityManager.refresh(o);
		
		else if (this.getLogger() != null && this.getLogger().isDebugEnabled())
			this.getLogger().debug(getId() + " " +"EntityManager couldn't open - persist impossible of object " + o.getClass().getName() + " "+ o.toString());

	}
	@Override
	public void update(Bean o) {
		
			if (!isOpen()) this.Open();
			
			if (entityManager.isOpen()) entityManager.merge(o);
			else if (this.getLogger() != null && this.getLogger().isDebugEnabled())
				this.getLogger().debug(getId() + " " +"EntityManager couldn't open - persist impossible of object " + o.getClass().getName() + " "+ o.toString());

		
	}
	@Override
	public synchronized void commit(EntityTransaction t) {
		if (entityManager != null)
			t.commit();
		else if (this.getLogger() != null && this.getLogger().isDebugEnabled()) 
						this.getLogger().debug(getId() + " " +"em is null");
	}

	/* (non-Javadoc)
	 * @see de.sfk.spicycurry.data.IPersistor#begin()
	 */
	@Override
	public synchronized EntityTransaction begin() {
		if (!isOpen()) this.Open();
		try {
			EntityTransaction aT = entityManager.getTransaction();
			aT.begin();
			return aT;
		}catch (Exception e)
		{
			if (this.getLogger() != null) {
				this.getLogger().error(getId() + " " +e.getMessage());
				if (this.getLogger().isDebugEnabled())
					this.getLogger().catching(e);
			}
		}
		return null;
	}

	@Override
	public synchronized void rollback(EntityTransaction t) {
		try {
 			t.rollback();
			}catch (Exception e){
				if (this.getLogger() != null) {
					this.getLogger().error(getId() + " " + e.getMessage());
					if (this.getLogger().isDebugEnabled())
						this.getLogger().error(e.getStackTrace());
				}
			}
			
	}

}