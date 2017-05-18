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
	private IPersistor persistor = null;
	@Transient
	private boolean isChanged = false;
	@Transient
	private boolean isCreated = false;
	@Transient
	private boolean isLoaded = false;
	
	protected Bean (IPersistor persistor){
		this.persistor = persistor;
		this.isCreated = true;
	}
	/*
	 * return the persistor
	 */
	public IPersistor getPersistor(){
		return persistor;
	}
	public void refresh(){
		if (persistor != null) persistor.refresh(this);
	}
	/**
	 * persist this object
	 */
	public void persist(){
		if (persistor != null) {
			if (isChanged || isCreated){
				EntityTransaction aT = persistor.begin();
				//persistor.getEm().lock(this, LockModeType.NONE); -> throws Entity must be managed to call lock: class de.sfk.spicycurry.data.Feature [1010-MIB3-ALG-144542,PMF01], try merging the detached and try the lock again.  
				if(isCreated) persistor.persist(this);
				else if (isLoaded) persistor.update(this);
				persistor.commit(aT);
				setCreated(false);
				setChanged(false);
				setLoaded();
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
