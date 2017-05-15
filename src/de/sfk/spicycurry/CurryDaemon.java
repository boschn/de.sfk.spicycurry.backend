/**
 * 
 */
package de.sfk.spicycurry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sfk.spicycurry.server.CurryServer;
import de.sfk.spicycurry.server.H2Server;

/**
 * daemon class for running as server in the background
 * 
 * @author boris
 *
 */
public class CurryDaemon implements Daemon {

	private CurryServer curryServerThread; 
	// logger
	private static Logger logger = LogManager.getLogger(CurryDaemon.class);
	
	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#destroy()
	 */
	@Override
	public void destroy() {
		curryServerThread = null;

	}

	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#init(org.apache.commons.daemon.DaemonContext)
	 */
	@Override
	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
		// get the command line arguments
		String[] args =  daemonContext.getArguments(); 
		CommandLine cmd = CmdLine.parse(args);
		CmdLine.interprete(cmd, true);
        
        // define the new daemon thread
        curryServerThread = new CurryServer(true);	
     }

	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#start()
	 */
	@Override
	public void start() throws Exception {
		curryServerThread.start();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.daemon.Daemon#stop()
	 */
	@Override
	public void stop() throws Exception {
	     
	        curryServerThread.interrupt();
	        // stop the server
	     	if (Globals.DBServer != null && Globals.DBServer.isServerRunning()) 
	     				Globals.DBServer.stopServer();
	}

}
