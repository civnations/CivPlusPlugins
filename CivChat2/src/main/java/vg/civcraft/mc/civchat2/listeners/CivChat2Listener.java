package vg.civcraft.mc.civchat2.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/*
 * @author jjj5311
 *
 */
public class CivChat2Listener implements Listener {

	private CivChat2Manager chatman;

	public CivChat2Listener(CivChat2Manager instance) {

		chatman = instance;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
		playerDeathEvent.setDeathMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
		playerQuitEvent.setQuitMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
		if (!CivChat2.getInstance().getPluginConfig().getLoginAnnounce()) {
			playerJoinEvent.setJoinMessage(null);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent playerKickEvent) {
		playerKickEvent.setLeaveMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChatEvent(final AsyncPlayerChatEvent asyncPlayerChatEvent) {

		asyncPlayerChatEvent.setCancelled(true);
		// This needs to be done sync to avoid a rare deadlock due to minecraft
		// internals
		new BukkitRunnable() {

			@Override
			public void run() {

				String chatMessage = asyncPlayerChatEvent.getMessage();
				Player sender = asyncPlayerChatEvent.getPlayer();
				UUID chatChannel = chatman.getChannel(sender);
				Group groupChat = chatman.getGroupChatting(sender);

				if (chatChannel != null) {
					StringBuilder sb = new StringBuilder();
					Player receiver = Bukkit.getPlayer(chatChannel);
					if (receiver != null) {
						chatman.sendPrivateMsg(sender, receiver, chatMessage);
						return;
					} else {
						chatman.removeChannel(sender);
						String offlineMessage = sb.append(ChatColor.GOLD)
								.append("The player you were chatting with has gone offline,")
								.append(" you have been moved to regular chat").toString();
						sb.delete(0, sb.length());
						sender.sendMessage(offlineMessage);
						return;
					}
				}
				if (groupChat != null) {
					// Player is group chatting
					if (NameAPI.getGroupManager().hasAccess(groupChat, sender.getUniqueId(),
							PermissionType.getPermission("WRITE_CHAT"))) {
						chatman.sendGroupMsg(sender, groupChat, chatMessage);
						return;
						// Player lost perm to write in the chat
					} else {
						chatman.removeGroupChat(sender);
						sender.sendMessage(ChatColor.RED
								+ "You have been removed from groupchat because you were removed from the group or lost the permission required to groupchat");
					}
				}
				chatman.broadcastMessage(sender, chatMessage, asyncPlayerChatEvent.getFormat(),
						asyncPlayerChatEvent.getRecipients());
			}
		}.runTask(CivChat2.getInstance());
	}
}
