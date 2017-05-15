/**
 * 
 */
package de.sfk.spicycurry.data;


import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sfk.spicycurry.Setting;

/**
 * @author boris.schneider
 *
 */
public class DataRunner {

	// Logger
	public static Logger logger = LogManager.getLogger(DataRunner.class);
	
	/**
	 * run the test method
	 * 
	 */
	public static void runTest() {
		
		if (logger.isDebugEnabled()) {
			logger.debug("entering test(String[])");
		}
		try {
			// Setting.Default.read();
			
			logger.info("test started");
			
			if (FeatureStore.db.keySet().size() <= 1400) {
				FeatureStore.db.loadAllPolarion();
				logger.info(FeatureStore.db.count() + " features loaded from polarion");
			}
			
			/* if (RequirementStore.db.has("1010-MIB3-ALG-57871")){ 
					RequirementStore.db.loadPolarion("1010-MIB3-ALG-57871");
					logger.info("1010-MIB3-ALG-57871 loaded from polarion");
					logger.info(RequirementStore.db.count() + " requirements loaded from polarion");
			}
			*/
			SpecificationStore.db.loadAllPolarion();
			logger.info(SpecificationStore.db.count() + " specifications loaded from polarion");
			
			Feature[] theFeatures=FeatureStore.db.all().toArray(new Feature[FeatureStore.db.all().size()]);
			
			// travel through the tree
			theFeatures[30].accept(new FeatureLiveVisitor());
			
			// save properties
			Setting.Default.write(true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("exiting test()");
		}
	}
	/**
	 * run full database feed from polarion
	 * @param args
	 */
	public static void runFeed(Temporal changedSince) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("entering runFeed()");
		}
		try {
			// Setting.Default.read();
			
			logger.info("runner full polarion feed started");
			
			FeatureStore.db.loadAllPolarion(changedSince);
			logger.info(FeatureStore.db.count() + " features loaded from polarion");
			
			RequirementStore.db.loadAllPolarion(changedSince); 
			logger.info(RequirementStore.db.count() + " requirements loaded from polarion");
			
			SpecificationStore.db.loadAllPolarion(changedSince); 
			logger.info(SpecificationStore.db.count() + " specifications loaded from polarion");
			
			logger.info("runner full polarion feed ended");
			
		} catch (Exception e) {
			logger.info("runner full polarion feed failed");
			if (logger.isDebugEnabled()){
				logger.debug(e);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("exiting runPolarionFullFeed()");
		}
	}

}
