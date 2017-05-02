/**
 * Helper class to import work items from polarion
 * 
 * @author boris.schneider
 */

package de.sfk.spicycurry.data;



import java.io.Closeable;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.xml.rpc.ServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.polarion.alm.ws.client.WebServiceFactory;
import com.polarion.alm.ws.client.session.SessionWebService;
import com.polarion.alm.ws.client.tracker.TrackerWebService;
import com.polarion.alm.ws.client.types.Text;
import com.polarion.alm.ws.client.types.projects.User;
import com.polarion.alm.ws.client.types.tracker.Category;
import com.polarion.alm.ws.client.types.tracker.Comment;
import com.polarion.alm.ws.client.types.tracker.Custom;
import com.polarion.alm.ws.client.types.tracker.CustomField;
import com.polarion.alm.ws.client.types.tracker.EnumOptionId;
import com.polarion.alm.ws.client.types.tracker.LinkedWorkItem;
import com.polarion.alm.ws.client.types.tracker.TimePoint;
import com.polarion.alm.ws.client.types.tracker.WorkItem;
import com.polarion.alm.ws.client.types.tracker.Attachment;


public class WorkItemPolarionLoader implements Closeable {
	
		// singleton
		public static WorkItemPolarionLoader singleton = new WorkItemPolarionLoader();
	
		// state
		private boolean isInitialized = false;
		
		// WebService per Polarion connection
		private WebServiceFactory factory = null;
		private SessionWebService sessionService;
		private TrackerWebService trackerService;
		
		// field to be returned from Polarion
		private String[] fields = null;
		
		// fields for time conversion
		public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z";
		public static final String GERMAN_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
		public static final String ISO_TIMEPOINT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
		
		private static SimpleDateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		private static SimpleDateFormat germanDateFormatter = new SimpleDateFormat(GERMAN_DATE_FORMAT, Locale.GERMANY);
		private static SimpleDateFormat isoTimePointFormatter = new SimpleDateFormat(ISO_TIMEPOINT_FORMAT);
		
		// Logger
		private Logger logger = LogManager.getLogger(WorkItemPolarionLoader.class);
		/**
		 * constructor with no beginSession
		 */
		public WorkItemPolarionLoader() {
			
		}
		/**
		 * constructor with begin session
		 * 
		 * @param url
		 * @param name
		 * @param password
		 * @throws Exception
		 */
		public WorkItemPolarionLoader(String url,String name,String password) throws Exception {
			beginSession(url, name, password);
		}

