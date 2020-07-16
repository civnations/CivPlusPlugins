package com.devotedmc.ExilePearl.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.event.PearlDecayEvent;

public class WorldBorderListener extends RuleListener {

	public WorldBorderListener(ExilePearlApi pearlApi) {
		super(pearlApi);
	}

	/**
	 * Frees any pearls that are stored beyond world border
	 * 
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPearlDecay(PearlDecayEvent e) {
		boolean autoFree = pearlApi.getPearlConfig().getShouldAutoFreeWorldBorder();
		if (!autoFree) {
			return;
		}
		// Free any pearls outside world border
		Location loc = e.getPearl().getLocation();
		if (!pearlApi.isLocationInsideBorder(loc)) {
			//delayed sync free to avoid ConcurrentModification as this is happening during Pearl iteration
			Bukkit.getScheduler().scheduleSyncDelayedTask(ExilePearlPlugin.getApi(), () -> {
				pearlApi.freePearl(e.getPearl(), PearlFreeReason.OUTSIDE_WORLD_BORDER);
			});
		}
	}
}
