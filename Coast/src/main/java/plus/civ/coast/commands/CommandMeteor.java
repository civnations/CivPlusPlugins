package plus.civ.coast.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

		float radius = Config.DEFAULT_RADIUS;
		try {
			radius = Float.parseFloat(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage("Radius must be a number!");
		} catch (IndexOutOfBoundsException e) {}

		float speed = Config.DEFAULT_SPEED;
		try {
			speed = Float.parseFloat(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage("Speed must be a number!");
		} catch (IndexOutOfBoundsException e) {}

		Location loc = ((Player)sender).getLocation();
		// Calculate dx, dy, dz for meteor velocity
		double yaw = Math.toRadians(loc.getYaw());
		double pitch = Math.toRadians(loc.getPitch());
		double dxPreNormalized = Math.cos(pitch) * Math.sin(-yaw);
		double dyPreNormalized = Math.sin(-pitch);
		double dzPreNormalized = Math.cos(pitch) * Math.cos(yaw);
		double magnitude = Math.sqrt(Math.pow(dxPreNormalized, 2) + Math.pow(dyPreNormalized, 2) + Math.pow(dzPreNormalized, 2));
		float dx = (float)(speed * dxPreNormalized / magnitude);
		float dy = (float)(speed * dyPreNormalized / magnitude);
		float dz = (float)(speed * dzPreNormalized / magnitude);

		Coast.getInstance().summonMeteor(loc.getWorld(), (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), dx, dy, dz, radius);

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
