/**
 * 
 */
package de.sfk.spicycurry.data;

/**
 * general data bean
 * @author boris.schneider
 *
 */
public class Bean {
	
	private IPersistor persistor = null;
	
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
			
				persistor.begin();
				persistor.persist(this);
				persistor.commit();
			
		}
	}

}
