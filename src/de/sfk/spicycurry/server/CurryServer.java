/**
 * 
 */
package de.sfk.spicycurry.server;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sfk.spicycurry.CmdLine;
import de.sfk.spicycurry.CurryDaemon;
import de.sfk.spicycurry.Globals;
import de.sfk.spicycurry.data.EclipseLinkPersistor;
import de.sfk.spicycurry.data.Feature;
import de.sfk.spicycurry.data.FeatureStore;
import de.sfk.spicycurry.data.IPersistor;
import de.sfk.spicycurry.data.IStore;
import de.sfk.spicycurry.data.Requirement;
import de.sfk.spicycurry.data.RequirementStore;
import de.sfk.spicycurry.data.Specification;
import de.sfk.spicycurry.data.SpecificationStore;

/**
 * server thread processing regular in the background
 * @author boris
 *
 */
public class CurryServer extends Thread {
	
    private boolean isDaemon = false;
    private ChoreStore chores = ChoreStore.db;
    
	// logger
	private static Logger logger = LogManager.getLogger(CurryDaemon.class);

	
	/**
	 * constructor
	 * @param daemon true if daemon
	 */
	public CurryServer(boolean daemon){
		isDaemon = daemon;
		
	}
	/**
	 * @return the chores
	 */
	public Collection<Chore> getChores() {
		return chores.all();
	}
	
    @Override
    public synchronized void start() {
        this.setDaemon(isDaemon);
        // CmdLine.startEmbeddedDBServer(cmd); -> should be started
        super.start();
    }
   
    /**
     * initialize the server
     * @return true if successfull
     */
    private boolean init() {
    	
    	//final String orgName = Thread.currentThread().getName();
        Thread.currentThread().setName("CurryServer");
        
        // default chores
        if (chores.count()<4){
        	// chores.add(new Chore((long) 110 ,"feed features from jira", Chore.JobType.Update, Duration.ofHours(1), new String[] { Feature.class.getName(), "jira" }), true);
        	chores.add(new Chore((long) 120,"feed features from polarion", Chore.JobType.Update, Duration.ofHours(4), new String[] { Feature.class.getName(), "polarion" }), true);
        	//chores.add(new Chore((long) 200,"feed requirement from polarion", Chore.JobType.Update, Duration.ofHours(4), new String[] { Requirement.class.getName(), "polarion" }), true);
        	//chores.add(new Chore((long) 300,"feed specifications from polarion", Chore.JobType.Update, Duration.ofHours(4), new String[] { Specification.class.getName(), "polarion" }), true);
        }
        
    	// load all Features
    	Collection<Feature> theFeatures = FeatureStore.db.all();
    	if (logger.isDebugEnabled()) logger.debug(theFeatures.size() + " features read from store");
    	// load all requirements
    	Collection<Requirement> theRequirements = RequirementStore.db.all();
    	if (logger.isDebugEnabled()) logger.debug(theRequirements.size() + " requirements read from store");
    	// load all Specifications
    	Collection<Specification> theSpecifications = SpecificationStore.db.all();
    	if (logger.isDebugEnabled()) logger.debug(theSpecifications.size() + " specifications read from store");
    	
    	
    	return true;
    }
    /**
     * main run of the curry server
     */
    @Override
    public void run() { 
    	
    	/**
    	 * init the server by loading all objects
    	 *    	 
    	 */
    	if (!init()) {
    		logger.fatal("could not initialize CurryServer '" + this.getName() + "' - server aborted");
    		return ;
    	}
    	/**
    	 * the endless loop until the server is interrupted
    	 */
    	 while ( !isInterrupted() ){
    		 try{
    			 Instant changeDate = Instant.now();
    			 	
    			 // run the chores
    			 for(Chore aChore: chores.all()){
    				// skip if the chore is running
    				if (!aChore.isRunning())
    					
    					
	    				// check if the chore is due
	    				if 
	    				  (aChore.isEnabled() && 
	    				  (aChore.getLastExecuted() == null || 
	    				        (aChore.getLastExecuted() != null && 
	    						(Duration.between(aChore.getLastExecuted().toInstant(), Instant.now()).toMinutes()) > aChore.getIntervallPeriod().toMinutes() ))
	    				  )
	    	    					// run the chore in an own thread
	    	    					//
	    	    				 	new Thread() {
	    	    				 		public void run(){
	    	    				 			this.setName("Chore-" + aChore.getId().toString());
	    	    				 			logger.info("thread " + this.getName() + " started");
	    	    				 			aChore.run(changeDate);
	    	    				 			logger.info("thread " + this.getName() + " finished");
	    	    				 		}
	    	    				 	}.start();
    			 }
    			 
    			 // lay to sleep
    			 Thread.sleep( 60000 );
    	       
    	      }catch ( InterruptedException e ){
    	       interrupt();
    	       logger.info("Server " + this.getName() + " exitting ...");
    	      }
    	 }
    }

}
