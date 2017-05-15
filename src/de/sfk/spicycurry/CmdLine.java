/**
 * Commandline interface
 */
package de.sfk.spicycurry;

import de.sfk.spicycurry.data.DataRunner;
import de.sfk.spicycurry.data.Globals;
import de.sfk.spicycurry.server.CurryServer;
import de.sfk.spicycurry.server.H2Server;

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
public class CmdLine {

		
	// commands of the command line
	public static final String cmdFeed = "f";
	public static final String cmdTest = "t";
	public static final String cmdHelp = "h";
	public static final String cmdProperty = "p";
	public static final String cmdServer = "s";
	public static final String cmdAutoStartDBServer = "a";
	
	private static final String cmdOptionChangedSinceDate = "changedSinceDate";
	private static final String cmdOptionPropertyFileName = "filename";
	private static final String cmdOptionServerType = "serverType";
	
	// log
	private static Logger logger = LogManager.getLogger();
	// the options
	private static Options cmdLineOptions = null;
	// the parser
	private static CommandLineParser cmdLineParser = null;
	
	/**
	 * Init the context
	 * 
	 * @param args
	 * @return
	 */
	public static boolean init(String[] args){
		// create the options
		createOptions();
		// setup the default parser
		cmdLineParser = new DefaultParser();
	
		return true;
	}
	/** 
	 * parse the argumentes
	 * 
	 * @param args
	 * @return
	 */
	public static CommandLine parse(String[] args){
	try {
			if (cmdLineParser == null) init(args);
			return cmdLineParser.parse(cmdLineOptions, args);
			  
		} catch (ParseException e) {
			
			// TODO Auto-generated catch block
			logger.fatal("failed to parse command line \n", e.getLocalizedMessage() );
			help(cmdLineOptions);
		}
		
		return null;
	}
	
	/**
	 * start the server mode
	 * @param cmd CommandLine
	 */
	public static void startEmbeddedDBServer(CommandLine cmd){
		String serverType = null;
		
		if (Globals.DBServer != null) {
			logger.debug("db server already running - not started again");
			return;
		}
		
		if (cmd.getOptionValue(cmdAutoStartDBServer)!=null){
			// TO-DO -> no other Servers implemented
			serverType = cmd.getOptionValue(cmdServer);
			Globals.DBServer = new H2Server();
			
		}else
			// default
			Globals.DBServer = new H2Server();
		
		// run even with null date -> full feed
		if (!Globals.DBServer.probeServer()) { 
			// start the server and also set the jdbc connection
			if (Globals.DBServer.startServer()) {
				logger.debug("db server at " + Globals.DBServer.getAddress() + " started");
			}
		} else logger.info("server '" + Globals.DBServer.getAddress()+ " running");
		
	}
	/**
	 * interprete the command line commands and execute them
	 * 
	 * @param cmd
	 */
	public static void interprete(CommandLine cmd, boolean ignoreServerMode){
		boolean aRunflag = false;
		
		// help
		if (cmd.hasOption(CmdLine.cmdHelp)){
			help(cmdLineOptions);
			aRunflag = true;
		}
		// command property file
		if (cmd.hasOption(cmdProperty)){
			if (cmd.getOptionValue(CmdLine.cmdProperty) !=null){
				String filename = cmd.getOptionValue(CmdLine.cmdProperty);
				Setting.Default.read(filename);
			} else Setting.Default.read(null);
		}
		
		// start server or start autoserver mode -> means to start the db server
		// !ignoreServerMode is not used -> CurryServer doesnot to start the db server
		if (cmd.hasOption(CmdLine.cmdAutoStartDBServer) || ( cmd.hasOption(CmdLine.cmdServer))) {
			startEmbeddedDBServer(cmd);
			// runflag
			aRunflag = true;
		}
						
		// command feed
		if (cmd.hasOption(CmdLine.cmdFeed)) {
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
		if (cmd.hasOption(CmdLine.cmdTest)) {
			// run 
			DataRunner.runTest();
			aRunflag = true;
		}
	
		// default - no commands
		if (!aRunflag){
			System.out.println("no command selected - run -h to see options");
		}
		
		// endless running
		if (!ignoreServerMode && cmd.hasOption(CmdLine.cmdServer)) {
			CurryServer aServer = new CurryServer(false);
			aServer.start();
			try {
				aServer.join(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.info(e.getLocalizedMessage());
				if (logger.isDebugEnabled()) logger.catching(e);
			}
			// stop the server
			if (Globals.DBServer != null && Globals.DBServer.isServerRunning()) 
				Globals.DBServer.stopServer();
			
		}

	}
	/**
	 * main function to run on the command line
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("A Spicy Curry Data Back End V.01\n");
		// parse arguments
		CommandLine cmd = parse(args);
		// interprete
		interprete(cmd, false);
		// exit
		System.exit(0);
	}
	
	/**
	 * create the command line options
	 * 
	 * @return the Options
	 */
	public static boolean createOptions()
	{
		cmdLineOptions = new Options();
		
		// help
		cmdLineOptions.addOption(CmdLine.cmdHelp, "help", false, "help for command line options");
		
		// properties
		Option commandProperty = Option.builder(CmdLine.cmdProperty)
			    .longOpt( "property" )
			    .required(false)
			    .desc( "name and location of the property file"  )
			    .hasArg()
			    .argName(cmdOptionPropertyFileName)
			    .build();
		cmdLineOptions.addOption(commandProperty);

		// command feed
		Option commandTest = Option.builder(CmdLine.cmdTest)
			    .longOpt( "test" )
			    .required(false)
			    .desc( "internal test"  )
			    .build();
		cmdLineOptions.addOption(commandTest);

		// command server mode
		Option commandServerMode = Option.builder(CmdLine.cmdServer)
			    .longOpt( "server mode" )
			    .required(false)
			    .desc( "automatically start in server mode"  )
			    .type(String.class)
			    .required(false)
			    .build();
		cmdLineOptions.addOption(commandServerMode);
		

		// command autoserver
		Option commandAutoServer = Option.builder(CmdLine.cmdAutoStartDBServer)
			    .longOpt( "autodbserver" )
			    .required(false)
			    .desc( "automatically start a database server"  )
			    .numberOfArgs(1)
			    .optionalArg(true)
			    .argName(CmdLine.cmdOptionServerType)
			    .type(String.class)
			    .required(false)
			    .build();
		cmdLineOptions.addOption(commandAutoServer);
		
		// command feed
		Option commandFeed = Option.builder(CmdLine.cmdFeed)
			    .longOpt( "feed" )
			    .required(false)
			    .desc( "feed data from polarion to the data store"  )
			    .numberOfArgs(1)
			    .optionalArg(true)
			    .argName(CmdLine.cmdOptionChangedSinceDate)
			    .type(Date.class)
			    .required(false)
			    .build();
		cmdLineOptions.addOption(commandFeed);
		
		return true;
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
