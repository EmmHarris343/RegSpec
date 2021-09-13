package net.yarr.regspec.Database;

import net.yarr.regspec.Logger;
import net.yarr.regspec.RegSpec;

import java.io.File;
import java.io.IOException;
// Sql:
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;


// Find path import:
import java.nio.file.Path;
import java.nio.file.Paths;
// File paths using RegSpec Files
import net.yarr.regspec.configuration.RSConfFile;
import net.yarr.regspec.hook.WrldGrdHook;

public class DBConnector{
	private static String dbLocation;
	private static Connection Conn;
	private static Connection Conndb;

	public void SQConnector(File dbLocation)
	{
		// This is completely useless... But w/e
		this.dbLocation = RSConfFile.getDirectory() + "RS.db";
		//Logger.debug(this.dbLocation);

	}
	public static Connection openConnection()
	{
		if (RSConfFile.useSQL == true)
		{		
			// Should try to open mysql database And set the Static Field Conn With the connection info.
			if (openMysql() == null) {
				Logger.error("Mysql Connection Failed Reverting to Flat File");
				RSConfFile.useSQL = false;
				Conn = (openSQlite());				
			}
			else {
				Conn = (openMysql());	
			}
		}
		else 
		{
			// Should Open sqlite, if it works properly it sets the Static Field Conn with the connection info.
			Conn = (openSQlite());
		}
		//Logger.debug("&eDB CONNECTOR --- " + Conn);
		return Conn;
	}
	// This is the Mysql Connection using the login details from RegSpecFile (Which is pulled from the config.yml) This returns the connection
	// Eventually Not setup yet
	public static Connection openMysql() {
		Logger.debug("&9--No Conenction Open -- Attempting to Open connect to MYSQL");
		Connection Conn;
		try
		{
			String url = RSConfFile.loadMYSQL().get(0).toString();
			String user = RSConfFile.loadMYSQL().get(1).toString();
			String pass = RSConfFile.loadMYSQL().get(2).toString();
			//Logger.debug("&a-- The URL &b" + url + " &aUser &b" + user + " &aand Pass &b" + pass);
			Conn = DriverManager.getConnection(url, user, pass);
			Logger.debug("&bConnected to Mysql Server:"+ Conn + " --");
			return Conn;
		}
		catch (SQLException localSQLException)
		{
			Logger.error("Couldn't connect to MYSQL - Are you sure you want to use it? Maybe check the conf file?");
			Conn = null;
			return Conn;
		}
	}
	// This is the SQLite flat file database connection. If the database file doesn't exist it will create it then return the connection.
	public static Connection openSQlite() {
		Logger.debug("&9--No Conenction Open -- Attempting to Open Connection - FLAT FILE--");
		if (RSConfFile.getDirectory() == null) {
			// If the location of the DB Folder return null... Abort can't access files
			Logger.error("&cNo directory folder found - Abort");
			Conn = null;
			//RegSpec RSPEC = new RegSpec ();
			//RSPEC.shutdown();
		}
		else {
			// Check if the file is not present.
			File file = new File(RSConfFile.getDirectory() + "RS.db");
			//Logger.debug("&e DB PATH:" + file);
			if (!file.exists()) {
				//If the DB File is not found, setupDatabases.
				Logger.debug("&dNo DB File Found - Creating.");
				Conn = setupDatabase();
			}
			else 
			{
				try {
					// db parameters
					//String url = "jdbc:sqlite:C:/sqlite/db/chinook.db";
					String url = "jdbc:sqlite:" + RSConfFile.getDirectory() + "RS.db";
					//Logger.debug("&eURL OF SQL CONN:  &c" + url);
					// create a connection to the database
					Conn = DriverManager.getConnection(url);
					Logger.debug("&bConnected to SQLite DB");
					//Logger.debug("Conn:  " + Conn);
					//System.out.println("Connection to SQLite has been established.");

				} catch (SQLException e) {
					System.out.println(e.getMessage());
					Logger.error("&cError Could not connect to SQL -- " + e.getMessage());
				}
			}
		}
		return Conn;
	}
	// Setup a database file (Only for sqlite flat file)
	private static Connection setupDatabase() {
		// Use sql False (Will need to be updated)
		if (!RSConfFile.useSQL)
		{			
			try {
				String url = "jdbc:sqlite:" + RSConfFile.getDirectory() + "RS.db";
				Conndb = DriverManager.getConnection(url);
				// If the Conndb connection failed, the DB file doesn't exist.
				// If it doesn't exist the connection auto creates it.
				if (Conndb != null) {
					DatabaseMetaData meta = Conndb.getMetaData();
					Logger.debug("The driver name is " + meta.getDriverName());
					Logger.info("&bFlat file RS.db created.");
				}

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
		return Conndb;
	}
	public static boolean checkConnection()
			throws SQLException
	{
		return (Conn != null) && (!Conn.isClosed());
	}
	public Connection getConnection()
	{
		return Conn;
	}

	public static void closeConnection()
			throws SQLException
	{
		if (Conn == null) {
			//return false;
		}
		else {
			Conn.close();
			Conn = null;
		}
		if (Conndb == null){
			
		}
		else {
			Conndb.close();
			Conndb = null;			
		}
		//return true;
	}
}