		protected void beginSession(String url, String name, String password) throws Exception {
			try {
				
				factory = new WebServiceFactory(url);
				
				// setup Web Service
				sessionService = factory.getSessionService();
				trackerService = factory.getTrackerService();
				
				sessionService.logIn(name, password);
				this.isInitialized = true;
				logger.info("Session with '" + url + "started");
			
			} catch (MalformedURLException me) {
				logger.error("Provided URL is malformed - protocol unknown.", me);
				throw me;
			} catch (ServiceException se) {
				logger.error("Unreachable web services at Polarion server.", se);
				throw se;
			} catch (RemoteException re) {
				logger.error("Log in unsuccessful", re);
				throw re;
			} catch (Exception ioe) {
				logger.error("Properties not loaded.", ioe);
				throw ioe;
			}
	    }
		/**
		 * set array of fields to be transmitted by Polarion
		 * @param fields
		 */
		public void setFields(String[] fields) {
			this.fields = fields;
		}
		/**
		 * get workitems from Polarion by query and sort
		 * 
		 * @param query
		 * @param sort
		 * @return
		 * @throws Exception
		 */
		public WorkItem[] getWorkItemsByQuery(String query,String sort) throws Exception {
			return getWorkItemsByQuery(query, sort, 0);
		}
		/**
		 * internal query
		 * 
		 * @param query
		 * @param sort
		 * @param retry
		 * @return
		 * @throws Exception
		 */
		private WorkItem[] getWorkItemsByQuery(String query,String sort, int retry) throws Exception {
			
			WorkItem[] items = null;
			try {
				items = trackerService.queryWorkItems(query, sort, fields);
				
			} catch (Exception e) {
				retry++;
				
				if (retry<3){
					logger.debug("Error ("+retry+") in getWorkItemsByQuery ["+e+"].", e);
					return getWorkItemsByQuery(query, sort, retry);
					
				} else {
					logger.error("Error in getWorkItemsByQuery ["+e+"].", e);
					e.printStackTrace();
					throw e;
				}
			}

			if (items==null)
				items= new WorkItem[0];
			
			logger.info("query '" + query + "' resulting " + items.length + " work items retrieved.");
			return items;
		}
		/**
		 * internal query
		 * 
		 * @param query
		 * @param sort
		 * @param retry
		 * @return
		 * @throws Exception
		 */
		public String[] getWorkItemsUriByQuery(String query,String sort) throws Exception {
			
			String[] theUris = null;
			try {
				theUris = trackerService.queryWorkItemUris(query, sort);
				
			} catch (Exception e) {
					logger.error("Error in getWorkItemsByQuery ["+e+"].", e);
					if (logger.isDebugEnabled()){ 
						e.printStackTrace();
						throw e;
					}
			}

			if (theUris==null)
				theUris= new String[0];
			
			logger.info("query '" + query + "' resulting " + theUris.length + " uris retrieved.");
			return theUris;
		}
		/**
		 * Gets the simple value performing these transformation:
		 * <ul><li>EnumOption -> String</li>
		 * <li>User -> String</li>
		 * <li>User[] -> String</li>
		 * <li>Text -> String</li>
		 * <li>Comment -> String</li>
		 * <li>GregorianCalendar -> String</li>
		 * <li>Categories -> String</li>
		 * <li>TimePoint -> String</li></ul>
		 * 
		 * @param value the value
		 * 
		 * @return the simple value
		 */
		public static String getSimpleValue(Object value ) {
			String toReturn = null;
			
			// EnumOption -> String
			if (value instanceof EnumOptionId) {
				value = ((EnumOptionId) value).getId();
			
			// User -> String
			} else if (value instanceof User) {
				value = ((User) value).getName();
				
			// User[] -> String
			} else if (value instanceof User[]) {
				String temp = "";
				for (int i = 0; i<((User[]) value).length; i++) {
					temp += ((User[]) value)[i].getName();
				}
				value = temp;
				
	    	// Text
			} else if (value instanceof Text) {
				value = ((Text) value).getContent();
				
			// Comment
			} else if (value instanceof Comment) {
				if (((Comment) value).getText()==null)
					return null;
				value = ((Comment) value).getText().getContent();
	    		
			// Data
			} else if (value instanceof Date || value instanceof GregorianCalendar) {
				if (value instanceof GregorianCalendar)
					value=((GregorianCalendar)value).getTime();
				else
					value = ((Date)value);
				value = dateFormatter.format(value);
				
			// Categories
			} else if (value instanceof Category[]) {
				Category[] cat = (Category[])value;
				String temp = "";
				for (int i=0; i<cat.length; i++) {
					temp += cat[i].getName();
				}
				value = temp;
			
			// TimePoint
			} else if (value instanceof TimePoint) {
				value = ((TimePoint) value).getName();
			}
			
			if (value != null){
				toReturn = value.toString();
			}
			
			return toReturn;
		}

