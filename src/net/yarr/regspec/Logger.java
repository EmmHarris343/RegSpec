package net.yarr.regspec;

import net.yarr.regspec.configuration.RSConfFile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger
{
  public static void log(String msg)
  {
    msg = ChatColor.translateAlternateColorCodes('&', "&3[&d" + RegSpec.getPlugin().getName() + "&3]&r " + msg);
    if (!RSConfFile.COLOR_LOGS) {
    	// if Color Logs = false.. Strip all colors:
      msg = ChatColor.stripColor(msg);
    }
    Bukkit.getServer().getConsoleSender().sendMessage(msg);
  }  
  public static void debug(String msg)
  {
    if (RSConfFile.DEBUG_MODE) {
    	// If Debug Logs = True then Print them. Otherwise print nothing.
      log("&7[&eDEBUG&7]&e " + msg);
    }
  }
  
  public static void warn(String msg)
  {
    log("&e[&6WARN&e]&6 " + msg);
  }
  
  public static void error(String msg)
  {
    log("&e[&4ERROR&e]&c " + msg);
  }
  
  public static void info(String msg)
  {
    log("&e[&fINFO&e]&r " + msg);
  }
}
