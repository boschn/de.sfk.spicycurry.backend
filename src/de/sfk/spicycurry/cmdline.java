/**
 * 
 */
package de.sfk.spicycurry;

import de.sfk.spicycurry.data.DataRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * command line interface and runner
 * @author boris.schneider
 *
 */
public class cmdline {

	private static Logger logger = LogManager.getRootLogger();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		logger.trace("Configuration File Defined To Be :: "+System.getProperty("log4j.configurationFile"));
    
		// TODO Auto-generated method stub
		DataRunner.run(args);
		
	}

}
