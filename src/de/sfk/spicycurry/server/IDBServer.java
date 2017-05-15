/**
 * 
 */
package de.sfk.spicycurry.server;

/**
 * general database eerver
 * 
 * @author boris
 *
 */
public interface IDBServer {

	/**
	 * 
	 * @return true if the server is running
	 */
	boolean isServerRunning();
	/**
	 * 
	 * @return true if the a server is listening
	 */
	boolean probeServer();
	/**
	 * start a server with default id
	 * @return
	 */
	boolean startServer();
	/**
	 * stop the server
	 */
	void stopServer();
	/**
	 * gets the address of the server
	 * @return String
	 */
	String getAddress();
	/**
	 * get the JDBC Connection URL
	 * @return
	 */
	String getJDBCUrl();
	/**
	 * get the Id of the database
	 * @return
	 */
	String getId();
	/**
	 * gets the driver class name 
	 * @return
	 */
	String getJDBCDriverName();
}
