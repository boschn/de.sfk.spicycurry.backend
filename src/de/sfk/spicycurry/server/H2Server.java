package de.sfk.spicycurry.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.message.DbException;
import org.h2.tools.Server;
import org.h2.util.NetUtils;

import de.sfk.spicycurry.Setting;
import de.sfk.spicycurry.data.AbstractPersistor;
import de.sfk.spicycurry.data.Attachment;

/**
 * H2 server wrapper for database server
 * 
 * @author boris
 *
 */
public class H2Server extends AbstractDBServer {

	public static final String defaultDatabaseName = "SpicyCurry";
	// default port
	public static final String defaultPort = "9092";
	// default lock file name
	private static final String defaultFileName = "dbserver.lock";
	// PropertyName for default Port
	private static final String PROPERTY_H2_DATABASE_PORT = "H2.PORT";
	// driver name
	public static final String PROPERTY_H2_JDBC_DRIVER = "H2.JDBC_DRIVER";
	// defaultdriver
	public static final String defaultJDBCDriver = "org.h2.driver";
	
	// H2 Server
	private Server server = null;

	// the port for the server
	private int autoServerPort;
	// the database id
	private String databaseId ;
	// the dartabase file
	private String databaseFile;
	// the driver name
	private String databaseJDBCDriver;
	
	// logger
	private static Logger logger = LogManager.getLogger(H2Server.class);
	
