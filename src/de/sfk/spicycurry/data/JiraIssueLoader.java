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
import net.rcarz.jiraclient.Comment;
import net.rcarz.jiraclient.CustomFieldOption;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

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
					int no = client.countIssues(query);
					
					logger.info("query '" + query + "' will return " + no + " issues.");
					System.out.println();
					
					// load with width 50
					for (int at=0; at < no; at+=50){
						List<Issue> theResult;
						theResult = client.searchIssues(query,null,null, no+50,at).issues;
						
						// add them
						for (Issue anIssue: theResult){
							theIssues.add(anIssue);
						}
						System.out.print(".");
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
			 * convert a date in the form yyyy'kw'ww to a proper calendar date
			 * @param week
			 * @throws ParseException 
			 */
			public static Calendar ConvertWeek2Date(String week) throws ParseException {
				if (week == null) return null;
				
				SimpleDateFormat sdf;
				if (week.toLowerCase().contains("cw")) sdf = new SimpleDateFormat("yyyy'cw'ww");
				else if(week.toLowerCase().contains("kw")) sdf = new SimpleDateFormat("yyyy'kw'ww");
				else return null;
				
				sdf.parse(week.toLowerCase());
				
				Calendar newCalendar = Calendar.getInstance();
				newCalendar.set(Calendar.YEAR, sdf.getCalendar().get(Calendar.YEAR));
				newCalendar.set(Calendar.WEEK_OF_YEAR, sdf.getCalendar().get(Calendar.WEEK_OF_YEAR));        
				newCalendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
				newCalendar.set(Calendar.HOUR, 23);
				newCalendar.set(Calendar.MINUTE, 59);
				newCalendar.set(Calendar.SECOND, 59);
				
				return newCalendar;
			}
			/**
			 * convert an jira plain issue to an JiraIssue object
			 * @param item
			 * @param anIssue
			 * @return
			 */
			public JiraIssue convertToIssue(Issue item, JiraIssue anIssue){

				anIssue.setSummary(item.getSummary());
				anIssue.setDescription(item.getDescription());
			
				anIssue.setCreatedOn((Calendar) getSimpleValue(item.getCreatedDate()));
				anIssue.setUpdatedOn((Calendar) getSimpleValue(item.getUpdatedDate()));
				anIssue.setDueDate((Calendar) getSimpleValue(item.getDueDate()));
				
				anIssue.setLabels(item.getLabels());
				
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
				
					
				if (item.getComments()!= null)
					for(Comment aComment: item.getComments()){
						anIssue.addComment(new JiraIssueComment(
						aComment.getAuthor().getDisplayName(),
						aComment.getBody().toString(),
						aComment.getCreatedDate()));
					}
				
				return anIssue;
			}
			/**
			 * makes simple values out of JiraClient customfield input
			 * 
			 * @param input
			 * @return object of ArrayList<String>, String, Calendar
			 */
			@SuppressWarnings("unchecked")
			public synchronized Object getSimpleValue(Object input){
				
				// if null
				if (input==null) return null;
				
				// json null ??
				if (input instanceof JSONNull) return null;
				
				// if date
				if(input instanceof Date) {
					Calendar aDate = new GregorianCalendar();
					aDate.setTime((Date) input);
					return aDate;
				}
				// if user
				if (input instanceof User) 
					return ((User) input).getDisplayName();
				
				// json object of some sort
				if (input instanceof JSONObject){
					return Field.getString((JSONObject)input);
				}
				// check if we have a list of CustomFieldOption
				if (input instanceof JSONArray){
					List<CustomFieldOption> theOptions;
					theOptions = Field.getResourceArray(CustomFieldOption.class,input,client.getRestClient());
					List<String> out = new ArrayList<String>();
					for (CustomFieldOption anOption: theOptions) 
						out.add(anOption.getValue());
					return out;
				}
				if (input.toString() != null) return input.toString();
				
				// not determined
				return null;
			}
			/**
			 * convert a plain Jira issue to an JiraIssueFeature (which is the issue ticket for the feature)
			 * 
			 * @param item
			 * @param aFeature
			 * @return the Jira IssueFeature
			 */
			@SuppressWarnings("unchecked")
			public JiraIssueFeature convertToIssueFeature(Issue item, JiraIssueFeature aFeature) {
				if (aFeature==null) aFeature = new JiraIssueFeature(item.getKey());
				
				convertToIssue(item, aFeature);

				// feature id
				aFeature.setFeatureId((String)getSimpleValue(item.getField("customfield_10828")));
				// source module
				aFeature.setSourceModule((String)getSimpleValue(item.getField("customfield_10822")));
				// Workgroup
				aFeature.setWorkgroup((String)getSimpleValue(item.getField("customfield_10804")));
			
				// markets
				Object aValue = getSimpleValue(item.getField("customfield_10814"));
				if (aValue != null && aValue instanceof List<?>) aFeature.setMarkets((List<String>) aValue);
				else if(aValue != null) aFeature.setMarket((String) aValue); 
				
				// brands
				aValue = getSimpleValue(item.getField("customfield_11201"));
				if (aValue != null && aValue instanceof List<?>) aFeature.setBrands((List<String>) aValue);
				else if(aValue != null) aFeature.setBrand((String) aValue); 
				

				// completeness
				aFeature.setCompleteness((String)getSimpleValue(item.getField("customfield_10812")));
				// SOP1EU
				try {
					aFeature.setSOP1EU(ConvertWeek2Date((String)getSimpleValue(item.getField("customfield_10820"))));
				} catch (ParseException e) {
					logger.error(e.getLocalizedMessage());
					if (logger.isDebugEnabled()) logger.catching(e);
				}
				// SOP1EUBaseline
				try {
					aFeature.setSOP1EUBaseline(ConvertWeek2Date((String)getSimpleValue(item.getField("customfield_11900"))));
				} catch (ParseException e) {
					logger.error(e.getLocalizedMessage());
					if (logger.isDebugEnabled()) logger.catching(e);
				}
				// SchedulingStatus
				aFeature.setSchedulingStatus((String)getSimpleValue(item.getField("customfield_10815")));
				// Suggested
				try {
					aFeature.setSuggestedDate(ConvertWeek2Date((String)getSimpleValue(item.getField("customfield_11202"))));
				} catch (ParseException e) {
					logger.error(e.getLocalizedMessage());
					if (logger.isDebugEnabled()) logger.catching(e);
				}
				// market type
				aFeature.setMarketType((String)getSimpleValue(item.getField("customfield_10809")));
				// schedule type
				aFeature.setScheduleType((String)getSimpleValue(item.getField("customfield_10810")));
				// schedule type
				aFeature.setSchedulingComments((String)getSimpleValue(item.getField("customfield_10817")));
				
				return aFeature;
			}
	
}
