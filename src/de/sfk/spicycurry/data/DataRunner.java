/**
 * 
 */
package de.sfk.spicycurry.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.polarion.alm.ws.client.types.tracker.Custom;
import com.polarion.alm.ws.client.types.tracker.CustomField;
import com.polarion.alm.ws.client.types.tracker.LinkedWorkItem;
import com.polarion.alm.ws.client.types.tracker.WorkItem;

/**
 * @author boris.schneider
 *
 */
public class DataRunner {

	// Logger
	public static Log logger = LogFactory.getLog(DataRunner.class);
	
	/**
	 * run database interaction
	 * @param args
	 */
	public static void run(String[] args) {

		if (args == null || args.length ==0) {
			logger.error("no command to run");
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
	}
	public static void test(String[] args) {
		
		try {
			// Setting.Default.read();
			
			logger.info("runner started");
			
			if (FeatureStore.db.keySet().size() == 0) {
				FeatureStore.db.loadAllPolarion();
				logger.info(FeatureStore.db.count() + " features loaded from polarion");
			}
			
			// RequirementStore.db.loadAllPolarion(); /* "1010-MIB3-ALG-57871" */
			// logger.info(RequirementStore.db.count() + " requirements loaded from polarion");
			
			Feature[] theFeatures=FeatureStore.db.all().toArray(new Feature[FeatureStore.db.all().size()]);
			// travel through the tree
			theFeatures[30].accept(new FeatureLiveVisitor());
			
			// save properties
			Setting.Default.write(true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * run full database feed from polarion
	 * @param args
	 */
	public static void runPolarionFullFeed(String[] args) {
		
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
	}

}
