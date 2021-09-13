package net.yarr.regspec;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.yarr.regspec.Logger;
import net.yarr.regspec.hook.VaultHook;
import net.yarr.regspec.hook.WrldGrdHook;
import net.yarr.regspec.Database.SQConnector;
import net.yarr.regspec.configuration.RSConfFile;
import net.yarr.regspec.listener.PlayerListener;


//import net.pl3x.bukkit.regionperms.listener.PlayerListener;

public class RegSpec extends JavaPlugin {
	// Declare Section:
	public static PluginManager pm;
	public boolean failedDependencies = false;
	// This method is when RegSpec first loads.
	// World Guard Allows Custom Flags, but they can ONLY be registered / injected when it starts. Once it starts running it blocks ALL Custom Flags
	public void onLoad()
	{
		saveDefaultConfig();
		// If it fails it will throw a flag first
		Logger.debug("Catching WG While still loading..");
		WrldGrdHook.InjectFlags();
		// This saves the Config file if it doesn't exist.

	}
	public void onEnable()
	{
		// Check if there is any major errors on startup. EI WG Not found Vault not found etc. Returns false (did not fail)
		// If everything checks out, continue

		//pm.registerEvents(new PlayerListener(), this);
		RSConfFile.loadConfig();
		
		Logger.info("&a" + getName() + " v" + getDescription().getVersion() + " enabled! No Missing Dependencies! - This is kind of a lie didn't run through the check");
		// Start the listener!

		VaultHook.setupPermissions();
		//SQConnector.loadData();
		SQConnector.QueryArray("");
	    //VaultHook.failedSetupPermissions();	    
		pm = Bukkit.getPluginManager();
	    pm.registerEvents(new PlayerListener(), this);
	    
	    this.getCommand("rspec").setExecutor(new Commands(this));
	    
	    
		// 	Star the SQL Connector
		//SQConnector conct = new SQConnector ();
		//conct.loadData();
		//conct.QueryArray();
		//conct.QueryArray();
		//conct.QueryArray("test");
		// SQConnector.QueryArray("test");
		
	    // Insert test values into database:
		//SQConnector conct = new SQConnector ();
	    //conct.addNODE("test", "EG", "EasyKits.kits.survival");
	    //conct.addNODE("test", "EGRP", "arena");
	    //conct.addNODE("test", "EXR", "EasyKits.kits.survival");
	    //conct.addNODE("test", "EXGRP", "arena");
		
		//DBNodes nodes = new DBNodes ();
		//Logger.debug("&bRegions --- : " + nodes.getREGION()); 
		//SQConnector.this.loadData();
	}
	public boolean errorChck() {
		pm = Bukkit.getPluginManager();
		//Check for errors, if errors found Disable RegSpec.
		if (!pm.isPluginEnabled("WorldGuard"))
		{
			Logger.error("WorldGuard not installed/found!");
			failedDependencies = true;
		}
		if (!pm.isPluginEnabled("Vault"))
		{
			Logger.error("Vault not installed/found!");
			failedDependencies = true;
		}
		// Vault.. removed

		if (failedDependencies)
		{
			Logger.error("Critical Error - Pugin is disabling");
			pm.disablePlugin(this);
			return true;
		}
		return false;
	}

	public void onDisable()
	{
		//Logger.info(getName() + " disabled.");
	}
	public static RegSpec getPlugin()
	{
		return (RegSpec)getPlugin(RegSpec.class);
	}
	public void disPlugin()
	{
		Logger.error("Critical Error - Pugin is disabling");
		pm.disablePlugin(this);
	}
}