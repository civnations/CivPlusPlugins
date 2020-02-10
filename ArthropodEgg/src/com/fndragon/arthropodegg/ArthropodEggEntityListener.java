package com.fndragon.arthropodegg;

import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;

/**
 * @author Randy
 * @since 1.0
 * 
 * All the event listeners related to ArthropodEgg plugin. Handles
 * each case that is currently configured for handling.
 */
public class ArthropodEggEntityListener implements Listener {

	private ArthropodEgg plugin;
	
	public ArthropodEggEntityListener( ArthropodEgg instance ) {
		plugin = instance;
	}
		
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDeath( EntityDeathEvent event ) {
		Player targetPlayer = event.getEntity().getKiller();
		if( null == targetPlayer ) {
			return;
		}
		
		Short currentEntityID = event.getEntity().getType().getTypeId();
		if( false == plugin.getConfig().getShortList("eggEntityIDList").contains( currentEntityID ) ) {
			return;
		}

		// Check for a baby animal
		if( event.getEntity() instanceof Ageable )
		{
			Ageable ageableEntity = (Ageable) event.getEntity();
			if( ageableEntity.isAdult() == false ) {
				return;  // NOPE.
			}
		}
		
		// Check the player's currently equipped weapon
		ItemStack handstack = targetPlayer.getItemInHand();
		// Get the map of enchantments on that item
		Map<Enchantment,Integer> itemEnchants = handstack.getEnchantments();
		if(itemEnchants.isEmpty()) {
			return;
		}
		
		// Check if one enchantment is BaneOfArthropods
		if( null == itemEnchants.get( org.bukkit.enchantments.Enchantment.DAMAGE_ARTHROPODS ) )
		{
			return;
		}
		
		double randomNum = Math.random();
		double eggArthropodPercentage = plugin.getConfig().getDouble( "eggArthropodPercentage" );
		double eggLootingPercentage = plugin.getConfig().getDouble( "eggLootingPercentage" );
		double levelOfArthropod = handstack.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DAMAGE_ARTHROPODS);
		double levelOfLooting = handstack.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS);
		
		double targetPercentage = (eggArthropodPercentage * levelOfArthropod) + (eggLootingPercentage * levelOfLooting);
		if( plugin.getConfig().getBoolean("eggDebug")) {
			targetPlayer.sendMessage( "Arth[" + levelOfArthropod + "], Loot[" + levelOfLooting + "]");
			targetPlayer.sendMessage( "Total =" + targetPercentage * 100 + "%, random% is " + randomNum * 100 );
		}
		
		// Check if egg should be spawned
		if( randomNum < targetPercentage )
		{
			ItemStack item = new ItemStack(Material.MONSTER_EGG, 1);
			SpawnEggMeta spawnMeta = (SpawnEggMeta) item.getItemMeta();
			spawnMeta.setSpawnedType(event.getEntityType());
			item.setItemMeta(spawnMeta);

			if( plugin.getConfig().getBoolean("eggRemoveDrops")) {
				event.getDrops().clear();
				event.setDroppedExp(0);
			}
			event.getDrops().add( item );
			if( plugin.getConfig().getBoolean("eggDebug")) {
				targetPlayer.sendMessage( "Egg generated." );
			}
		}		
	}
}
