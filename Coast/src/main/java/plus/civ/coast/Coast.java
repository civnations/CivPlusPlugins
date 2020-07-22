package plus.civ.coast;

import org.bukkit.World;
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

	public void summonMeteor(World world, float x, float y, float z, float dx, float dy, float dz, float radius) {
		Meteor met = new Meteor(world, x, y, z, dx, dy, dz, radius);
		activeMeteors.add(met);
		met.runTaskTimer(Coast.getInstance(), Config.TICK_RATE, Config.TICK_RATE);
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
