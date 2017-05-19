/**
 * 
 */
package de.sfk.spicycurry.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sfk.spicycurry.Setting;
import de.sfk.spicycurry.data.EclipseLinkPersistor;
import de.sfk.spicycurry.data.IPersistor;
import de.sfk.spicycurry.data.IStore;
import de.sfk.spicycurry.data.JiraIssueFeature;
import de.sfk.spicycurry.data.JiraIssueLoader;
import de.sfk.spicycurry.data.JiraIssueStore;
import de.sfk.spicycurry.data.SpecificationStore;

/**
 * store of the chores
 * @author boris
 *
 */
public class ChoreStore implements IStore<Chore> {

	// store of chores
	private Queue<Chore> chores = new ConcurrentLinkedQueue<Chore>();

	// Persistor
	private IPersistor persistor;
		
	private boolean isInitialized = false;
	
	// class singleton
	public static final ChoreStore db = new ChoreStore();
		
	// Logger
	private Logger logger = LogManager.getLogger(ChoreStore.class);
		
	/**
	 * constructor
	 */
	protected ChoreStore() {
		super();
	}
	
	@Override
	public Chore getById(String id) {
		if (!isInitialized()) this.open();
		for (Chore aChore: chores)
			if (id.equals(aChore.getId().toString())) return aChore;
		return null;
	}
	public Chore getById(long id) {
		
		for (Chore aChore: chores)
			if (id == aChore.getId()) return aChore;
		return null;
	}
	@Override
	public Chore add(Chore o, boolean force) {
		if (!isInitialized()) this.open();
		for (Chore aChore: chores)
			if (force && (o.getId()==aChore.getId())) chores.remove(aChore);
			else if (o.getId()==aChore.getId()) return null;
			
		chores.add(o);
		return o;
	}

	@Override
	public boolean has(Chore o) {
		if (!isInitialized()) this.open();
		for (Chore aChore: chores)
			if (o.getId().toString().equals(aChore.getId().toString())) return true;
		return false;
	}

	@Override
	public boolean has(String id) {
		if (!isInitialized()) this.open();
		for (Chore aChore: chores)
			if (id.equals(aChore.getId().toString())) return true;
		return false;
	}
	public boolean has(long id) {
		if (!isInitialized()) this.open();
		for (Chore aChore: chores)
			if (id == aChore.getId()) return true;
		return false;
	}

	@Override
	public long count() {
		if (!isInitialized()) this.open();
		return chores.size();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean persist() {
		boolean flag = true;
		for (Chore aChore: chores)  aChore.persist();
		return flag;
	}

	@Override
	public IPersistor getPersistor() {
		if (persistor == null) 
			persistor = new EclipseLinkPersistor("ChoreStore");
		return persistor;
	}
	/**
	 * @return the isInitialized
	 */
	public synchronized boolean isInitialized() {
		return isInitialized;
	}
	/**
	 * @param isInitialized the isInitialized to set
	 */
	private synchronized void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}
	/**
	 * open persistence
	 */
	private synchronized void open() {

		try {
		Query aQueryFeature =
				getPersistor().getEm().createQuery("select f from ServerChores f");
			aQueryFeature.setLockMode(LockModeType.NONE);
	        List<Chore> theFeatureResults = aQueryFeature.getResultList();
	        for (Chore f : theFeatureResults) {
	        	f.setLoaded();
	        	chores.add(f);
	        }
	        this.setInitialized(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			 logger.info(e.getLocalizedMessage());
			 return;
		} 
	        
	    logger.info("Store has retrieved " + chores.size() + " entries from persistence");
	}

	public Collection<Chore> all() {
		
		return chores;
	}

}
