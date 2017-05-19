/**
 * 
 */
package de.sfk.spicycurry.data;

import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * general data bean
 * @author boris.schneider
 *
 */

public class Bean {
	@Transient
	private IStore store = null;
	@Transient
	private boolean isChanged = false;
	@Transient
	private boolean isCreated = false;
	@Transient
	private boolean isLoaded = false;
	
	/**
	 * ctor
	 * @param store
	 */
	protected Bean (IStore store){
		this.store = store;
		this.isCreated = true;
	}
	
	/*
	 * return the persistor
	 */
	public IPersistor getPersistor(){
		return store.getPersistor();
	}
	/**
	 * refresh the bean from persistence
	 */
	public void refresh(){
		if (getPersistor() != null){ 
			synchronized(this){
				getPersistor().refresh(this);
			}
		}
	}
	/**
	 * persist this object - either create or update
	 */
	public void persist(){
		if (getPersistor() != null) {
			if (isChanged || isCreated){
				synchronized (this){
					EntityTransaction aT = getPersistor().begin();
					//persistor.getEm().lock(this, LockModeType.NONE); -> throws Entity must be managed to call lock: class de.sfk.spicycurry.data.Feature [1010-MIB3-ALG-144542,PMF01], try merging the detached and try the lock again.  
					if(isCreated) getPersistor().persist(this);
					else if (isLoaded) getPersistor().update(this);
					getPersistor().commit(aT);
					setCreated(false);
					setChanged(false);
					setLoaded();
				}
			}
		}
	}
	public boolean isChanged() {
		return isChanged;
	}
	protected void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}
	public boolean isCreated() {
		return isCreated;
	}
	protected void setCreated(boolean flag) {
		this.isCreated = flag;
	}
	public void setLoaded(){
		this.isCreated=false;
		this.isChanged=false;
		this.isLoaded = true;
	}
	public boolean isLoaded() {
		return isLoaded;
	}

}
