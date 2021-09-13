package net.yarr.regspec.hook;

import javax.lang.model.element.Element;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

//World Guard required fields:
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;

import net.yarr.regspec.RegSpec;
import net.yarr.regspec.Logger;

// This hook into world guard, Injects flags etc
public class WrldGrdHook extends JavaPlugin {
	// Declare the required fields: 
	public static WorldGuardPlugin WGPlugin = null;

	// This gets the worldguard plugin, and ensures it doesn't return null
	public static void InjectFlags()
	{
		// Declare WG PLUGIN, so we don't have to say it repeatedly for each registry.
		//WGPlugin = WorldGuardPlugin(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"));
		WorldGuardPlugin WGPlugin = WGBukkit.getPlugin();
		if (WGPlugin != null) 
		{
			Logger.debug("WORLD GUARD FOUND - ATTEMPTING INJECTION");
			// Only 2 flags needed for WG Regions. 
			final Flag<State> perm_enter = new StateFlag("perm-enter", true);
			final Flag<State> perm_exit = new StateFlag("perm-exit", true);
			// 2 Flags for additional Coding. Assign a group Or Remove From Group. Allow For Enter and Exit?
			final Flag<State> grp_enter = new StateFlag("group-enter", true);
			final Flag<State> grp_exit = new StateFlag("group-exit", true);
			final Flag<State> lag = new StateFlag("xx--injected--xx", true);
			//FlagRegistry registry = ((WorldGuardPlugin)WorldGuardPlugin.getPlugin(WorldGuardPlugin.class)).getFlagRegistry();
			// ^ that may not be needed. Arbitrary code.
			try 
			{                 
				// try to register our flag with the registry API.
				WGPlugin.getFlagRegistry().register(perm_enter);
				WGPlugin.getFlagRegistry().register(perm_exit);
				// Adding flags for assign groups on enter /exit. Not sure if they will implemented
				WGPlugin.getFlagRegistry().register(grp_enter);
				WGPlugin.getFlagRegistry().register(grp_exit);
				// This one is for debug Purposes.  This gives an easy to find Flag inside WG Regions (the xx-injected-xx is easy to find in a huge list)
				WGPlugin.getFlagRegistry().register(lag);
				// NOTE, This may need to be fixed. If it can't register a flag because it already exists.. Does that mean It stops at that flag and throws an error? 
				// If yes.. Some may not register if they were not already in use. May require Each register to have a Try / Catch Loop.
				Logger.debug("Injection complete, Please Verify");
			} catch (FlagConflictException e) 
			{
				//Flag already exists - Already Registered? Flag saved in a region? Tried Registering twice?
				Logger.error("Fag Inject Conflict! (Flag is already Registered) - WG Kept Active Custom Flag because its in use by a region?");
			}
		}
		else {
			Logger.error("WorldGuard Not found injection failed!");
		}
	}
}

