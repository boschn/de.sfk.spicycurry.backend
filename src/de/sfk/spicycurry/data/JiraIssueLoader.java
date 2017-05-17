/**
 * Jira Issues Importer
 */
package de.sfk.spicycurry.data;

import java.io.Closeable;
import java.io.IOException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.xml.rpc.ServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.polarion.alm.ws.client.types.tracker.WorkItem;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.CustomFieldOption;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

import java.net.URI;
/**
 * Jira loader per REST-API
 * 
 * @author boris.schneider
 *
 */
public class JiraIssueLoader implements Closeable {
			// singleton
			public static JiraIssueLoader singleton = new JiraIssueLoader();
			
			// state
			private boolean isInitialized = false;
			
			// jira connection
			private JiraClient client = null;

			// fields for time conversion
			public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z";
			public static final String GERMAN_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
			public static final String ISO_TIMEPOINT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
			
			private static SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
			private static SimpleDateFormat germanDateFormatter = new SimpleDateFormat(GERMAN_DATE_FORMAT, Locale.GERMANY);
			private static SimpleDateFormat isoTimePointFormatter = new SimpleDateFormat(ISO_TIMEPOINT_FORMAT);
			
			// Logger
			private Logger logger = LogManager.getLogger(JiraIssueLoader.class);
			/**
			 * constructor with no beginSession
			 */
			public JiraIssueLoader() {
				
			}
			/**
			 * constructor with begin session
			 * 
			 * @param url
			 * @param name
			 * @param password
			 * @throws Exception
			 */
			public JiraIssueLoader(String url,String name,String password) throws Exception {
				beginSession(url, name, password);
			}

			/**
			 * start a jira Session
			 * 
			 * @param url
			 * @param name
			 * @param password
			 * @throws Exception
			 */
			protected void beginSession(String url, String name, String password) throws Exception {
				
				BasicCredentials creds = new BasicCredentials(name, password);
		        client = new JiraClient(url, creds);
		        setInitialized(true);
					
		    }
			
			/**
			 * close session
			 */
			public void close() {
					
				if (client != null) client = null;
			}
			
			/**
			 * query for issues
			 * 
			 * @param query
			 * @return an Array of Issue
			 * @throws Exception
			 */
			public synchronized Issue[] getIssuesByQuery(String query) throws Exception {
				ArrayList<Issue> theIssues = new ArrayList<Issue>();
				
				try {
					for (Issue anIssue: client.searchIssues(query).issues){
						theIssues.add(anIssue);
					}
					
					
				} catch (JiraException e) {
					
					logger.error(e.getLocalizedMessage());
					if (logger.isDebugEnabled()) logger.catching(e);
				}
				
				logger.info("query '" + query + "' resulting " + theIssues.size() + " issues retrieved.");
				return theIssues.toArray(new Issue[0]);
			}
			/**
			 * set initialization
			 * @param flag
			 */
			public void setInitialized(boolean flag){
				this.isInitialized = flag;
			}
			/**
			 * initialized
			 * @return
			 */
			public boolean isInitialized() {
				
				return isInitialized;
			}
			/**
			 * convert an jira plain issue to an JiraIssue object
			 * @param item
			 * @param anIssue
			 * @return
			 */
			public JiraIssue convertToIssue(Issue item, JiraIssue anIssue){
				Calendar aDate = new GregorianCalendar();

				anIssue.setSummary(item.getSummary());
				anIssue.setDescription(item.getDescription());
				 
				aDate.setTime(item.getCreatedDate());
				anIssue.setCreatedOn(aDate);
				
				aDate.setTime(item.getUpdatedDate());
				anIssue.setUpdatedOn(aDate);
				
				anIssue.setLabels(item.getLabels());
				
				if (item.getDueDate()!= null) {
					aDate.setTime(item.getDueDate());
					anIssue.setDueDate(aDate);
				}
				
				if (item.getStatus()!=null)
					anIssue.setStatus(item.getStatus().getName());
				if (item.getUrl()!=null)
					anIssue.setUrl(item.getUrl());
				if (item.getAssignee() != null) 
					anIssue.setAssignee(item.getAssignee().getDisplayName());
				if (item.getParent()!=null)
					anIssue.setParentIssueId(item.getParent().getKey());
				if (item.getSubtasks()!=null)
					for (Issue anSubIssue:item.getSubtasks()){
						anIssue.addSubIssueId(anSubIssue.getKey());
					}
				
					
				
				return anIssue;
			}
			/**
			 * convert a plain Jira issue to an JiraIssueFeature (which is the issue ticket for the feature)
			 * 
			 * @param item
			 * @param aFeature
			 * @return the Jira IssueFeature
			 */
			public JiraIssueFeature convertToIssueFeature(Issue item, JiraIssueFeature aFeature) {
				if (aFeature==null) aFeature = new JiraIssueFeature(item.getKey());
				
				convertToIssue(item, aFeature);
				
				if (item.getField("customfield_10828") != null)
						aFeature.setFeatureId(item.getField("customfield_10828").toString());
				
				if (item.getField("customfield_10822") != null)
					aFeature.setSourceModule(item.getField("customfield_10822").toString());
			
				/* Pretend customfield_5678 is a multi-select box. Print out the selected values. */
	            List<CustomFieldOption> cfselect = Field.getResourceArray(
	                CustomFieldOption.class,
	                item.getField("customfield_5678"),
	                client.getRestClient()
	            );
				return aFeature;
			}
	
}
