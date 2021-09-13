package net.yarr.regspec.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
// Bukkit imports for players / listeners:
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

// WG Imports
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// Other classes imports
import net.yarr.regspec.Logger;
import net.yarr.regspec.hook.VaultHook;
import net.yarr.regspec.Database.DBNodes;
import net.yarr.regspec.Database.SQConnector;
import net.yarr.regspec.configuration.RSConfFile;

public class PlayerListener implements Listener {
	
	// Define the worldguardplugin (easier to call it)
	public static WorldGuardPlugin worldGuardPlugin;
	// The array list.
	public static ArrayList<DBNodes> RegionEntries = null;
	
	// Event Handlers: (Do  shit when it detects things)
    // When player moves
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        this.checkFlags(event.getPlayer(), event.getTo(), event.getFrom());
    }
    // When player teleports
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        this.checkFlags(event.getPlayer(), event.getTo(), event.getFrom());
    }
    // When player logs in    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final ApplicableRegionSet toRegions = this.getApplicableRegions(player.getLocation());
        if (toRegions == null) {
            return;
        }
        this.enteringRegions(player, toRegions.getRegions());
    }

    private void checkFlags(final Player player, final Location to, final Location from) {
    	// If player didn't actually move, return nothing.
    	if (to == null || from == null || (from.getWorld().getName().equals(to.getWorld().getName()) && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
    		return;
    	}
    	// Declare things:
    	// This is the entering / exiting region values
    	final Set<ProtectedRegion> cllctIsRegion = new HashSet<ProtectedRegion>();
    	final Set<ProtectedRegion> cllctWasRegion = new HashSet<ProtectedRegion>();

    	// IMPORTANT!!
    	// Was Regions  :  Means --> Was in Region (s)?
    	// Is Regions  :  Means --> Is it now in a Region (s) ? 
    	// Runs: getApplicableRegions(from) and Runs: getApplicableRegions(to) 
    	// Which sends the move to / from location to getApplicableRegions. Which will see if it is/was inside a Region or not. 
    	// ---- > If yes returns Worldguard Region Object ----> If no, it returns null making the objects null.
    	final ApplicableRegionSet WasRegions = this.getApplicableRegions(from); 
    	final ApplicableRegionSet IsRegions = this.getApplicableRegions(to);
    	// This is the declare needed to assemble the enteringRegions / ExitingRegions Object? Sure
    	final ApplicableRegionSet set = null;
    	final ApplicableRegionSet set2 = null;
    	// Player not in region
    	if (WasRegions == null && IsRegions == null) {
    		//Logger.debug("&cBoth IsReg and WasReg = null - &a Player is not in a Region!");
    		return;
    	}
    	// Player Entered a Region from a unprotected area.
    	if (IsRegions != null & WasRegions == null) {
    		Logger.debug("&9IsReg =! null, WasReg = null ---- Did you leave a unprotected area and enter into a region?");
    		// This converts the ApplicableRegionSet to a ProtectedRegion. Allows to get the region ID (the name of it and many other important things!)
    		cllctIsRegion.addAll((Collection<? extends ProtectedRegion>)IsRegions.getRegions().stream().filter(region -> set == null || !set.getRegions().contains(region)).collect(Collectors.toList()));
    		enteringRegions(player, cllctIsRegion);
    		Logger.debug("&aREG = " + cllctIsRegion);
    	}
    	// Player Left a region
    	if (WasRegions != null & IsRegions == null) {
    		Logger.debug("&9IsReg == Null, WasReg != null -- Did you just leave a region?");
    		// This converts the ApplicableRegionSet to a ProtectedRegion. Allows to get the region ID (the name of it and many other important things!)
    		cllctWasRegion.addAll((Collection<? extends ProtectedRegion>)WasRegions.getRegions().stream().filter(region -> set == null || !set.getRegions().contains(region)).collect(Collectors.toList()));
    		exitingRegions(player, cllctWasRegion);
    	}
    	// Player is either in the same region as before, or left a region into another one (priority regions or overlapping regions)
    	if (WasRegions != null & IsRegions != null) {
    		// This converts the ApplicableRegionSet to a ProtectedRegion. Allows to get the region ID (the name of it and many other important things!)
    		//Logger.debug("&dBoth WasReg and IsReg != null -- Are you in the same region? Or enter a new one?");
    		cllctIsRegion.addAll((Collection<? extends ProtectedRegion>)IsRegions.getRegions().stream().filter(region -> set == null || !set.getRegions().contains(region)).collect(Collectors.toList()));
    		cllctWasRegion.addAll((Collection<? extends ProtectedRegion>)WasRegions.getRegions().stream().filter(region -> set == null || !set.getRegions().contains(region)).collect(Collectors.toList()));
    		this.checkRegion(player, cllctIsRegion, cllctWasRegion);
    	}
    }
    private void checkRegion(final Player player, final Set<ProtectedRegion> cllctIsRegion, final Set<ProtectedRegion> cllctWasRegion) {
    	// Check if the Region the player is in, Is the same one, or a different one
    	//final ProtectedRegion IsRegion = cllctIsRegion.
    	for (final ProtectedRegion IsRegion : cllctIsRegion) {
        	for (final ProtectedRegion WasRegion : cllctWasRegion) {
        		if (IsRegion.getId() == WasRegion.getId()) {
        			//Logger.debug("&b---->CHECKING... Region Did not change! - Doing nothing");        			
        		}
        		else {
        			//Logger.debug("&b----->CHECKING... The Region Did change! &aSending to: Entering Regions Method");
        			enteringRegions(player, cllctIsRegion);
        		}
        	}
    	}
    }
    private void enteringRegions(final Player player, final Set<ProtectedRegion> enteringRegions) {
    	// This makes region = enteringRegions and runs through the loop
    	Logger.debug("&dEnteringRegions called");
    	String world = null;
    	String node_r = null;
    	String node_g = null;
    	String node_grp = null;

    	int gave_perm = 0;
    	int removed_perm = 0;
    	int assign_grp = 0;

    	for (final ProtectedRegion region : enteringRegions) {
    		//Logger.debug("&dBack in For Loop");
    		String r_name = region.getId();

    		// Check if the Query Array, Array list is empty first.
    		if (SQConnector.QueryArray(r_name) != null){
    			RegionEntries = SQConnector.QueryArray(r_name);
    			for (int i=0; i < RegionEntries.size(); i++) {
    				Logger.debug("&2 Type" +RegionEntries.get(i).getTYPE());
    				if (RegionEntries.get(i).getTYPE().contentEquals("ER")) {
    					// If the RegionEntries current i type = ER It means ENTER REMOVE PERM.
    					//Logger.debug("&d Type IS ER VERIFY -->" + RegionEntries.get(i).getTYPE());
    					node_r = RegionEntries.get(i).getNODE();
    					world = RegionEntries.get(i).getWORLD();
    					if (node_r != null && !node_r.isEmpty() && player.hasPermission(node_r)) {
    						// REMOVE THE PERMISSION FROM THE PLAYER! 
    						if (world != null)
    						{
        						Logger.debug("Enter: Removing permission from " + player.getName() + " : " + node_r);
        						VaultHook.removePermission(player, node_r, world);
        						Logger.debug("Removed Success");
        						// Set that A Permission was given.
        						removed_perm = 1;
    						}
    						else {
        						Logger.debug("Enter: Removing permission from " + player.getName() + " : " + node_r);
        						VaultHook.removePermission(player, node_r, null);
        						Logger.debug("Removed Success");
        						// Set that A Permission was given.
        						removed_perm = 1;
    						}
    					}
    				}
    				if (RegionEntries.get(i).getTYPE().contentEquals("EG")) {
    					// If the RegionEntries current i type = EG It means ENTER GIVE PERM
    					//Logger.debug("&d Type IS EG VERIFY -->" + RegionEntries.get(i).getTYPE());
    					node_g = RegionEntries.get(i).getNODE();
    					world = RegionEntries.get(i).getWORLD();
    					if (node_g != null && !node_g.isEmpty() && !player.hasPermission(node_g)) {
    						if (world != null)
    						{
    							// Give Permissions to player:
    							// World is empty... Ignore world name and add the permission globally
    							Logger.debug("&dEnter: Giving Permissions to " + player.getName() + " : " + node_g);
    							// 	give the permission  
    							VaultHook.addPermission(player, node_g, world);	
    							gave_perm = 1;
    						}
    						else {
    							// Give Permissions to player:
    							Logger.debug("&dEnter: Giving Permissions to " + player.getName() + " : " + node_g);
    							// Remove the 
    							VaultHook.addPermission(player, node_g, (String) null);	
    							gave_perm = 1;
    						}
    					}
    				}
    				if (RegionEntries.get(i).getTYPE().contentEquals("EGRP")) {
    					// World isn't needed for groups right?
    					
    					// If the RegionEntries current i type = EGRP Enter Group Assign.
    					//Logger.debug("&a Type IS EGRP VERIFY-->" + RegionEntries.get(i).getTYPE());
    					node_grp = RegionEntries.get(i).getNODE();
    					Logger.debug("Group yes / no" + VaultHook.inGroup(player, node_grp));
    					if (node_grp != null && !node_grp.isEmpty() && !VaultHook.inGroup(player, node_grp)) {
        						// Assign the Player to a group 
    							Logger.debug("&bPLWorld isn't null");
        						Logger.debug("&dEnter: Assigning to Group " + player.getName() + " : " + node_grp);
        						// Doesn't matter what world location the player is in.
        						VaultHook.addGroup(player, node_grp); 					
        						assign_grp = 1;    							    							
    					}
    				}
    				if (i == (RegionEntries.size() -1 ))
    				{
    					// Reached end of For Loop.
    					node_r = null;
    					node_g = null;
    					node_grp = null;
    					if (RSConfFile.PLAYER_SEND == true) {
    						if (removed_perm == 1)
    						{
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou Entered a Region with Specific Permissions"));
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b     Permission (s) have been &3Removed"));
    							removed_perm = 0;
    						}
    						if (gave_perm == 1) {
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou Entered a Region with Specific Permissions"));
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b     Permission (s) have been &3Granted"));
    							gave_perm = 0;
    						}
    						if (assign_grp == 1)
    						{
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou Entered a Region with Group Specific Permissions"));
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b     You have been &3assigned &bto a Group"));
    							assign_grp = 0;
    						}
    					}
    					RegionEntries.clear();
    				}
    			}	
    		}
    		else {
    			Logger.debug("&c Looks like there is no Entries for this Region.");
    		}

    	}
    }	
    private void exitingRegions(final Player player, final Set<ProtectedRegion> exitingRegions) {
    	// This is the exit region part of things (removes perms groups  etc when they leave the region!)
    	Logger.debug("&d<PL>ExitingRegions called");
    	String world = null;
    	String node_r = null;
    	String node_g = null;
    	String node_grp = null;

    	int gave_perm = 0;
    	int removed_perm = 0;
    	int removed_grp = 0;

    	for (final ProtectedRegion region : exitingRegions) {
    		//Logger.debug("&dIn Loop PL Exiting Regions");
    		String r_name = region.getId();

    		//Logger.debug("&dAssigning RegionEntries");
    		if (SQConnector.QueryArray(r_name) != null){
    			RegionEntries = SQConnector.QueryArray(r_name);
    			for (int i=0; i < RegionEntries.size(); i++) {
    				Logger.debug("&2 Type" +RegionEntries.get(i).getTYPE());
    				// Now see what the names of things are
    				if (RegionEntries.get(i).getTYPE().contentEquals("EXR")) {
    					//Logger.debug("&d Type IS EXR VERIFY-->" + RegionEntries.get(i).getTYPE());
    					node_r = RegionEntries.get(i).getNODE();
    					world = RegionEntries.get(i).getWORLD();
    					if (node_r != null && !node_r.isEmpty() && player.hasPermission(node_r)) {
    						if (world != null)
    						{
        						// REMOVE THE PERMISSION FROM THE PLAYER!
        						// CHECKING FOR WORLD IS NOT NEEDED WHEN REMOVING PERMISSIONS!
        						Logger.debug("EXIT: Removing permission from " + player.getName() + " : " + node_r);
        						VaultHook.removePermission(player, node_r, world);
        						Logger.debug("&dRemoved Successfully");
        						removed_perm = 1;
    						}
    						else {
        						// REMOVE THE PERMISSION FROM THE PLAYER!
        						// CHECKING FOR WORLD IS NOT NEEDED WHEN REMOVING PERMISSIONS!
        						Logger.debug("EXIT: Removing permission from " + player.getName() + " : " + node_r);
        						VaultHook.removePermission(player, node_r, (String) null);
        						Logger.debug("&dRemoved Successfully");
        						removed_perm = 1;
    						}

    					}
    				}
    				if (RegionEntries.get(i).getTYPE().contentEquals("EXG")) {
    					// If the RegionEntries current I type = EG It means ENTER GIVE PERM
    					// Don't comment these out: These are needed to load what region type and world the current For loop i is at
    					node_g = RegionEntries.get(i).getNODE();
    					world = RegionEntries.get(i).getWORLD();
    					if (node_g != null && !node_g.isEmpty() && !player.hasPermission(node_g)) {
    						if (world != (String)null)
    						{
    							
    							// Give Permissions to player:
    							// World is empty... Ignore world name and add the permission globally 
    							Logger.debug("&dEnter: Giving Permissions to " + player.getName() + " : " + node_g);
    							// 	give the permission  
    							VaultHook.addPermission(player, node_g, world);
    							gave_perm = 1;
    						}
    						else {
    							// Give Permissions to player:
    							Logger.debug("&dEnter: Giving Permissions to " + player.getName() + " : " + node_g);
    							// Add the Permissoin
    							VaultHook.addPermission(player, node_g, (String) null);
    							gave_perm = 1;
    						}
    					}
    				}
    				if (RegionEntries.get(i).getTYPE().contentEquals("EXGRP")) {
    					// If the RegionEntries current I type = EGRP Enter Group Assign.
    					//Logger.debug("&a Type IS EXGRP VERIFY-->" + RegionEntries.get(i).getTYPE());
    					// Don't comment this out: These are needed to load what region type and world the current For loop i is at
    					node_grp = RegionEntries.get(i).getNODE();
    					if (node_grp != null && !node_grp.isEmpty() && VaultHook.inGroup(player, node_grp)) {
        						// Assign the Player to a group 
    							Logger.debug("&bPLWorld isn't null");
        						Logger.debug("&dEXIT: Removing from Group " + player.getName() + " : " + node_grp);
        						VaultHook.removeGroup(player, node_grp);
        						removed_grp = 1;	
    					}
    				}
    				if (i == (RegionEntries.size() -1 ))
    				{
    					// Reached end of For Loop.
    					node_r = null;
    					node_g = null;
    					node_grp = null;
    					if (RSConfFile.PLAYER_SEND == true) {
    						if (gave_perm == 1) {
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou left a Region that had Region Specific Permissions"));
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b     Permission (s) have been &3Granted"));
    							gave_perm = 0;
    						}
    						if (removed_perm == 1)
    						{
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou left a Region that had Region Specific Permissions"));
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b     Permission (s) have been &3Removed"));
    							removed_perm = 0;
    						}
    						if (removed_grp == 1)
    						{
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bYou left a Region that had Group Specific Permissions"));
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b     You have been &3Removed &bfrom a Group"));
    							removed_grp = 0;
    						}
    					}
    					RegionEntries.clear();
    				}
    			}
    		}
    		else {
    			Logger.debug("&c    -<PL>Looks like there is no Entries for this Region.");
    		}
    	}
    }
    public WorldGuardPlugin getWorldGuard() {
    	worldGuardPlugin = (WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
    	Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	    return (WorldGuardPlugin) plugin;
	}
    // This should be checking WG to find all regions in the world based on players location.
    // Then once it has found all regions, Send back all regions the player is in. (I think)
    
    
    // This code gets if a player is inside a region. based on location data. Returns null if player is not in a region.
	private ApplicableRegionSet getApplicableRegions(final Location location) {
		RegionContainer container = getWorldGuard().getRegionContainer();
    	final RegionManager regionManager = container.get(location.getWorld());
    	// Check if the region manager couldn't get info for the location entered.
    	if (regionManager == null) {
        	Logger.error("&cNo regions in world found!");
        	return null;
    	}
    	ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitUtil.toVector(location));
    	
    	// Run a for loop to get all regions returned (this is needed because sometimes you may be in overlapping regions)
    	// WG API SITE: If your interest is in getting the list of regions, ApplicableRegionSet implements Iterable<ProtectedRegion> so you can loop over it:
    	for (ProtectedRegion region : set) {
    		// Inside region
    		if(region.getId() != null){
        		//Logger.debug("Inside a region. Returning Region Object");
        		//return region;
        		return set;
        		
    			//Logger.debug("The RM Applicable regions --->" + regionManager.getApplicableRegions(BukkitUtil.toVector(location)));
    		}

    	}

        // Found Regions, Return regions where location is inside
    	//Logger.debug("There is regions. But doesn't look like currently in one");
        //return null;
    	return null;
    }
	
	

}
