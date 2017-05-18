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
	private static final String defaultUserId = "sa";
	private static final String defaultPassword = "";
	// PropertyName for default Port
	private static final String PROPERTY_H2_DATABASE_PORT = "H2.PORT";
	// driver name
	public static final String PROPERTY_H2_JDBC_DRIVER = "H2.JDBC_DRIVER";
	// defaultdriver
	public static final String defaultJDBCDriver = "org.h2.Driver";
	
	public static final String PROPERTY_H2_LOCK_MODE = "H2.LOCK_MODE";
	public static final String defaultH2LockMode = "3"
	;
	// H2 Server
	private Server serverH2TCP = null;
	private Server serverH2Postgress = null;
	private Server serverWeb = null;
	
	// the port for the server
	private int autoServerPort;
	// the database id
	private String databaseId ;
	// the dartabase file
	private String databaseFile;
	// the driver name
	private String databaseJDBCDriver;
	// user
	private String userId = "";
	// password
	private String passWord = "";
	// logger
	private static Logger logger = LogManager.getLogger(H2Server.class);
	
	/**
	 * default ctor
	 */
	public H2Server()
	{
		super();
		int aPort = Integer.parseUnsignedInt(Setting.Default.get(PROPERTY_H2_DATABASE_PORT,defaultPort));
		Path aDefaultPath = null;
		try {
			aDefaultPath = Paths.get(H2Server.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e) {
			logger.debug(e.getLocalizedMessage());
			if (logger.isDebugEnabled()) logger.catching(e);
			aDefaultPath = Paths.get(".");
		}
		String aDatabasePath = Setting.Default.get(AbstractPersistor.PROPERTY_PERSISTOR_DATABASE_PATH, aDefaultPath.toString());
		String aDatabaseName = Setting.Default.get(AbstractPersistor.PROPERTY_PERSISTOR_DATABASE_NAME, defaultDatabaseName);
		String aDriverClassName = Setting.Default.get(this.PROPERTY_H2_JDBC_DRIVER, this.defaultJDBCDriver);
		String anUserId = Setting.Default.get(AbstractPersistor.PROPERTY_PERSISTOR_DATABASE_USERNAME, defaultUserId);
		String aPassword = Setting.Default.get(AbstractPersistor.PROPERTY_PERSISTOR_DATABASE_PASSWORD, defaultPassword);

		
		setJDBCDriverName(aDriverClassName);
		setId(aDatabaseName);
		setAutoServerPort(aPort);
		setDatabaseFile(aDatabasePath + "/" + aDatabaseName);
		setUserId(anUserId);
		setPassWord(aPassword);
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
	public synchronized String getDatabaseFile() {
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
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the passWord
	 */
	public String getPassWord() {
		return passWord;
	}
	/**
	 * @param passWord the passWord to set
	 */
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	/**
	 * start the postgress server
	 * @return true if successfull
	 */
	public boolean startPostgressServer() {
		try {
			serverH2Postgress = Server.createPgServer("-pgAllowOthers", 
											   "-ifExists",
											   "-pgDaemon",
											   "-key", getId(), 
							                    this.getDatabaseFile()
											  );
			serverH2Postgress.start();
			
		} catch (Exception e) {                                                                                 
		        logger.error("h2 postgress server '"+ getId() + "' failed to start @'" + this.getAddress()+ ":" + serverH2Postgress.getPort()); 
		        logger.error(e.getLocalizedMessage());                                                              
		        if (logger.isDebugEnabled()) logger.catching(e);                                                    
		        return false;                                                                                       
		}                                                                               
		
		logger.info("h2 postgress server '"+ getId() + "' started on host '" + getHostName());
		return true;
	}
	/**
	 * start web server
	 * @return true if successfull
	 */
	public boolean startWebServer() {
		
		try {
			serverWeb = Server.createWebServer("-webAllowOthers", 
											   "-ifExists",
											   "-webDaemon",
											   "-key", getId(), 
							                    this.getDatabaseFile()
											  );
			serverWeb.start();
			logger.info("h2 web server '"+ getId() + "' started on host '" + getHostName()+ ":" + serverWeb.getPort());
			
		} catch (Exception e) {                                                                                 
		        logger.error("h2 web server '"+ getId() + "' failed to start @'" + this.getAddress()); 
		        logger.error(e.getLocalizedMessage());                                                              
		        if (logger.isDebugEnabled()) logger.catching(e);                                                    
		        return false;                                                                                       
		}                                                                                                       
		
		
		return true;
	}
	/**
	 * start the server with the default id
	 * @return
	 */
	public boolean startServer(){
		 String localAddress = NetUtils.getLocalAddress();
	     String address = localAddress + ":" + Integer.toString(this.getAutoServerPort());
	     String hostName = NetUtils.getHostName(localAddress);
	           
			try {
	            serverH2TCP = Server.createTcpServer(
	                    "-tcpPort", Integer.toString(this.getAutoServerPort()),
	                    "-tcpAllowOthers",
	                    "-tcpDaemon",
	                    "-key", getId(), 
	                    this.getDatabaseFile());
	            
	            serverH2TCP.start();
	        } catch (Exception e) {
	            logger.error("h2 server '"+ getId() + "' failed to start on host '" + hostName+ "' at " + address);
	            logger.error(e.getLocalizedMessage());
	            if (logger.isDebugEnabled()) logger.catching(e);
	            return false;
	        }
	        if (!serverH2TCP.isRunning(false)){
	        	logger.error("h2 server '"+ getId() + "' failed to start on host '" + hostName+ "' at " + address);
	            logger.error(serverH2TCP.getStatus());
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
	        System.getProperties().setProperty("H2_IS_EMBEDDED_TCP_SERVER"+ serverH2TCP.getPort(), "TRUE");
	        // log info
	        logger.info("h2 server '"+ getId() + "' started on host '" + hostName+ "' at " + address);
	        
	        // start the other server
	        startWebServer();
	        startPostgressServer();
	        // return
	        return true;
	    }

	 	/**
	 	 * stop the server
	 	 */
	    public synchronized void stopServer() {
	        if (serverH2TCP != null) {
	            Server s = serverH2TCP;
	            // avoid calling stop recursively
	            // because stopping the server will
	            // try to close the database as well
	            serverH2TCP = null;
	            s.stop();
	            
	            File file = new File(defaultFileName);
	    		if(!file.delete()){
	    			logger.error("could not delete lock file " + defaultFileName);
	    		}

	            // Added the following...
	            System.getProperties().setProperty("H2_IS_EMBEDDED_TCP_SERVER"+ s.getPort(), "FALSE");
	            // return 
		        logger.info("h2 server '"+ getId() + "' stopped on host '" + getHostName());
	        }
	        
	        if (serverWeb != null) {
	            Server s = serverWeb;
	            // avoid calling stop recursively
	            // because stopping the server will
	            // try to close the database as well
	            serverWeb = null;
	            s.stop();
	            // return 
		        logger.info("h2 web server '"+ getId() + "' stopped on host '" + getHostName());
	        }
	        if (serverH2Postgress != null) {
	            Server s = serverH2Postgress;
	            // avoid calling stop recursively
	            // because stopping the server will
	            // try to close the database as well
	            serverH2Postgress = null;
	            s.stop();
	            // return 
		        logger.info("h2 postgress server '"+ getId() + "' stopped on host '" + getHostName());
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
			if (serverH2TCP == null) return false;
			if (serverH2TCP.isRunning(false)) return true;
			
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
			
			return this.getHostName() + ":" + this.getAutoServerPort() + ":" + this.getDatabaseFile();
		}
		@Override
		public String getJDBCUrl() {
			String aLockMode = Setting.Default.get(PROPERTY_H2_LOCK_MODE, defaultH2LockMode);
			return "jdbc:h2:tcp://" + this.getHostName()+ ":" + Integer.toString(getAutoServerPort())+ "/" + this.getId()+ ";LOCK_MODE=" + aLockMode;
		}
		      
		
}
		

