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
import de.sfk.spicycurry.data.Requirement;
import de.sfk.spicycurry.data.RequirementStore;
import de.sfk.spicycurry.data.Specification;

/**
 * define the tasks which the server does
 * @author boris.schneider
 *
 */
@Entity(name="ServerChores")
public class Chore extends Bean{
	
	// job
	public enum JobType {
		Update,
		Nothing
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
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;
	
	// jobType what to do
	@Column(nullable=true, length=2048)
	private String description = "";
		
	// jobType what to do
	private JobType job = JobType.Nothing;
	
	// Arguments of the job type
	@ElementCollection
	private ArrayList<String> arguments = new ArrayList<String>();
	
	// period of the intervall until we do it again
	private Duration intervallPeriod = Duration.ofHours(12);
	
	// when executed
	@Column(nullable=true)
	private Timestamp lastExecuted;
	
	// jobType what to do
	@Column(nullable=true, length=2048)
	private String lastLog = "";
	
	// last status
	private StatusType lastStatus = StatusType.Enqueued;
	
	@Version
	private Timestamp lastUpdate;

	// logger
	@Transient
	private static Logger logger = LogManager.getLogger(Chore.class);
	
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
	public Chore(String description, JobType job, Duration intervallPeriod, String args[] ) {
		super(Globals.Persistor);
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
	public void setId(Long id) {
		Id = id;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param job the job to set
	 */
	public void setJob(JobType job) {
		this.job = job;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(ArrayList<String> arguments) {
		this.arguments = arguments;
	}

	/**
	 * @param intervallPeriod the intervallPeriod to set
	 */
	public void setIntervallPeriod(Duration intervallPeriod) {
		this.intervallPeriod = intervallPeriod;
	}

	/**
	 * @param lastLog the lastLog to set
	 */
	public void setLastLog(String lastLog) {
		this.lastLog = lastLog;
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
	}
	/**
	 * run an full update of an Object
	 * @return
	 */
	public boolean runUpdate(Temporal changeDate){
		
		try {
			String ClazzName = this.getArguments().get(0);
			String ForeignStore = this.getArguments().get(1);
			this.lastLog = "";
			
			// check on the object
			if (ClazzName.toUpperCase().contains(Requirement.class.getName().toUpperCase())) {
				if (RequirementStore.db.loadAllPolarion(changeDate)){
					return true;
				}else {
					this.lastLog = "polarion load failed - see log file";
					return false;
				}
			}else
			if (ClazzName.toUpperCase().contains(Specification.class.getName().toUpperCase())) {
				if (RequirementStore.db.loadAllPolarion(changeDate)) {
						this.lastLog = "polarion load succeeded";
						return true;
				}else {
					this.lastLog = "polarion load failed - see log file";
					return false;
				}
			}else
			if (ClazzName.toUpperCase().contains(Feature.class.getName().toUpperCase())) {
				if (ForeignStore.toUpperCase().contains("POLARION")){
					if (RequirementStore.db.loadAllPolarion(changeDate)){
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
	public boolean run(Temporal changeDate) {
		
		boolean result = false;
		
		// check if the chore is due
		if (this.getLastExecuted() != null) {
			// set the incremental change date to last
			if (changeDate == null) changeDate = (Temporal) this.getLastExecuted();
			// check 
			Duration passed = Duration.between(changeDate, Instant.now());
			// check if we are due
			if (passed.toMinutes() < this.getIntervallPeriod().toMinutes()) return false;
			
		}
		
		// run the command
		switch (this.getJob()){
			case Nothing:
				result = true;
				break;
			case Update:
				result = runUpdate(changeDate);
				break;
		}
		
		
		// write the result
		this.setLastExecuted(Timestamp.from((Instant)changeDate));
		
		if (result){
			this.setLastStatus(StatusType.Success);
		} else this.setLastStatus(StatusType.Failed);
		
		// persist
		this.persist();
		
		return true;
	}
	
}
