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
			String url = Setting.Default.get(JiraParameter.PROPERTY_JIRA_BASEURL, "https://jira3.technisat-digital.de");
			String name = Setting.Default.get(JiraParameter.PROPERTY_JIRA_USERNAME, "boris.schneider");
			String password = Setting.Default.get(JiraParameter.PROPERTY_JIRA_PASSWORD, "Carconnect1!");
			JiraIssueStore.db.loadJira("MIBSERIELH-4426");
			
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
