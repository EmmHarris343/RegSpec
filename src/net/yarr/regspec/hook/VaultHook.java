package net.yarr.regspec.hook;

//Vault dependicies
import net.milkbowl.vault.permission.Permission;
import net.yarr.regspec.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook
{
  private static Permission permission = null;
  public static PluginManager pm;
  
  public static boolean setupPermissions() {
      RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
      permission = rsp.getProvider();
      Logger.debug("&c Hooked Permissions - Vault");
      return permission != null;
  }
  public static void addPermission(Player player, String node, String world)
  {
	  if (permission != null) {
		  if (world != null)
		  {
			  permission.playerAdd(world, player, node);
		  }
		  else {
			  permission.playerAdd(null, player, node);
		  }
	  }
	  // Add permission to player - NODE
  }
  
  public static void removePermission(Player player, String node, String world)
  {
	  if (permission != null) {
		  if (world != null)
		  {
			  permission.playerRemove(world, player, node);
		  }
		  else {			  
			  permission.playerRemove(null, player, node);
		  }
		  
	  }
	  //remove permission from player - NODE
  }
  public static boolean inGroup(Player player, String group)
  {
	  // Check if the player is in the group specified
	  if (permission.playerInGroup(player, group)) {
		  return true;    	
	  }
	  return false;    
  }
  public static void addGroup(Player player, String group)
  {
	  if (permission != null) {
			  permission.playerAddGroup((String) null, player, group);

	  }
  }
  public static void removeGroup(Player player, String group)
  {
	  if (permission != null) {
		  permission.playerRemoveGroup((String) null, player, group);
	  }
  }
  public static Permission getPermissions() {
	  return permission;
  }

}
