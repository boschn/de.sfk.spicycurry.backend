/**
 * 
 */
package de.sfk.spicycurry.data;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author boris.schneider
 *
 */
public class Setting {

	public static final String defaultFileName = "properties.ini";
	
	// singleton
	public static Setting Default = new Setting();
	
	// property store
	private Properties defaultProperties = new Properties();
	private boolean changed = false;
	
	// Logger
	private Logger logger = LogManager.getLogger(Setting.class);
	
	/**
	 * ctor
	 */
	public Setting() {
		super();
		this.read();
	}
	/**
	 * gets aProperty or null
	 * @param property
	 * @return String
	 */
	public Boolean has(String key){
		return defaultProperties.containsKey(key);
	}
	/**
	 * gets aProperty or null
	 * @param property
	 * @return String
	 */
	public String get(String key){
		return defaultProperties.getProperty(key);
	}
	/**
	 * gets aProperty or the defaultvalue (which is stored)
	 * @param property
	 * @return String
	 */
	public String get(String key, String defaultValue){
		if (!defaultProperties.containsKey(key)) set(key,defaultValue);
		return defaultProperties.getProperty(key);
	}
	/**
	 * gets aProperty or null
	 * @param property
	 * @return String
	 */
	public void set(String key, String value){
		if (defaultProperties.containsKey(key) && !value.equals(defaultProperties.getProperty(key))) changed = true;
		defaultProperties.setProperty(key, value);
	}
	/**
	 * write the settings to the filesystem if the properties have been changed
	 * 
	 * @param force true if write any way
	 */
	public void write(boolean force) {
		FileWriter aWriter = null;
		try
		{
			if (changed || force){
			aWriter = new FileWriter(defaultFileName );
			defaultProperties.store(aWriter, "properties for SpicyCurry Java from " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Timestamp(new Date().getTime())));
			logger.info(defaultFileName + " written");
			aWriter.close();
			changed = false;
			}
		}
		catch ( IOException e )
		{
		  logger.debug("writing to " + defaultFileName + "failed :'" + e.getMessage()+"'");
		  e.printStackTrace();
		}
		finally
		{
		  try { aWriter.close(); } catch ( Exception e ) { }
		}
	}
	/**
	 * read the settings from the filesystem
	 * 
	 * @return true if successfull
	 */
	public boolean read(){
		FileReader aReader = null;
		try
		{
		  aReader = new FileReader( defaultFileName );
		  defaultProperties.load( aReader );
		  logger.info(defaultFileName + " read");
		  aReader.close();
		  return true;
		}
		catch ( IOException e )
		{
		  logger.debug("reading from " + defaultFileName + "failed :'" + e.getMessage()+"'");
		  if (logger.isDebugEnabled()){
			  e.printStackTrace();
		  }
		}
		finally
		{
		  try {aReader.close(); } catch ( Exception e ) { }
		}
		return false;
	}
}
