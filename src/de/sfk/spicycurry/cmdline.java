/**
 * 
 */
package de.sfk.spicycurry;

import de.sfk.spicycurry.data.DataRunner;
import de.sfk.spicycurry.data.Setting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.cli.*;

/**
 * command line interface and runner
 * 
 * @author boris.schneider
 *
 */
public class cmdline {

	
	
	// commands of the command line
	public static final String cmdFeed = "f";
	public static final String cmdTest = "t";
	public static final String cmdHelp = "h";
	public static final String cmdProperty = "p";
	
	private static final String cmdOptionChangedSinceDate = "changedSinceDate";
	private static final String ArgumentPropertyFileName = "filename";
	// log
	private static Logger logger = LogManager.getLogger();
	
	/**
	 * main function to run on the command line
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean aRunflag = false;
		
		System.out.println("A Spicy Curry Data Back End V.01\n");
		
		// create the options
		Options theOptions = createOptions();
		// setup the default parser
		CommandLineParser parser = new DefaultParser();
		
		// parse
		
		try {
			
			CommandLine cmd = parser.parse(theOptions, args);
						
			// help
			if (cmd.hasOption(cmdline.cmdHelp)){
				help(theOptions);
				aRunflag = true;
			}
			// command property file
			if (cmd.hasOption(cmdProperty)){
				if (cmd.getOptionValue(cmdline.cmdProperty) !=null){
					String filename = cmd.getOptionValue(cmdline.cmdProperty);
					Setting.Default.read(filename);
				} else Setting.Default.read(null);
			}
			// command feed
			if (cmd.hasOption(cmdline.cmdFeed)) {
				Date aChangedDate = null;
				if (cmd.getOptionValue(cmdFeed)!=null){
					SimpleDateFormat aFormatter = new SimpleDateFormat("yyyy-MM-dd");
					try{
					     aChangedDate = aFormatter.parse(cmd.getOptionValue(cmdFeed));
					} catch (java.text.ParseException e) {
						logger.error("failed to interprete date '" + cmd.getOptionValue(cmdFeed)+ "': " 
									 + e.getLocalizedMessage());
						System.exit(0);
					}
				}
				// run even with null date -> full feed
				DataRunner.runFeed(aChangedDate);
				aRunflag = true;
			}
			
			// command test
			if (cmd.hasOption(cmdline.cmdTest)) {
				// run 
				DataRunner.runTest();
				aRunflag = true;
			}

			// default - no commands
			if (!aRunflag){
				System.out.println("no command selected - run -h to see options");
			}
		} catch (ParseException e) {
			
			// TODO Auto-generated catch block
			logger.fatal("failed to parse command line \n", e.getLocalizedMessage() );
			help(theOptions);
		}
		
	}
	
	/**
	 * create the command line options
	 * 
	 * @return the Options
	 */
	public static Options createOptions()
	{
		final Options theOptions = new Options();

		
		// help
		theOptions.addOption(cmdline.cmdHelp, "help", false, "help for command line options");
		
		// properties
		Option commandProperty = Option.builder(cmdline.cmdProperty)
			    .longOpt( "property" )
			    .required(false)
			    .desc( "name and location of the property file"  )
			    .hasArg()
			    .argName(ArgumentPropertyFileName)
			    .build();
		theOptions.addOption(commandProperty);

		// command feed
		Option commandTest = Option.builder(cmdline.cmdTest)
			    .longOpt( "test" )
			    .required(false)
			    .desc( "internal test"  )
			    .build();
		theOptions.addOption(commandTest);

		
		// command feed
		Option commandFeed = Option.builder(cmdline.cmdFeed)
			    .longOpt( "feed" )
			    .required(false)
			    .desc( "feed data from polarion to the data store"  )
			    .numberOfArgs(1)
			    .optionalArg(true)
			    .argName(cmdline.cmdOptionChangedSinceDate)
			    .type(Date.class)
			    .required(false)
			    .build();
		theOptions.addOption(commandFeed);
		
		return theOptions;
	}
	
	/**
	 * run the helper and show the output
	 * 
	 * @param options
	 */
	
	public static void help(Options options)
	{
		// This prints out some help
		  HelpFormatter formater = new HelpFormatter();
		  formater.printHelp("run", options);
		  System.exit(0);

	}

}