		/**
		 * return true if the fieldname is a customfield
		 * 
		 * @param fieldName
		 * @param workItem
		 * @return
		 * @throws Exception
		 */
		protected boolean isCustomField(String fieldName, WorkItem workItem) throws Exception {
			return isCustomField(fieldName, workItem, 0);
		}
		/**
		 * internal method for determining custom fields
		 * 
		 * @param fieldName
		 * @param workItem
		 * @param retry
		 * @return
		 * @throws Exception
		 */
		private boolean isCustomField(String fieldName, WorkItem workItem, int retry) throws Exception {
			boolean custom = false;
			
			try {
				String[] customFields = trackerService.getCustomFieldKeys(workItem.getUri());
				for (int i = 0; i < customFields.length && !custom; i++) {
					if (fieldName.equalsIgnoreCase(customFields[i])) {
						custom = true;
					}
				}
			
			} catch (RemoteException e) {
				retry++;
				
				if (retry<3){
					logger.debug("Errore ("+retry+") nel isCustomField per il campo "+fieldName+" ["+e+"].");
					return isCustomField(fieldName, workItem, retry);
					
				} else {
					logger.error("Errore nel isCustomField per il campo "+fieldName+" ["+e+"].");
					e.printStackTrace();
					throw e;
				}
			}
			
			return custom;
		}
		/**
		 * returns a custom field of the workitem
		 * 
		 * @param workItemUri
		 * @param fieldName
		 * @return CustomField
		 * @throws Exception
		 */
		protected CustomField getCustomField(String workItemUri, String fieldName) throws Exception {
			return getCustomField(workItemUri, fieldName, 0);
		}
		/**
		 * internal method for returning a custom field
		 * 
		 * @param workItemUri
		 * @param field
		 * @param retry
		 * @return
		 * @throws Exception
		 */
		private CustomField getCustomField(String workItemUri, String field, int retry) throws Exception {
			CustomField customField = null;
			
			try {
				customField = trackerService.getCustomField(workItemUri, field);
				
			} catch (Exception e) {
				retry++;
				
				if (retry<3){
					logger.debug("Error ("+retry+") in getCustomField for field "+field+" ["+e+"].");
					return getCustomField( workItemUri, field, retry);
					
				} else {
					logger.error("Error in getCustomField for field "+field+" ["+e+"].");
					if (logger.isDebugEnabled()) {
						e.printStackTrace();
					}
					throw e;
				}
			}
			
			return customField;
		}
		/**
		 * returns a List of derived work items by type and project filtered
		 * 
		 * @param workItem
		 * @param type
		 * @param project
		 * @return
		 */
		public List<WorkItem> getDerived(WorkItem workItem, String type, String project) {
			List<WorkItem> children = null;
			
			String fatherId = workItem.getId();
			children = new ArrayList<WorkItem>();
			
			String sort = "id";
			String query = "linkedWorkItems:"+fatherId+" AND project.id:"+project;
			if (type!=null)
				query += " AND type:"+type;
			
			try {
				WorkItem[] linkedWorkItems = getWorkItemsByQuery(query, sort);
				
				if (linkedWorkItems!=null) {
					for (int i = 0; i<linkedWorkItems.length; i++) {
						children.add(linkedWorkItems[i]);
					}
				}
			
			} catch (Exception e) {
				logger.error("error in getting children of  ["+workItem.getId()+"]! " + e);
			}
				
			return children;
		}
		/**
		 * return type of WorkItem
		 * @param workItem
		 * @return type of workitem as string
		 */
		public String getWorkItemType(WorkItem workItem) {
			String type = null;
			try {
				type = workItem.getType().getId();
			} catch (Exception e) {
				logger.info("unpossible to determine type of workitem ["+workItem.getId()+"]! ");
				if (logger.isDebugEnabled()) {
					e.printStackTrace();
				}
			}
			return type;
		}	
		/**
		 * get parents
		 * 
		 * @param workItem
		 * @param type
		 * @param project
		 * @return list of workitem
		 */
	    public List<WorkItem> getFather(WorkItem workItem, String type, String project) {
			
			List<WorkItem> fathers = new ArrayList<WorkItem>();
			String childId = workItem.getId();
			
			String sort = "id";
			String query = "backlinkedWorkItems:"+childId+" AND project.id:"+project;
			if (type != null)
				query += " AND type:"+type;
			
			try {
				WorkItem[] backlinkedWorkItems = getWorkItemsByQuery(query, sort);
				
				if (backlinkedWorkItems!=null) {
			    	for (int i = 0; i<backlinkedWorkItems.length; i++) {
			    		fathers.add(backlinkedWorkItems[i]);
			    	}
		    	}
				
			} catch (Exception e) {
				logger.error("error in getting father of  ["+workItem.getId()+"]! " + e);
			}
			
			return fathers;
		}
	    
