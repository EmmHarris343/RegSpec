package net.yarr.regspec.Database;

import net.yarr.regspec.Logger;
import net.yarr.regspec.RegSpec;


import java.io.File;
import java.io.IOException;
// Sql:
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
// Needed for list files:
import java.util.List;

import javax.swing.table.DefaultTableModel;

// This programs imports:
import net.yarr.regspec.configuration.RSConfFile;
import net.yarr.regspec.Logger;
import net.yarr.regspec.RegSpec;
import net.yarr.regspec.Database.DBNodes;
//import net.yarr.regspec.hook.WrldGrdHook;

public class SQConnector {
	private static Connection ConnDB;
	// Declare the array
	// This is the loaded Entry list with all the regions in it. Makes it so it doesn't have to access the MYSQL database 
	public static ArrayList<DBNodes> loadEntries;
	// This is the specific entries array. This holds the information for a specific region specified.
	// This is actually useless and should be removed.. It uses Region_Result which it automatically defines and recreates each time queryArray is Called
	//public static ArrayList<DBNodes> specEntries;
	
	private static boolean TableExists;
	
	// METHODS :
	

	// This  loads all entries found in the database table. Stores it in an array list to avoid using Mysql on every player move event.
	// ONLY CALL THIS METHOD When its required that the loadEntires be re-populated!
	public static ArrayList<DBNodes> loadData() {
		//List<Product> Entries = new ArrayList<Product>();
		// Using the DB nodes file Declare it as the "entries" for the while loop.
		loadEntries = new ArrayList<DBNodes>();
		// Declare the Statement to laod database info.
		Logger.debug("&4<SQ><NOTICE> loadData Has been called. Querying SQL Server");
		PreparedStatement stmt = null;
		try {
			// Calls the DBConnector Which returns the connection to what ever database and type it is using (Mysql or Flatfile Sqlite)
			if(DBConnector.openConnection() != null) {
				ConnDB = DBConnector.openConnection();
				// Check if tables exist. If not Create.
				chkTables();
				stmt = ConnDB.prepareStatement("select * from RegSpec_PermNode");
				ResultSet result = stmt.executeQuery();
				// If the result has no rows.. There
				if(!result.next())
				{
					// No rows Found. Do nothing.
					Logger.debug("&e<SQ>SQL Table is Empty or Doesn't Exist.");
					if (TableExists != true) {
						Logger.error("&c<SQ>FATAL ERROR - SQL Table appears to not exist. Was it not created?");
					}
				}
				else
				{
				do { // Do this code WHILE Result.next = true basically
					// Call the DB nodes class Get the Entries
					DBNodes Nodes = new DBNodes();
					Nodes.setID(result.getLong("id"));
					Nodes.setREGION(result.getString("region"));
					Nodes.setTYPE(result.getString("type"));
					Nodes.setNODE(result.getString("node"));
					Nodes.setWORLD(result.getString("world"));
					loadEntries.add(Nodes);
				} while (result.next());
				}
			}
			else {
				Logger.error("&c<SQ> Error Connection Returned Null! - Aborting");
			}
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
			Logger.error("&c<SQ>SQL Table could not be loaded or Database Connection Failed.");
			return null;
		}
		finally { // This finally part. Closes the statements and closes the DB connection (having it open is a waste, and is bad coding)
			if (stmt != null) {
				try { 
					Logger.debug("&1<SQ> Statement Closed");
					stmt.close();
				} catch (SQLException ex) {
				}
			}
			if (ConnDB != null) {
				try { 
					Logger.debug("&1<SQ> ConnDB Closed, DB Connector Closed");
					ConnDB.close();
					DBConnector.closeConnection();
				} catch (SQLException ex) {
				}
			}
		}
		// This returns loadEntries (Which is the array with all the Entries.. But most times this is called, its not actually being use..
		return loadEntries;
	}
	// This returns An arraylist that stores information for the specific region name specified. Appossed to returning the entire Array. (This is also more efficient)
	public static ArrayList<DBNodes> QueryArray(String r_name) {
		// Define the array list each time Everytime this line Runs. It clears all results in Region_Result!
		ArrayList<DBNodes> Region_Result = new ArrayList<DBNodes>();
		Logger.debug("&d<SQ><NOTICE> QueryArray Called is Checking loadEntires State");
		if (loadEntries != null) {
			if (!loadEntries.isEmpty()) {
				Logger.debug("&a<SQ>loadEntries Array was not null and was not empty. Trying to Populate Region_Result's array with Regions that have the name as: &d" + r_name);
				Logger.debug("&e<SQ>Did not query SQl Database -- Saved Time, Resources and memory");
				for (int i=0; i < loadEntries.size(); i++) {
					// Using the full data loaded from loadEntries
					// Check if any of them have the region name provided
					if (loadEntries.get(i).getREGION().equalsIgnoreCase(r_name)) {
						DBNodes Nodes = new DBNodes();
						Nodes.setID(loadEntries.get(i).getID());
						Nodes.setREGION(loadEntries.get(i).getREGION());
						Nodes.setTYPE(loadEntries.get(i).getTYPE());
						Nodes.setNODE(loadEntries.get(i).getNODE());
						Nodes.setWORLD(loadEntries.get(i).getWORLD());
						//Logger.debug("&b<SQ>Result of region Nodes- " + loadEntries.get(i).getNODE());
						Region_Result.add(Nodes);
						// While in the for loop and it = the region name. Add it to the Region_result Array list.
					}
				}
			}else {
				Logger.debug("&c<SQ>Somehow it looks like loadEntries isn't Null, but it is empty (something caused it to be empty).. Trying to loadData again (This is wasting resources!)");
				loadData();			
				Logger.debug("&c<SQ>loadData Method Called, Checking if loadEntries is empty");
				if (!loadEntries.isEmpty()) {
					Logger.debug("&a<SQ>loadEntries Was not Empty, Trying to Populate Region_Result's array with Regions that have the name as: &d" + r_name);
					for (int i=0; i < loadEntries.size(); i++) {
						// Using the full data loaded from loadEntries
						// Check if any of them have the region name provided
						
						if (loadEntries.get(i).getREGION().equalsIgnoreCase(r_name)) {
							DBNodes Nodes = new DBNodes();
							Nodes.setID(loadEntries.get(i).getID());
							Nodes.setREGION(loadEntries.get(i).getREGION());
							Nodes.setTYPE(loadEntries.get(i).getTYPE());
							Nodes.setNODE(loadEntries.get(i).getNODE());
							Nodes.setWORLD(loadEntries.get(i).getWORLD());
							//Logger.debug("&b<SQ>Result of region Nodes- " + loadEntries.get(i).getNODE());
							Region_Result.add(Nodes);
							// While in the for loop and it = the region name. Add it to the Region_result Array list.
						}
					}
				}
				else {
					Logger.debug("&c<SQ>Even After Calling loadData the Array loadEntries is still Empty. Can't go Further.");
				}
			}
		}
		else {
			// If it looks like it was cleared, reload it.
			Logger.debug("&f<SQ>Array Is Null. Trying to load loadEntries Array by calling loadData method - THIS SHOULD ONLY HAPPEN ON STARTUP!");
			loadData();			
			Logger.debug("&f<SQ>loadData Method Called, Checking if loadEntries is empty");
			if (!loadEntries.isEmpty()) {
				Logger.debug("&f<SQ>loadEntries &a -->Was not Empty, Trying to Populate Region_Result's array with Regions that have the name as: &d" + r_name);
				for (int i=0; i < loadEntries.size(); i++) {
					// Using the full data loaded from loadEntries
					// Check if any of them have the region name provided
					
					if (loadEntries.get(i).getREGION().equalsIgnoreCase(r_name)) {
						DBNodes Nodes = new DBNodes();
						Nodes.setID(loadEntries.get(i).getID());
						Nodes.setREGION(loadEntries.get(i).getREGION());
						Nodes.setTYPE(loadEntries.get(i).getTYPE());
						Nodes.setNODE(loadEntries.get(i).getNODE());
						Nodes.setWORLD(loadEntries.get(i).getWORLD());
						//Logger.debug("&b<SQ>Result of region Nodes- " + loadEntries.get(i).getNODE());
						Region_Result.add(Nodes);
						// While in the for loop and it = the region name. Add it to the Region_result Array list.
					}
				}
			}
			else {
				Logger.debug("&c<SQ>After calling the loadData Method. loadEntry's Array was still empty.. Can't go any further.");
			}
		}
		return Region_Result;
	}
	// Add a new node for the region specified.
	public static void addNODE(String insrt_region, String insrt_type, String insrt_node, String insrt_world) {
		// Make a insert string to allow for easy editing of the prepared statement.
		String insertNODE = "INSERT INTO RegSpec_PermNode"
				+ "(region,type,node,world) VALUES"
				+ "(?,?,?,?)";
		// Declare the statement to insert into the table.
		PreparedStatement localPreparedStatement = null;
		try {
			// Calls the DBConnector Which returns the connection to what ever database and type it is using (Mysql or Flatfile Sqlite)
			if (DBConnector.openConnection() != null)  {

				ConnDB = DBConnector.openConnection();
				// This checks the RegSpec PermNode Table, If it exists it does nothing. If I can't find it.. it creates a new table
				chkTables();
				Logger.debug("&b<SQ> Attempting Insert" );
				// Attempt to insert
				// OLD WAY.
				//localPreparedStatement = ConnDB.prepareStatement("INSERT INTO RegSpec_PermNode (region,type,node,world) VALUES('" + insrt_region + "', '" + insrt_type + "', '" + insrt_node + "', '" + insrt_world + "')");
				localPreparedStatement = ConnDB.prepareStatement(insertNODE);
				
				localPreparedStatement.setString(1, insrt_region);
				localPreparedStatement.setString(2, insrt_type);
				localPreparedStatement.setString(3, insrt_node);
				if (insrt_world != null) {
					Logger.debug("&c<SQ>WORLD IS NOT NULL");
					localPreparedStatement.setString(4, insrt_world);
				}
				else {
					Logger.debug("&b<SQ>WORLD WAS NULL! - this is good");
					localPreparedStatement.setNull(4, java.sql.Types.VARCHAR);;
				}
				localPreparedStatement.executeUpdate();
				Logger.debug("&b<SQ> If No Errors. Successful" );
			}
			else {
				Logger.error("&c<SQ> Error Connection Returned Null! - Aborting");
			}
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
			Logger.error("&c<SQ>SQL Table - Could not insert into table!");
		}
		finally {
			if (localPreparedStatement != null) {
				try { 
					Logger.debug("&1<SQ> Statement Closed");
					localPreparedStatement.close();
				} catch (SQLException ex) {
				}
			}
			if (ConnDB != null) {
				try { 
					Logger.debug("&1<SQ> ConnDB Closed, DB Connector Closed");
					ConnDB.close();
					DBConnector.closeConnection();
					reloadSQL();
				} catch (SQLException ex) {
				}
			}
		}
	}
	// This deletes the SQL entry by id
	public static void delEntry(String id) {
		PreparedStatement stmt = null;
		try {
			// Calls the DBConnector Which returns the connection to what ever database and type it is using (Mysql or Flatfile Sqlite)
			if (DBConnector.openConnection() != null) {

				ConnDB = DBConnector.openConnection();
				stmt = ConnDB.prepareStatement("DELETE FROM RegSpec_PermNode WHERE id = " + id);
				// execute the delete statement
				stmt.executeUpdate();
			}
			else {
				Logger.error("&c<SQ> Error Connection Returned Null! - Aborting");
			}
		}
		catch (SQLException e) {
			Logger.error("&c<SQ>Couldn't delete collumn" + e.getMessage());
		}
		finally {
			if (stmt != null) {
				try { 
					Logger.debug("&a<SQ> Statement Closed");
					stmt.close();
				} catch (SQLException ex) {
				}
			}
			if (ConnDB != null) {
				try { 
					Logger.debug("&a<SQ> ConnDB Closed, DB Connector Closed");
					ConnDB.close();
					DBConnector.closeConnection();
					// Reload the SQL data.
					reloadSQL();
				} catch (SQLException ex) {
				}
			}
		}
	}
	// This tells loadData to reload the data. (without each programming calling it)
	public static void reloadSQL() {
		// Reloading the sql data and populating the Array : loadEntries
		Logger.debug("&b<SQ>--Reloading SQL Data from Database. Something just made a change to the SQL Server. This is a good thing");
		loadData();
	}
	// Check if the Table Exists -- If there is no table found Create a table
	public static void chkTables(){
		try {

			DatabaseMetaData chkTble= ConnDB.getMetaData();
			// check if The Table "RegSpec_PermNode" exists
			ResultSet Result = chkTble.getTables(null, null, "RegSpec_PermNode", null);
			if (Result.next()) {
				// Table exists
				Logger.debug("&a<SQ>Check Tables - Table Exists in database Continuing..");
				TableExists = true;
			}
			else {
				// Table does not exist
				if (RSConfFile.useSQL != true) {
					try{
						String sqlCreate = "CREATE TABLE IF NOT EXISTS RegSpec_PermNode"
								+ " (id INTEGER PRIMARY KEY AUTOINCREMENT,"		
								+ " region VARCHAR(60),"
								+ " type VARCHAR(40),"
								+ " node VARCHAR(40),"
								+ " world VARCHAR(40));";
						Logger.debug("&eSQL String----:" + sqlCreate );
						Statement stmt = ConnDB.createStatement();
						stmt.execute(sqlCreate);
						Logger.debug("&bNo Table found Created new Table");
						TableExists = true;
					}
					catch (SQLException e){
						Logger.error("&c<SQ>No Table found - Tried to create - Failed");
						Logger.error("&c<SQ>EXCEPTION-- " + e.getMessage());
						ConnDB.close();
						DBConnector.closeConnection();
						RegSpec rsShutdown = new RegSpec();
						//SQConnector conct = new SQConnector ();
						rsShutdown.disPlugin();
					}
				}
				else {
					try{
						String sqlCreate = "CREATE TABLE IF NOT EXISTS RegSpec_PermNode"
								+ " (id INT(64) NOT NULL AUTO_INCREMENT,"		
								+ " region VARCHAR(60),"
								+ " type VARCHAR(40),"
								+ " node VARCHAR(40),"
								+ " world VARCHAR(40),"
								+ " PRIMARY KEY ( id ));";
						Logger.debug("&eSQL String----:" + sqlCreate );
						Statement stmt = ConnDB.createStatement();
						stmt.execute(sqlCreate);
						Logger.debug("&bNo Table found Created new Table");
						TableExists = true;
					}
					catch (SQLException e){
						Logger.error("&c<SQ>No Table found - Tried to create - Failed");
						Logger.error("&c<SQ>EXCEPTION-- " + e.getMessage());
						ConnDB.close();
						DBConnector.closeConnection();
						RegSpec rsShutdown = new RegSpec();
						rsShutdown.disPlugin();
					}
					
				}
			}
		}
		catch (SQLException e){
			Logger.error("&c<SQ>Could not connect ConnDB and load Metadata");
			Logger.error("&c<SQ>EXCEPTION-- " + e.getMessage());
		}
	}
}
