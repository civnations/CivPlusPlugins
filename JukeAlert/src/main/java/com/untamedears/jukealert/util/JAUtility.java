package com.untamedears.jukealert.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.namelayer.permission.PermissionType;

// Static methods only
public final class JAUtility {

	private JAUtility() {

	}

	public static Snitch findClosestSnitch(Location loc, PermissionType perm, UUID player) {
		Snitch closestSnitch = null;
		double closestDistance = Double.MAX_VALUE;
		Collection<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().getSnitchesCovering(loc);
		for (Snitch snitch : snitches) {
			if (snitch.hasPermission(player, perm)) {
				double distance = snitch.getLocation().distanceSquared(loc);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestSnitch = snitch;
				}
			}
		}
		return closestSnitch;
	}

	public static Snitch findLookingAtOrClosestSnitch(Player player, PermissionType perm) {

		Snitch cursorSnitch = getSnitchUnderCursor(player);
		if (cursorSnitch != null && cursorSnitch.hasPermission(player, perm)) {
			return cursorSnitch;
		}
		return findClosestSnitch(player.getLocation(), perm, player.getUniqueId());
	}

	public static Snitch getSnitchUnderCursor(Player player) {
		SnitchManager snitchMan = JukeAlert.getInstance().getSnitchManager();
		Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
		while (itr.hasNext()) {
			Block block = itr.next();
			Snitch found = snitchMan.getSnitchAt(block.getLocation());
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public static TextComponent genTextComponent(Snitch snitch) {
		String name = snitch.getName().isEmpty() ? snitch.getType().getName() : snitch.getName();
		TextComponent textComponent = new TextComponent(ChatColor.AQUA + name);
		addSnitchHoverText(textComponent, snitch);
		return textComponent;
	}

	public static void addSnitchHoverText(TextComponent text, Snitch snitch) {
		StringBuilder sb = new StringBuilder();
		Location loc = snitch.getLocation();
		sb.append(String.format("%sLocation: %s(%s) [%d %d %d]%n", ChatColor.GOLD, ChatColor.AQUA,
				loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		if (!snitch.getName().isEmpty()) {
			sb.append(String.format("%sName: %s%s%n", ChatColor.GOLD, ChatColor.AQUA, snitch.getName()));
		}
		sb.append(String.format("%sGroup: %s%s%n", ChatColor.GOLD, ChatColor.AQUA, snitch.getGroup().getName()));
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(sb.toString()).create()));
	}

	public static Material parseMaterial(String id) {
		try {
			return Material.valueOf(id);
		} catch (IllegalArgumentException e) {
			return Material.STONE;
		}
	}
	
	public static boolean isSameWorld(Location loc1, Location loc2) {
		return loc1.getWorld().getUID().equals(loc2.getWorld().getUID());
	}

	public static String formatLocation(Location location, boolean includeWorld) {
		if (includeWorld) {
			return String.format("[%s %d %d %d]", location.getWorld().getName(), location.getBlockX(), location.getBlockY(),
					location.getBlockZ());
		}
		return String.format("[%d %d %d]", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
}
