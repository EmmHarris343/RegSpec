package net.yarr.regspec.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import net.yarr.regspec.Logger;

// YML File stuff
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
// SQL 

public class RSConfFile extends JavaPlugin {
	private File jarFile;
	public URL update;
	protected static YamlConfiguration config;
	public static boolean useSQL;
	public static boolean COLOR_LOGS;
	public static boolean DEBUG_MODE;
	public static boolean PLAYER_SEND;
	public static String directory;
	//public static SqlConnector con = null;
	//COLOR_LOGS(Boolean.valueOf(true)),  DEBUG_MODE(Boolean.valueOf(false));
	
	
	private static String getLocation(){
		// Get the location of the running Jar file. 
		String path = RSConfFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath;
		try 
		{
			decodedPath = URLDecoder.decode(path, "UTF-8");
			return decodedPath;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Logger.error("Failed to decode path of jar file!");
		}
		return null;
	}
	public static String getDirectory(){
		// Get the current folder and path for RegSpec/<config folder>
		//Once it gets the directory and sets it. It simply returns "directory". Instead of getting the directory and jar file location each time.
		if (directory == "" | directory == null)
		{
		String directoryfix = getLocation();
		String directoryfixed = directoryfix.replaceAll("\\b.jar\\b", ""); 
		directory = directoryfixed + "/";
		Logger.debug("&a -- Directory not yet loaded -- Getting directory -- ");
		// once it calls get location, It corrects the path by removing .jar at the end of it. then adds a /
		// Returns the RegSpec config folder.
		}
		else {
			Logger.debug("&a -- Static Directory returned. --");
			//Logger.debug("&a --LOCATION-- : "+ directory);
		}
		return directory;
	}
	public static File getFile(File base, String path)
	{
		if (Paths.get(path, new String[0]).isAbsolute()) {
			return new File(path);
		}
		return new File(base, path);
	}
	
	public static void loadConfig() {
		config = loadYamlFile("config.yml");
		if (config == null)
		{
			Logger.error("&cConfig File doesn't exist! ");
		}
		else {
			if (config.getBoolean("color-logs", true))
			{
				COLOR_LOGS = true;
			}
			if (config.getBoolean("debug-mode", true))
			{
				DEBUG_MODE = true;
			}
			if (config.getBoolean("use-mysql", true))
			{
				useSQL = true;
			}
			if (config.getBoolean("player-send", true))
			{
				PLAYER_SEND = true;
			}
			
		}
		
	}
	// This ONLY loads the SQL Config INFO. It does NOT connect to mysql, this only returns the connection info. So it can connect to it mysql.
	public static ArrayList<String> loadMYSQL()
	{
		// This compiles an array, with the Login, The Username, and the password. then returns it.
		ArrayList<String> ConnUrl = new ArrayList<String>();
		
		ConnUrl.add("jdbc:mysql://" + config.getString("sql.host", "localhost") + ":" + config.getString("sql.port", "3306") + "/" + config.getString("sql.database", "database"));	
		ConnUrl.add(config.getString("sql.username", "username"));
		ConnUrl.add(config.getString("sql.password", "password"));
		return ConnUrl;
	}
	// Load the REGSPEC Config File
	public static YamlConfiguration loadYamlFile(String paramString)
	{
		File localFile1 = new File(getDirectory());
		File localFile2 = new File(localFile1, paramString);

		YamlConfiguration localYamlConfiguration = null;
		if (localFile2.exists()) {
			try
			{
				localYamlConfiguration = new YamlConfiguration();
				localYamlConfiguration.load(localFile2);
				Logger.debug("&a-- FILE FOUND: "+ localFile2 + " --");
			}
			catch (Exception localException)
			{
				localException.printStackTrace();
				Logger.debug("&c Could not load YML file");
			}
		}
		return localYamlConfiguration;
	}
	// This just saves the default Yml file called config.yml
	public static void saveFile(){
		
	}
	// This saves the specified YML file (This does not allow any kind of Comment tho. If comments are wanted use saveFile() )
	private static void saveYamlFile(YamlConfiguration paramYamlConfiguration, String paramString) {
		File localFile1 = new File(getDirectory());
		File localFile2 = new File(localFile1, paramString);
			    try
			    {
			      paramYamlConfiguration.save(localFile2);
			      Logger.info("Created new file:" + paramString);
			    }
			    catch (Exception localException)
			    {
			      localException.printStackTrace();
			      Logger.error("&c Failed to save YML file");
			    }
		
	}
}
