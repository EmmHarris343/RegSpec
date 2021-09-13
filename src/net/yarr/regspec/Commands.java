package net.yarr.regspec;

import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import net.yarr.regspec.Database.DBNodes;
import net.yarr.regspec.Database.SQConnector;
import net.yarr.regspec.configuration.RSConfFile;

public class Commands implements CommandExecutor {
	//This is where the commands for the plugin will be placed.
	
	private final RegSpec plugin;
	public static ArrayList<DBNodes> Entries = null;

	public Commands(RegSpec plugin) {
		this.plugin = plugin; // Store the plugin in situations where you need it.
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// implementation exactly as before...
		if (cmd.getName().equalsIgnoreCase("rspec")) {
			if (args.length == 1)
			{
				if (args[0] == "hi") {
					return true;
				}
				if (args[0].equalsIgnoreCase("help"))
				{
					// Command /rspec help was called, sending to the print out help file.
					cmdhelp(sender);
		            return true;				
				}
				if (args[0].equalsIgnoreCase("list"))
				{
					// The List command was called;
					//	This makes a nifty little arrow when making a second line
					char c = 0x2937; 
					String arrow = String.valueOf(c);
					// Explain what the ER EG Etc Mean. Then Print Each Entry To the Player					
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6- &eLegend for 'type' &6-"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eEG = &aEnter Give &eER = &aEnter Remove &eEXG = &aExit Give &eEXR = &aExit Remove "));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eEGRP = &aEnter Assign Group &eEXGRP = &aEnter Remove Group"));
					Entries = SQConnector.loadData();;
					for (int i=0; i < Entries.size(); i++) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bID &3" + Entries.get(i).getID() + " &bREGION &3" + Entries.get(i).getREGION() + " &bType &e" + Entries.get(i).getTYPE()));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "    &a&l" + arrow + " &bNode/Group &3 " + Entries.get(i).getNODE()));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "    &a&l" + arrow + " &bWorld: &3 " + Entries.get(i).getWORLD() ));
					}
					Entries.clear();
		            return true;
				}
				if (args[0].equalsIgnoreCase("reload")) {
					// The reload command was issued.. Reload the config, connection and reinitialize
					RSConfFile.loadConfig();
					SQConnector.reloadSQL();
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloading config and sql connection"));
					return true;
				}
			}
			if (args.length == 2) {
				// The list region name command (/rspec list region name)
				if (args[0].equalsIgnoreCase("list"))
				{
					// The List command was called;
					//	This makes a nifty little arrow when making a second line
					char c = 0x2937; 
					String arrow = String.valueOf(c);
					// Explain what the ER EG Etc Mean. Then Print Each Entry To the Player
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6- &eLegend for 'type' &6-"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eEG = &aEnter Give &eER = &aEnter Remove &eEXG = &aExit Give &eEXR = &aExit Remove "));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eEGRP = &aEnter Assign Group &eEXGRP = &aEnter Remove Group"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b- Showing Results for &3" + args[1].toLowerCase() + " &b-"));
					Entries = SQConnector.QueryArray(args[1].toLowerCase());
					for (int i=0; i < Entries.size(); i++) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bID &3" + Entries.get(i).getID() + " &bREGION &3" + Entries.get(i).getREGION() + " &bType &e" + Entries.get(i).getTYPE()));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "    &a&l" + arrow + " &bNode/Group &3 " + Entries.get(i).getNODE()));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "    &a&l" + arrow + " &bWorld: &3 " + Entries.get(i).getWORLD() ));
					}
					Entries.clear();
		            return true;
				}
				if (args[0].equalsIgnoreCase("del"))
				{
					// Delete an entry id number.
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bDeleted Entry-> &3" + args[1].toLowerCase()));
					SQConnector.delEntry(args[1].toLowerCase());
		            return true;
				}				
			}
			if (args.length == 3) {
				// This should ONLY be rspec list <region name>
				if (args[0].equalsIgnoreCase("egroup"))
				{
					SQConnector.addNODE(args[1], "EGRP", args[2], null);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCrated Entry - &3Enter Assign Group"));
					return true;
				}
				if (args[0].equalsIgnoreCase("exgroup"))
				{
					SQConnector.addNODE(args[1].toLowerCase(), "EXGRP", args[2], null);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCrated Entry - &3Exit Group Remove"));
		            return true;
				}
			}
			// No world Argument.
			if (args.length == 4) {
				if (args[0].equalsIgnoreCase("enter"))
				{
					// Send to a create Method depending upon arguments.
					// Player sender, Region Name, Type Be it give / remove, node, world
					createEntry(sender, args[1].toLowerCase(), args[2].toLowerCase(), args[3].toLowerCase(), null);
					return true;
				}
				if (args[0].equalsIgnoreCase("exit"))
				{
					// Send to a create Method depending upon arguments.
					createExit(sender, args[1].toLowerCase(), args[2].toLowerCase(), args[3].toLowerCase(), null);
					return true;
				}
			}
			// Allow for world Argument.
			if (args.length == 5) {
				if (args[0].equalsIgnoreCase("enter"))
				{
					// Send to a create Method depending upon arguments.
					createEntry(sender, args[1].toLowerCase(), args[2].toLowerCase(), args[3].toLowerCase(), args[4].toLowerCase());
					return true;
				}
				if (args[0].equalsIgnoreCase("exit"))
				{
					// Send to a create Method depending upon arguments.
					createExit(sender, args[1].toLowerCase(), args[2].toLowerCase(), args[3].toLowerCase(), args[4].toLowerCase());
					return true;
				}
			}
		}
		return false;		
	}
	public void createEntry(CommandSender sender, String r_name, String type, String node, String world)
	{		
		String f_type;
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bOutput: " + r_name + type + node + world));
		if(type.equalsIgnoreCase("give")){
			f_type = "EG";
			if (world != null)
			{
				// No world specified. Ignoring. 
				SQConnector.addNODE(r_name, f_type, node, world);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bWorld was not null"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Enter Give - For a Specific World"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node + "&b World: &3" + world));
			}
			else {
				SQConnector.addNODE(r_name, f_type, node, null);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bWorld was null"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Enter Give"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node));
			}
		}
		if(type.equalsIgnoreCase("remove")) {
			f_type = "ER";
			if (world != null)
			{
				// No world specified. Ignoring. 
				SQConnector.addNODE(r_name, f_type, node, world);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bWorld was not null"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Enter Remove - Specific world"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node + "&b World: &3" + world));
			}
			else {
				SQConnector.addNODE(r_name, f_type, node, null);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bWorld was null"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Enter Remove"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node));
			}
		}
	}
	public void createExit(CommandSender sender, String r_name, String type, String node, String world)
	{
		String f_type;
		if(type.equalsIgnoreCase("give")) {
			f_type = "EXG";
			if (world != null)
			{
				// No world specified. Ignoring. 
				SQConnector.addNODE(r_name, f_type, node, world);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bWorld was not null"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Exit Give - Specific world"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node + "&b World: &3" + world));
			}
			else {
				// World specified. entering: 
				SQConnector.addNODE(r_name, f_type, node, null);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bWorld was null"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Exit Give"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node));
			}
		}
		if (type.equalsIgnoreCase("remove")) {
			f_type = "EXR";
			if (world != null)
			{
				SQConnector.addNODE(r_name, f_type, node, world);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Exit Remove"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node + "&b World: &3" + world));
			}
			else {
				SQConnector.addNODE(r_name, f_type, node, null);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCreated Region Entry - &3Exit Remove"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bNode: &3" + node));				
			}
		}
	}
	public void cmdhelp(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6-----[&eRegSpec Commands&6]-----"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec help - &aShows this Help file"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec list - &aLists All Regions &2Region, the type, node or group"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec list <region-name> - &aLists all Regions with the provided Region-Name &2including: type, node or group"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec <enter/exit> <region> <give/remove> <node> <world>"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCreates a new Entry for Region - &2On Player Region Enter or Exit; What Permission to give or take; What The permission is; For What World (World can be blank) "));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPermission Node Must Match node of plugin! &2Example Node: &aeasykits.kits.armordiamond"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec egroup <region> <groupname> - &aOn Player Region Enter - &2Assign player to a specific group"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec exgroup <region> <groupname> - &aOn Player Region Exit - &2Remove player from a specific group"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec del <id> - &aDeletes an Entire Entry - &2Use the ID number to deleted the entry for that region"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c/rspec reload - &aReloads Config File - &2Reloads config, and re-initializes plugin"));
	}
}