		/**
		 * close session with polarion
		 */
		public void close() {
			
			try {
				sessionService.endSession();
			} catch (RemoteException e) {
				if (logger.isDebugEnabled()) e.printStackTrace();
			}
		}
		
		/**
		 * returns aRequirement from a item with the 
		 * @param workItem
		 * @param requirement of an existing requirment or null for new feature
		 * @return a requirement object
		 */
		public Requirement convertToRequirement(WorkItem item, Requirement requirement){
		
			if (requirement == null) requirement = new Requirement(item.getId());
			
			// get all Default fields
			
			if (item.getDescription() != null) requirement.setDescription(getSimpleValue(item.getDescription()));
			else requirement.setDescription("");
			
			requirement.setTitle(item.getTitle());
			requirement.setPolarionUri(item.getUri());
			requirement.setCreatedOn(item.getCreated());
			requirement.setUpdatedOn(item.getCreated());
			requirement.setAuthor(item.getAuthor().getId());
			requirement.setStatus(item.getStatus().getId());
			
			// Hack
			if (item.getAssignee().length >0) requirement.setAssignee(item.getAssignee()[0].getName());
			
			// get Attachments 
			if (item.getAttachments() != null)
				for (com.polarion.alm.ws.client.types.tracker.Attachment anAttachment: item.getAttachments()){
					requirement.addAttachment(anAttachment.getId(), anAttachment.getUrl(), anAttachment.getFileName(), anAttachment.getUri());
				}
			
			// get all links
			if (item.getLinkedWorkItemsDerived() != null)
				for (LinkedWorkItem anItem: item.getLinkedWorkItemsDerived()){
					requirement.addDerivedPolarionURI(anItem.getWorkItemURI());
				}
			if (item.getLinkedWorkItems() != null)
				for (LinkedWorkItem anItem: item.getLinkedWorkItems()){
					requirement.addLinkedPolarionURI(anItem.getWorkItemURI());
				}
			// copy all custom fields
			for (Custom aCustomField : item.getCustomFields()){
				
				if (aCustomField.getKey().equalsIgnoreCase("idKA"))
					requirement.setCustomerRequirementId(aCustomField.getValue().toString());
				
				if (aCustomField.getKey().equalsIgnoreCase("titleKA"))
					requirement.setCustomerRequirementTitle(aCustomField.getValue().toString());
				
				if (aCustomField.getKey().equalsIgnoreCase("customerType"))
					requirement.setCustomerType(aCustomField.getValue().toString());
				
				if (aCustomField.getKey().equalsIgnoreCase("sourceModule"))
					requirement.setSourceModule(aCustomField.getValue().toString());
				
				if (aCustomField.getKey().equalsIgnoreCase("identification"))
					requirement.setSourceID(aCustomField.getValue().toString());
				
				if ((aCustomField.getKey().equalsIgnoreCase("descKA")) || (aCustomField.getKey().equalsIgnoreCase("rif_Specific_1st-Tier")))
					if (requirement.getDescription()!= null)
						requirement.setDescription(requirement.getDescription() + "\n" + (String) ((Text) aCustomField.getValue()).getContent());
					else requirement.setDescription((String) ((Text) aCustomField.getValue()).getContent());
				
				if (aCustomField.getKey().equalsIgnoreCase("OPL_Responsible_1st-Tier")){
					if (getSimpleValue(aCustomField.getValue()).equalsIgnoreCase("rif_responsible")) 
							requirement.setResponsible(true);
				}
				
				if (aCustomField.getKey().equalsIgnoreCase("rif_OPL_Status_OEM_1st-Tier")){
					if (getSimpleValue(aCustomField.getValue()).equalsIgnoreCase("rif_accepted")) 
							requirement.setAccepted(true);
				}
				
				if (aCustomField.getKey().equalsIgnoreCase("kategorien"))
					requirement.setCategory(getSimpleValue(aCustomField.getValue()));
				
				if (aCustomField.getKey().equalsIgnoreCase("customreqformKA"))
					requirement.setCustomerReqType((getSimpleValue(aCustomField.getValue())));
				
				if (aCustomField.getKey().equalsIgnoreCase("updateType"))
					if (aCustomField.getValue()!= null) 
						requirement.setUpdateType((getSimpleValue(aCustomField.getValue())));
				
				if (aCustomField.getKey().equalsIgnoreCase("rif_lastUpdateKA")){
					Calendar aCal = Calendar.getInstance();
					String aString = (String) aCustomField.getValue();
					
					try {
						if (aString.indexOf('T')>= 0) {
							aCal.setTime(isoTimePointFormatter.parse(aString));
						}else aCal.setTime(germanDateFormatter.parse(aString));
						requirement.setCustomerUpdatedOn(aCal);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						logger.error("time not convertable " + aString);
						if (logger.isDebugEnabled()) e.printStackTrace();
					}
					
				}
					
				
				if (aCustomField.getKey().equalsIgnoreCase("validityKA"))
					requirement.setCustomerStatus(getSimpleValue(aCustomField.getValue()));
				
				if (aCustomField.getKey().equalsIgnoreCase("brand"))
					for(EnumOptionId anID: (EnumOptionId [])aCustomField.getValue())
						requirement.addBrand(anID.getId());
				
				if (aCustomField.getKey().equalsIgnoreCase("market"))
					for(EnumOptionId anID: (EnumOptionId [])aCustomField.getValue())
						requirement.addMarket(anID.getId());
				
				if (aCustomField.getKey().equalsIgnoreCase("brandOEM"))
					for(EnumOptionId anID: (EnumOptionId [])aCustomField.getValue())
						requirement.addCustomerBrand(anID.getId());
				
				if (aCustomField.getKey().equalsIgnoreCase("marketOEM"))
					for(EnumOptionId anID: (EnumOptionId [])aCustomField.getValue())
						requirement.addCustomerMarket(anID.getId());
			}
			
			
			
			// return
			return requirement;
		}
		