	/**
	 * default ctor
	 */
	public H2Server()
	{
		super();
		int port = Integer.parseUnsignedInt(Setting.Default.get(PROPERTY_H2_DATABASE_PORT,defaultPort));
		Path defaultPath = null;
		try {
			defaultPath = Paths.get(H2Server.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			logger.debug(e.getLocalizedMessage());
			if (logger.isDebugEnabled()) logger.catching(e);
			defaultPath = Paths.get(".");
		}
		String databasePath = Setting.Default.get(AbstractPersistor.PROPERTY_PERSISTOR_DATABASE_PATH, defaultPath.toString());
		String databaseName = Setting.Default.get(AbstractPersistor.PROPERTY_PERSISTOR_DATABASE_NAME, defaultDatabaseName);
		String driverClassName = Setting.Default.get(this.PROPERTY_H2_JDBC_DRIVER, this.defaultJDBCDriver);
		
		setJDBCDriverName(driverClassName);
		setId(databaseName);
		setAutoServerPort(port);
		setDatabaseFile(databasePath + "/" + databaseName);
	}
	/**
	 * creates an server instance with given databaseName and port
	 * @param port
	 * @param databasefile
	 */
	public H2Server(String Id, String databasefile, int port ) {
		super();
		setId(Id);
		setAutoServerPort(port);
		setDatabaseFile(databasefile);
		String driverClassName = Setting.Default.get(this.PROPERTY_H2_JDBC_DRIVER, this.defaultJDBCDriver);
		setJDBCDriverName(driverClassName);
		
	}

	/**
	 * get the AutoServerPort (default)
	 * @return
	 */
	public synchronized int getAutoServerPort() {
		return autoServerPort;
	}

	/**
	 * set the AutoServerPort
	 * 
	 * @param autoServerPort
	 */
	private synchronized void setAutoServerPort(int autoServerPort) {
		this.autoServerPort = autoServerPort;
	}

	/**
	 * get the databaseName
	 * 
	 * @return
	 */
	public synchronized String getDatabasFile() {
		return databaseFile;
	}

	/**
	 * @return the databaseId
	 */
	public synchronized String getId() {
		return databaseId;
	}
	/**
	 * @param databaseId the databaseId to set
	 */
	private synchronized void setId(String databaseId) {
		this.databaseId = databaseId;
	}
	/** 
	 * set the database name
	 * @param databaseName
	 */
	private synchronized void setDatabaseFile(String databaseName) {
		this.databaseFile = databaseName;
	}

	/**
	 * @return the databaseJDBCDriver
	 */
	public synchronized String getJDBCDriverName() {
		return databaseJDBCDriver;
	}
	/**
	 * @param databaseJDBCDriver the databaseJDBCDriver to set
	 */
	public synchronized void setJDBCDriverName(String databaseJDBCDriver) {
		this.databaseJDBCDriver = databaseJDBCDriver;
	}
	/**
	 * start the server with the default id
	 * @return
	 */
	public boolean startServer(){
		return startServer(this.getId());
	}
	/**
	  * start the server with a name
	  * @param id server name
	  */
	 protected boolean startServer(String id) {
		 
		 String localAddress = NetUtils.getLocalAddress();
	     String address = localAddress + ":" + Integer.toString(this.getAutoServerPort());
	     String hostName = NetUtils.getHostName(localAddress);
	           
			try {
	            server = Server.createTcpServer(
	                    "-tcpPort", Integer.toString(this.getAutoServerPort()),
	                    "-tcpAllowOthers",
	                    "-tcpDaemon",
	                    "-key", id, 
	                    this.getDatabasFile());
	            
	            server.start();
	        } catch (Exception e) {
	            logger.error("h2 server '"+ id + "' failed to start on host '" + hostName+ "' at " + address);
	            logger.error(e.getLocalizedMessage());
	            if (logger.isDebugEnabled()) logger.catching(e);
	            return false;
	        }
	        if (!server.isRunning(false)){
	        	logger.error("h2 server '"+ id + "' failed to start on host '" + hostName+ "' at " + address);
	            logger.error(server.getStatus());
	        	return false;
	        }
			// lock
			Properties lock = new Properties();
	        lock.setProperty("server", address);
	        lock.setProperty("hostName", hostName);
	        try {
	        	FileWriter aFileWriter = new FileWriter(defaultFileName );
	        	lock.store(aFileWriter, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Timestamp(new Date().getTime())));
	        } catch (Exception e){
	        	logger.error("could not write lock file '" + defaultFileName);
	        	logger.error(e.getLocalizedMessage());
	        	if (logger.isDebugEnabled()) logger.catching(e);		
	        }
	        
	        // add the shutdown hook for stopping the server
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	            public void run() {
	              stopServer();
	            }
	          });
	        
	        // Added the following... 
	        System.getProperties().setProperty("H2_IS_EMBEDDED_TCP_SERVER"+ server.getPort(), "TRUE");
	        
	        // return 
	        logger.info("h2 server '"+ id + "' started on host '" + hostName+ "' at " + address);
	        return true;
	    }

	 	/**
	 	 * stop the server
	 	 */
	    public synchronized void stopServer() {
	        if (server != null) {
	            Server s = server;
	            // avoid calling stop recursively
	            // because stopping the server will
	            // try to close the database as well
	            server = null;
	            s.stop();
	            
	            File file = new File(defaultFileName);
	    		if(!file.delete()){
	    			logger.error("could not delete lock file " + defaultFileName);
	    		}

	            // Added the following...
	            System.getProperties().setProperty("H2_IS_EMBEDDED_TCP_SERVER"+ s.getPort(), "FALSE");
	        }
	    }
	    /**
	     * finalize by stopping the server
	     */
	    public void finalize(){
	    		// stopServer();
	    }
		@Override
		public boolean isServerRunning() {
			// TODO Auto-generated method stub
			if (server == null) return false;
			if (server.isRunning(false)) return true;
			
			return false;
		}

		/**
		 * return the local host name
		 * @return
		 */
		public String getHostName(){
			String localAddress = NetUtils.getLocalAddress();
		    return NetUtils.getHostName(localAddress);
		}
		/**
		 * probe the server on the local host and with the default port
		 * @return
		 */
		public boolean probeServer(){
			 String localAddress = NetUtils.getLocalAddress();
		     String hostName = NetUtils.getHostName(localAddress);
		     
		     return probeServer(hostName, getAutoServerPort());
		}

		/**
		 * probe if there is a H2 server running on a host and port
		 * 
		 * @param hostName
		 * @param port
		 * @return true if there is so
		 */
		public boolean probeServer(String hostName, int port){
			// declaration section:
			// mySocket: our client socket pretending to be a browser
			// os: output stream
			// is: input stream
	        Socket mySocket = null;  
	        DataOutputStream os = null;
	        DataInputStream is = null;

	        // Initialization section:
			// btw make sure parameters are passed noting that this quick code is NOT
			// Try to open input and output streams
	        
	        try {
	            mySocket = new Socket(hostName,port);
	            os = new DataOutputStream(mySocket.getOutputStream());
	            is = new DataInputStream(mySocket.getInputStream());
	        } catch (UnknownHostException e) {
	        	logger.error("db server start failed due to unresolvable host:" + hostName);
	            return false;
	        } catch (IOException e) {
	        	if (logger.isDebugEnabled()) logger.info("Couldn't get I/O for the connection to:" + hostName + " no db server to probe");
	            return false;
	        }
	        
			// If everything has been initialized then we want to write some data
			// to the socket we have opened a connection to on port 80, 8082, whatever 
			// (what the server is listening on)
			if (mySocket != null && os != null && is != null) {
			   try {
				    // pretend to be a browser and do a GET against a resource
			        os.writeBytes("GET /index.html HTTP/1.0\r\n\r\n");    
			        // wait for response from webserver, dump out response for sanity check
	                String responseLine;
	                while ((responseLine = is.readLine()) != null) {
	                	if (logger.isDebugEnabled()) logger.debug("Server: " + responseLine);
	                    if (responseLine.indexOf("Ok") != -1) {
	                      break;
	                    }
	                }
	                
					// clean up:
					// close the output stream
					// close the input stream
					// close the socket
			        os.close();
	                is.close();
	                mySocket.close();   
	            } catch (UnknownHostException e) {
	            	if (logger.isDebugEnabled()) logger.info("Trying to connect to unknown host: " + e);
	                return false;
	            } catch (IOException e) {
	                if (logger.isDebugEnabled()) logger.info("wanted IOException:  " + e);
	                return false;
	            }
	        }
			return true;    
		}
		
		@Override
		public String getAddress() {
			
			return this.getHostName() + ":" + this.getAutoServerPort() + ":" + this.getDatabasFile();
		}
		@Override
		public String getJDBCUrl() {
			
			return "jdbc:h2:tcp://localhost:" + Integer.toString(getAutoServerPort())+ "/" + this.getDatabasFile();
		}
		      
		
}
		

