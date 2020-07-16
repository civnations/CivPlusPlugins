package com.github.igotyou.FactoryMod.recipes;

import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

/**
 * Encapsulates a specific functionality for a FurnCraftChest factory. Each
 * factory of this type can have of many different recipes and what the recipe
 * actually does is completly kept inside the recipe's class
 *
 */
public interface IRecipe {
	/**
	 * @return The identifier for this recipe, which is used both internally and
	 *         to display the recipe to a player
	 */
	public String getName();

	/**
	 * @return A unique identifier for this recipe
	 */
	public String getIdentifier();

	/**
	 * @return How long this recipe takes for one run in ticks
	 */
	public int getProductionTime();

	/**
	 * Checks whether enough material is available in the given inventory to run
	 * this recipe at least once
	 * 
	 * @param i
	 *            Inventory to check
	 * @return true if the recipe could be run at least once, false if not
	 */
	public boolean enoughMaterialAvailable(Inventory i);

	/**
	 * Applies whatever the recipe actually does, it's main functionality
	 * 
	 * @param i
	 *            Inventory which contains the materials to work with
	 * @param f
	 *            Factory which is run
	 * @return true if the recipe could be run; false otherwise (e.g: not enough storage space)
	 */
	public boolean applyEffect(Inventory i, FurnCraftChestFactory f);

	/**
	 * Each implementation of this class has to specify a unique identifier,
	 * which is used to identify instances of this recipe in the config
	 * 
	 * @return Unique identifier for the implementation
	 */
	public String getTypeIdentifier();
}
