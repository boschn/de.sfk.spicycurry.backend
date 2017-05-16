/**
 * 
 */
package de.sfk.spicycurry.server;

import java.time.Duration;
import java.time.Instant;
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
import de.sfk.spicycurry.data.Feature;
import de.sfk.spicycurry.data.FeatureStore;
import de.sfk.spicycurry.data.IPersistor;
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
    
    // store of chores
 	private List<Chore> chores = new ArrayList<Chore>();
	private IPersistor persistor = Globals.Persistor;
	
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
	public List<Chore> getChores() {
		return chores;
	}
	/**
	 * @param chores the chores to set
	 */
	public void setChores(List<Chore> chores) {
		this.chores = chores;
	}
    @Override
    public synchronized void start() {
        this.setDaemon(isDaemon);
        // CmdLine.startEmbeddedDBServer(cmd); -> should be started
        super.start();
    }
    /**
	 * load the chores
	 */
	private void loadChores() {

		try {
			Query aQueryChores =
					persistor.getEm().createQuery("select f from ServerChores f");
			
			chores = aQueryChores.getResultList();
	        
		} catch (Exception e) {
			 logger.info(e.getLocalizedMessage());
			 return;
		} 
	        
	    logger.info(chores.size() + " chore entries loaded");
	}
    /**
     * initialize the server
     * @return true if successfull
     */
    private boolean init() {
    	
    	//final String orgName = Thread.currentThread().getName();
        Thread.currentThread().setName("CurryServer");
        
        // load the chores
        this.loadChores();
        // default chores
        if (chores.size()<=3){
        	chores.add(new Chore("feed features from polarion", Chore.JobType.Update, Duration.ofHours(1), new String[] { Feature.class.getName(), "polarion" }));
        	chores.add(new Chore("feed requirement from polarion", Chore.JobType.Update, Duration.ofHours(4), new String[] { Requirement.class.getName(), "polarion" }));
        	chores.add(new Chore("feed specifications from polarion", Chore.JobType.Update, Duration.ofHours(4), new String[] { Specification.class.getName(), "polarion" }));
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
    			 for(Chore aChore: chores){
    					 if (aChore.run(changeDate)) logger.info(aChore.getDescription() + " run with success");
    					 else logger.info(aChore.getDescription() + " failed while running");
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
