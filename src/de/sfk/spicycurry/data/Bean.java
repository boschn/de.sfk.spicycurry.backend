/**
 * 
 */
package de.sfk.spicycurry.data;

import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

/**
 * general data bean
 * @author boris.schneider
 *
 */
public class Bean {
	
	private IPersistor persistor = null;
	private boolean isChanged = false;
	
	protected Bean (IPersistor persistor){
		this.persistor = persistor;
	}
	/*
	 * return the persistor
	 */
	public IPersistor getPersistor(){
		return persistor;
	}
	
	/**
	 * persist this object
	 */
	public void persist(){
		if (persistor != null) {
				EntityTransaction aT = persistor.begin();
				//persistor.getEm().lock(this, LockModeType.NONE); -> throws Entity must be managed to call lock: class de.sfk.spicycurry.data.Feature [1010-MIB3-ALG-144542,PMF01], try merging the detached and try the lock again.  
				persistor.persist(this);
				persistor.commit(aT);
				setChanged(false);
		}
	}
	public boolean isChanged() {
		return isChanged;
	}
	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

}