		/**
		 * convertToFeature
		 * 
		 * @param item workitem
		 * @param feature to update reused or NULL
		 * @return updated or new feature
		 */
		public Feature convertToFeature(WorkItem item, Feature feature){
			Requirement requirement = null;
			// use existing requirment to create feature
			if (feature != null) 
				requirement = feature.getRequirement();
			// convert
			requirement = this.convertToRequirement(item, requirement);
			return new Feature(requirement);
		}

		/**
		 * retrieves a Work Item from Polarion by URI
		 * 
		 * @param polarionUri
		 * @return work item
		 */
		public WorkItem getWorkItemByUri(String uri) {
			try {
				return this.trackerService.getWorkItemByUri(uri);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		/**
		 * retrieves a Work Item from Polarion by id
		 * 
		 * @param id
		 * @return work item
		 */
		public WorkItem getWorkItemById(String project, String id) {
			try {
				return this.trackerService.getWorkItemById(project, id);
			} catch (RemoteException e) {
				if (logger.isDebugEnabled()) e.printStackTrace();
				return null;
			}
		}
		/**
		 * @return the isInitialized
		 */
		public boolean isInitialized() {
			return isInitialized;
		}
		/**
		 * @param isInitialized the isInitialized to set
		 */
		public void setInitialized(boolean isInitialized) {
			this.isInitialized = isInitialized;
		}

	
}
