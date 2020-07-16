package com.github.igotyou.FactoryMod.recipes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

/**
 * Used to decompact itemstacks, which means a single item with compacted lore
 * is turned into a whole stack without lore. This reverses the functionality of
 * the CompactingRecipe
 *
 */
public class DecompactingRecipe extends InputRecipe {
	private String compactedLore;

	public DecompactingRecipe(String identifier, ItemMap input, String name, int productionTime,
			String compactedLore) {
		super(identifier, name, productionTime, input);
		this.compactedLore = compactedLore;
	}

	@Override
	public boolean enoughMaterialAvailable(Inventory i) {
		if (!input.isContainedIn(i)) {
			return false;
		}
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				if (isDecompactable(is)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		if (input.isContainedIn(i)) {
			for (ItemStack is : i.getContents()) {
				if (is != null) {
					if (isDecompactable(is)) {
						ItemStack removeClone = is.clone();
						removeClone.setAmount(1);
						ItemMap toRemove = new ItemMap(removeClone);
						ItemMap toAdd = new ItemMap();
						removeCompactLore(removeClone);
						toAdd.addItemAmount(removeClone, CompactingRecipe.getCompactStackSize(removeClone.getType()));
						if (toAdd.fitsIn(i)) { //fits in chest
							if (input.removeSafelyFrom(i)) { //remove extra input
								if (toRemove.removeSafelyFrom(i)) { //remove one compacted item
									for(ItemStack add : toAdd.getItemStackRepresentation()) {
										i.addItem(add);
									}
								}
							}
						} else { // does not fit in chest
							return false;
						}
						break;
					}
				}
			}
		}
		logAfterRecipeRun(i, fccf);
		return true;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<>();
		if (i == null) {
			ItemStack is = new ItemStack(Material.STONE, 64);
			ItemAPI.addLore(is, compactedLore);
			is.setAmount(1);
			result.add(is);
			return result;
		}
		result = createLoredStacksForInfo(i);
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				if (isDecompactable(is)) {
					ItemStack compactedStack = is.clone();
					result.add(compactedStack);
					break;
				}
			}
		}
		return result;
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.TRAPPED_CHEST;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<>();
		if (i == null) {
			result.add(new ItemStack(Material.STONE, 64));
			return result;
		}
		for (ItemStack is : i.getContents()) {
			if (is != null) {
				if (isDecompactable(is)) {
					ItemStack copy = is.clone();
					removeCompactLore(copy);
					ItemMap output = new ItemMap();
					output.addItemAmount(copy, CompactingRecipe.getCompactStackSize(copy.getType()));
					result.addAll(output.getItemStackRepresentation());
				}
			}
		}
		return result;
	}

	private boolean isDecompactable(ItemStack is) {
		//dont allow decompation if the item is enchanted or has additional lore, as the enchant/additional lore could have been applied to the compacted item
		//and decompacting it would produce many items, which all have that enchant/lore
		if (((is.getItemMeta().hasEnchants() || (is.getItemMeta().hasLore() && is.getItemMeta().getLore().size() >= 2)) && is.getType().getMaxStackSize() == 1)) {
			return false;
		}
		List <String> lore = is.getItemMeta().getLore();
		if (lore != null) {
			for(String content : lore) {
				if (content.equals(compactedLore)) {
					return true;
				}
			}
		}
		return false;
	}

	private void removeCompactLore(ItemStack is) {
		List <String> lore = is.getItemMeta().getLore();
		if (lore != null) {
			lore.remove(compactedLore);
		}
		ItemMeta im = is.getItemMeta();
		im.setLore(lore);
		is.setItemMeta(im);
	}

	@Override
	public String getTypeIdentifier() {
		return "DECOMPACT";
	}

	public String getCompactedLore() {
		return compactedLore;
	}
	
	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("An entire stack of the decompacted item if it's stackable", "---OR---", "Eight of the decompacted item if it's not stackable");
	}

	@Override
	public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("A single compacted item");
	}
}
