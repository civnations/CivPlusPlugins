package plus.civ.coast;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import plus.civ.coast.commands.CommandMeteor;
import vg.civcraft.mc.civmodcore.ACivMod;

import java.util.ArrayList;

public class Coast extends ACivMod {
	private static Coast instance;

    private NmsHellManager nmsHellManager;

	private ArrayList<Meteor> activeMeteors;

    public void onEnable() {
    	super.onEnable();
    	instance = this;
        this.nmsHellManager = new NmsHellManager();
		this.activeMeteors = new ArrayList<>();
		this.getStandaloneCommandHandler().registerCommand(new CommandMeteor());
    }

    public void onDisable() {
        this.cancelAllMeteorsInFlight();
    }

	/**
	 * Create a new meteor
	 * @param x The center x coordinate
	 * @param y The center y coordinate
	 * @param z The center z coordinate
	 * @param pitch The pitch of the meteor's velocity
	 * @param yaw The yaw of the meteor's velocity
	 * @param speed The meteor's speed (magnitude of velocity)
	 * @param radius The radius of the meteor in blocks (decimals can be used to mess with the exact shape)
	 * @param delay The amount of minutes to wait before launching the meteor
	 * @param inv The Inventory that will be put in the meteor's chest after it lands
	 */
	public void summonMeteor(World world, float x, float y, float z, float pitch, float yaw, float speed, float radius, float delay, Inventory inv) {
		// Rounded coordinates for notification
		int xRounded = Math.round(x / 100) * 100;
		int zRounded = Math.round(z / 100) * 100;
		// Notify the players
		String message = String.format("%sA meteor has been spotted in the sky above (%d, %d)! It will fall in %.0f minutes!", ChatColor.AQUA, xRounded, zRounded, delay);
		for (Player p : world.getPlayers()) {
			p.sendMessage(message);
			p.sendTitle(message, null, 6, 80, 20);
		}
		// Summon the meteor when it's time
		int delayTicks = (int)(delay * 60 * 20);
		new BukkitRunnable() {
			@Override
			public void run() {
				// Summon the meteor
				Meteor met = new Meteor(world, x, y, z, pitch, yaw, speed, radius, inv);
				activeMeteors.add(met);
				met.runTaskTimer(Coast.getInstance(), Config.TICK_RATE, Config.TICK_RATE);
				// Notify the players
				String message = String.format("%sA meteor is falling near (%d, %d)!", ChatColor.RED, xRounded, zRounded);
				for (Player p : world.getPlayers()) {
					p.sendMessage(message);
					p.sendTitle(message, null, 6, 80, 20);
				}
			}
		}.runTaskLaterAsynchronously(this, delayTicks);
	}

	public void cancelAllMeteorsInFlight() {
		for (Meteor m : activeMeteors) {
			this.cancelMeteor(m);
		}
	}

	public void cancelMeteor(Meteor meteor) {
		// TODO implement
	}
    
    public static Coast getInstance() {
    	return instance;
	}

	public NmsHellManager getNmsHellManager() {
    	return this.nmsHellManager;
	}
}
