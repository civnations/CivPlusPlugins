package plus.civ.coast.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import plus.civ.coast.Coast;
import plus.civ.coast.Config;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

import java.util.ArrayList;
import java.util.List;

@CivCommand(id = "meteor")
public class CommandMeteor extends StandaloneCommand {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can run this command!");
			return false;
		}

		float radiusTemp = Config.DEFAULT_RADIUS;
		try {
			radiusTemp = Float.parseFloat(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage("Radius must be a number!");
			return false;
		} catch (ArrayIndexOutOfBoundsException e) { }
		// This has to be final because later we want to pass it into an anonymous listener
		final float radius = radiusTemp;

		float speed = Config.DEFAULT_SPEED;
		try {
			speed = Float.parseFloat(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage("Speed must be a number!");
			return false;
		} catch (IndexOutOfBoundsException e) {}

		final Player p = (Player)sender;
		final Location loc = p.getLocation();
		// Calculate dx, dy, dz for meteor velocity
		double yaw = Math.toRadians(loc.getYaw());
		double pitch = Math.toRadians(loc.getPitch());
		double dxPreNormalized = Math.cos(pitch) * Math.sin(-yaw);
		double dyPreNormalized = Math.sin(-pitch);
		double dzPreNormalized = Math.cos(pitch) * Math.cos(yaw);
		double magnitude = Math.sqrt(Math.pow(dxPreNormalized, 2) + Math.pow(dyPreNormalized, 2) + Math.pow(dzPreNormalized, 2));
		final float dx = (float)(speed * dxPreNormalized / magnitude);
		final float dy = (float)(speed * dyPreNormalized / magnitude);
		final float dz = (float)(speed * dzPreNormalized / magnitude);

		// Create the inventory that will go in the meteor
		Inventory meteorChestInv = Bukkit.getServer().createInventory(p, 27, "Meteor");
		p.openInventory(meteorChestInv);
		// Register a listener for when the player closes the inv, and unregister it after it goes off
		Listener invCloseListener = new Listener() {
			@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
			public void onInventoryClose(InventoryCloseEvent event) {
				if (event.getPlayer() != p) {
					return;
				}
				if (event.getInventory() != meteorChestInv) {
					return;
				}

				Coast.getInstance().summonMeteor(loc.getWorld(), (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), dx, dy, dz, radius, meteorChestInv);

				HandlerList.unregisterAll(this);
			}
		};
		Bukkit.getServer().getPluginManager().registerEvents(invCloseListener, Coast.getInstance());

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
