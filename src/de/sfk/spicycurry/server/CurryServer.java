/**
 * 
 */
package de.sfk.spicycurry.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sfk.spicycurry.CmdLine;
import de.sfk.spicycurry.CurryDaemon;

/**
 * server thread processing regular in the background
 * @author boris
 *
 */
public class CurryServer extends Thread {
	
    private boolean isDaemon = false;
	// logger
	private static Logger logger = LogManager.getLogger(CurryDaemon.class);

	/**
	 * constructor
	 * @param daemon true if daemon
	 */
	public CurryServer(boolean daemon){
		isDaemon = daemon;
		
	}
    @Override
    public synchronized void start() {
        this.setDaemon(isDaemon);
        // CmdLine.startEmbeddedDBServer(cmd); -> should be started
        super.start();
    }
    @Override
    public void run() { 
    	 while ( !isInterrupted() )
    	    {
    	      System.out.println( "Und er läuft und er läuft und er läuft" );

    	      try
    	      {
    	       Thread.sleep( 500 );
    	      }
    	      catch ( InterruptedException e )
    	      {
    	       interrupt();
    	       System.out.println( "Unterbrechung in sleep()" );
    	      }
    	    }
    }

}
