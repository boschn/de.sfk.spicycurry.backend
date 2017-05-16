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

import net.rcarz.jiraclient.JiraClient;

import java.net.URI;
/**
 * Jira loader per REST-API
 * 
 * @author boris.schneider
 *
 */
public class JiraIssuesLoader implements Closeable {
			// singleton
			public static JiraIssuesLoader singleton = new JiraIssuesLoader();
			
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
			private Logger logger = LogManager.getLogger(JiraIssuesLoader.class);
			/**
			 * constructor with no beginSession
			 */
			public JiraIssuesLoader() {
				
			}
			/**
			 * constructor with begin session
			 * 
			 * @param url
			 * @param name
			 * @param password
			 * @throws Exception
			 */
			public JiraIssuesLoader(String url,String name,String password) throws Exception {
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

					
		    }
			
			/**
			 * close session
			 */
			public void close() {
					
			}
			
			
	
}
