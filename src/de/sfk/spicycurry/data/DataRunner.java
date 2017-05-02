/**
 * 
 */
package de.sfk.spicycurry.data;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author boris.schneider
 *
 */
public class DataRunner {

	// Logger
	public static Logger logger = LogManager.getLogger(DataRunner.class);
	
	/**
	 * run database interaction
	 * @param args
	 */
	public static void run(String[] args) {

		if (logger.isDebugEnabled()) {
			logger.debug("entering run(String[])");
		}
		if (args == null || args.length ==0) {
			logger.error("no command to run");
			if (logger.isDebugEnabled()) {
				logger.debug("exiting run()");
			}
			return;
		}

		try {
			switch (args[0].toUpperCase()) {
			
			case "FULLFEED": 
				runPolarionFullFeed(args);
				break;
			case "TEST":
				test(args);
				break;
			default:
				logger.error("no command detected " + args[0] + " to run.\n Possible Comands: FULLFEED, TEST \n");
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			if (logger.isTraceEnabled()) e.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("exiting run()");
		}
	}
	public static void test(String[] args) {
		
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
			
			if (RequirementStore.db.has("1010-MIB3-ALG-57871")){ 
					RequirementStore.db.loadPolarion("1010-MIB3-ALG-57871");
					logger.info("1010-MIB3-ALG-57871 loaded from polarion");
			}
			
			logger.info(RequirementStore.db.count() + " requirements loaded from polarion");
			
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
	public static void runPolarionFullFeed(String[] args) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("entering runPolarionFullFeed(String[])");
			logger.debug("args: " + args);
		}
		try {
			// Setting.Default.read();
			
			logger.info("runner full polarion feed started");
			
			FeatureStore.db.loadAllPolarion();
			logger.info(FeatureStore.db.count() + " features loaded from polarion");
			
			RequirementStore.db.loadAllPolarion(); 
			logger.info(RequirementStore.db.count() + " requirements loaded from polarion");
			
			logger.info("runner full polarion feed ended");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			logger.info("runner full polarion feed failed");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("exiting runPolarionFullFeed()");
		}
	}

}
