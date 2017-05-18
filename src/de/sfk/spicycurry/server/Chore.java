/**
 * 
 */
package de.sfk.spicycurry.server;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sfk.spicycurry.CurryDaemon;
import de.sfk.spicycurry.Globals;
import de.sfk.spicycurry.data.Bean;
import de.sfk.spicycurry.data.Feature;
import de.sfk.spicycurry.data.FeatureStore;
import de.sfk.spicycurry.data.Requirement;
import de.sfk.spicycurry.data.RequirementStore;
import de.sfk.spicycurry.data.Specification;
import de.sfk.spicycurry.data.SpecificationStore;

/**
 * define the tasks which the server does
 * @author boris.schneider
 *
 */
@Entity(name="ServerChores")
public class Chore extends Bean{
	
	// job
	public enum JobType {
		Nothing,
		Update,
		FullFeed
	}
	
	// status
	public enum StatusType {
		Failed,
		Success,
		Enqueued
	}
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id 
	private Long Id;
	
	// jobType what to do
	@Column(nullable=true, length=2048)
	private String description = "";
		
	// jobType what to do
	@Column
	private JobType job = JobType.Nothing;
	
	// Arguments of the job type
	@ElementCollection
	private ArrayList<String> arguments = new ArrayList<String>();
	
	// period of the intervall until we do it again
	@Column
	private Duration intervallPeriod = Duration.ofHours(12);
	
	// when executed
	@Column(nullable=true)
	private Timestamp lastExecuted;
	
	// jobType what to do
	@Column(nullable=true, length=2048)
	private String lastLog = "";
	
	// last status
	@Column
	private StatusType lastStatus = StatusType.Enqueued;
	
	@Version
	private Timestamp lastUpdate;

	// logger
	@Transient
	private static Logger logger = LogManager.getLogger(Chore.class);
	
	@Transient
	private boolean isRunning = false;
	
	/**
	 * ctor
	 */
	public Chore()
	{
		super(Globals.Persistor);
	}
	/**
	 * ctor
	 * @param description
	 * @param job
	 * @param arguments
	 * @param intervallPeriod
	 */
	public Chore(Long id, String description, JobType job, Duration intervallPeriod, String args[] ) {
		super(Globals.Persistor);
		this.Id = id;
		this.description = description;
		this.job = job;
		this.intervallPeriod = intervallPeriod;
		// add the arguments
		for (String argument : args){
			arguments.add(argument);
		}
	}

	/**
	 * @return the lastExecuted
	 */
	public Timestamp getLastExecuted() {
		return lastExecuted;
	}

