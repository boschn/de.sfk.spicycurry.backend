/**
 * 
 */
package de.sfk.spicycurry.server;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * define the tasks which the server does
 * @author boris.schneider
 *
 */
@Entity(name="ServerChores")
public class Chore {

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

	
	/**
	 * ctor
	 * @param description
	 * @param job
	 * @param arguments
	 * @param intervallPeriod
	 */
	public Chore(String description, JobType job, Duration intervallPeriod, String args[] ) {
		super();
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
	 * run the chore
	 * @return
	 */
	public boolean run() {
		return false;
		// TODO Auto-generated method stub
		
	}
	
}