	public boolean isRunning() {
		return isRunning;
	}
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	/**
	 * @param lastExecuted the lastExecuted to set
	 */
	public void setLastExecuted(Timestamp lastExecuted) {
		this.lastExecuted = lastExecuted;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return Id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the job
	 */
	public JobType getJob() {
		return job;
	}

	/**
	 * @return the arguments
	 */
	public ArrayList<String> getArguments() {
		return arguments;
	}

	/**
	 * @return the intervallPeriod
	 */
	public Duration getIntervallPeriod() {
		return intervallPeriod;
	}

	/**
	 * @return the lastLog
	 */
	public String getLastLog() {
		return lastLog;
	}

	/**
	 * @param id the id to set
	 */
	private void setId(Long id) {
		Id = id;
		setChanged(true);
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
		setChanged(true);
	}

	/**
	 * @param job the job to set
	 */
	public void setJob(JobType job) {
		this.job = job;
		setChanged(true);
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(ArrayList<String> arguments) {
		this.arguments = arguments;
		setChanged(true);
	}

	/**
	 * @param intervallPeriod the intervallPeriod to set
	 */
	public void setIntervallPeriod(Duration intervallPeriod) {
		this.intervallPeriod = intervallPeriod;
		setChanged(true);
	}

	/**
	 * @param lastLog the lastLog to set
	 */
	public void setLastLog(String lastLog) {
		this.lastLog = lastLog;
		setChanged(true);
	}

	/**
	 * @return the lastStatus
	 */
	public StatusType getLastStatus() {
		return lastStatus;
	}

	/**
	 * @param lastStatus the lastStatus to set
	 */
	public void setLastStatus(StatusType lastStatus) {
		this.lastStatus = lastStatus;
		setChanged(true);
	}
	/**
	 * run an update of all the objects specified
	 * @param lastChangeReferenceDate : if not null incremental update
	 * @return true on success
	 */
	public synchronized boolean runUpdate(Temporal lastChangeReferenceDate){
		
		try {
			String ClazzName = this.getArguments().get(0);
			String ForeignStore = this.getArguments().get(1);
			this.lastLog = "";
			
			// check on the object
			if (ClazzName.toUpperCase().contains(Requirement.class.getName().toUpperCase())) {
				if (RequirementStore.db.loadAllPolarion(lastChangeReferenceDate)){
 				   RequirementStore.db.persist();
					return true;
				}else {
					this.lastLog = "polarion load failed - see log file";
					return false;
				}
			}else
			if (ClazzName.toUpperCase().contains(Specification.class.getName().toUpperCase())) {
				if (SpecificationStore.db.loadAllPolarion(lastChangeReferenceDate)) {
						// start persisting it all
						SpecificationStore.db.persist();
				        this.lastLog = "polarion load succeeded";
						return true;
				}else {
					this.lastLog = "polarion load failed - see log file";
					return false;
				}
			}else
			if (ClazzName.toUpperCase().contains(Feature.class.getName().toUpperCase())) {
				if (ForeignStore.toUpperCase().contains("POLARION")){
					if (FeatureStore.db.loadAllPolarion(lastChangeReferenceDate)){
						// start persisting it all
						FeatureStore.db.persist();
						this.lastLog = "polarion load succeeded";
						return true;
					}else {
						this.lastLog = "polarion load failed - see log file";
						return false;
					}
				}else if (ForeignStore.toUpperCase().contains("JIRA")){
					if (JiraIssuesStore.db.loadAllJira(lastChangeReferenceDate)){
						// start persisting it all
						FeatureStore.db.persist();
						this.lastLog = "polarion load succeeded";
						return true;
					}else {
						this.lastLog = "polarion load failed - see log file";
						return false;
					}
				}else {
					this.lastLog = "other datasource not supported";
					return false;
				}
			}
				
			this.lastLog = "could not determine the data objects to update";
			return false;
			
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			this.lastLog =  e.getLocalizedMessage() + "\n";
			if (logger.isDebugEnabled()) {
				logger.catching(e);
				this.lastLog = lastLog + e.getStackTrace() + "\n";
			}
			return false;
		}
		
		
	}
	/**
	 * run the chore
	 * @return true if run
	 */
	public synchronized boolean run(Temporal newRunTimestamp) {
		Temporal changeReferenceDate = null;
		boolean result = false;
		try {
			if (newRunTimestamp == null) newRunTimestamp = Instant.now();
			
			// check if the chore is due
			if (this.getLastExecuted() != null) {
				// set the incremental change date to last
				changeReferenceDate = (Temporal) this.getLastExecuted().toInstant();
				// check 
				Duration passed = Duration.between(changeReferenceDate, Instant.now());
				// check if we are due
				if (passed.toMinutes() < this.getIntervallPeriod().toMinutes()) return false;
				
			}
			
			this.setRunning(true);
			
			// run the command
			switch (this.getJob()){
				case Nothing:
					result = true;
					break;
				case Update:
					// allowed to be null -> full feed
					result = runUpdate(changeReferenceDate);
					break;
				case FullFeed:
					// allowed to be null -> full feed
					result = runUpdate(null);
					break;
				}
			
		}catch(Exception e){

			logger.catching(e);
			this.setLastLog(getLastLog() + "\n" + e.getLocalizedMessage());
		}
		
		this.setRunning(false);
		
		// write the result
		this.setLastExecuted(Timestamp.from((Instant)newRunTimestamp));
		
		if (result){
			this.setLastStatus(StatusType.Success);
			logger.info("chore done with success");
		} else {
			this.setLastStatus(StatusType.Failed);
			logger.info("chore failed");
		}
		
		// persist
		this.persist();
		
		return true;
	}
	
}
