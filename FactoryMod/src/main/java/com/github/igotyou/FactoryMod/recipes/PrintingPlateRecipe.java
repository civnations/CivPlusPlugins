/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagManager;

public class PrintingPlateRecipe extends PrintingPressRecipe {
	public static final String itemName = "Printing Plate";

	private ItemMap output;

	public ItemMap getOutput() {
		return this.output;
	}

	public PrintingPlateRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap output) {
		super(identifier, name, productionTime, input);
		this.output = output;
	}	

	@Override
	public boolean enoughMaterialAvailable(Inventory i) {
		return this.input.isContainedIn(i) && getBook(i) != null;
	}

	@Override
	public boolean applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);

		ItemStack book = getBook(i);
		BookMeta bookMeta = (BookMeta)book.getItemMeta();
		if (!bookMeta.hasGeneration()){
			bookMeta.setGeneration(Generation.TATTERED);
		}
		String serialNumber = UUID.randomUUID().toString();

		ItemMap toRemove = input.clone();
		ItemMap toAdd = output.clone();

		if (toRemove.isContainedIn(i) && toRemove.removeSafelyFrom(i)) {
			for(ItemStack is: toAdd.getItemStackRepresentation()) {
				is = addTags(i, serialNumber, is);

				ItemAPI.setDisplayName(is, itemName);
				ItemAPI.setLore(is,
						serialNumber,
						ChatColor.WHITE + bookMeta.getTitle(),
						ChatColor.GRAY + "by " + bookMeta.getAuthor(),
						ChatColor.GRAY + getGenerationName(bookMeta.getGeneration())
						);
				is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
				is.getItemMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
				i.addItem(is);
			}
		}

		logAfterRecipeRun(i, fccf);
		return true;
	}

	private ItemStack addTags(Inventory i, String serialNumber, ItemStack is) {
		ItemStack book = getBook(i);
		TagManager bookTag = new TagManager(book);
		TagManager isTag = new TagManager(is);

		isTag.setString("SN", serialNumber);
		isTag.setCompound("Book", bookTag);

		return isTag.enrichWithNBT(is);
	}

	private static String getGenerationName(Generation gen) {
		switch(gen) {
		case ORIGINAL: return "Original";
		case COPY_OF_ORIGINAL: return "Copy of Original";
		case COPY_OF_COPY: return "Copy of Copy";
		case TATTERED: return "Tattered";
		default: return "";
		}
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<>();

		if (i == null) {
			result.add(new ItemStack(Material.WRITTEN_BOOK, 1));
			result.addAll(this.input.getItemStackRepresentation());
			return result;
		}

		result = createLoredStacksForInfo(i);

		ItemStack book = getBook(i);

		if(book != null) {
			result.add(book.clone());
		}

		return result;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> stacks = new ArrayList<>();
		stacks.add(getPrintingPlateRepresentation(this.output, itemName));
		stacks.add(new ItemStack(Material.WRITTEN_BOOK));

		if (i == null) {
			return stacks;
		}

		int possibleRuns = this.input.getMultiplesContainedIn(i);

		for (ItemStack is : stacks) {
			ItemAPI.addLore(is, ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
		}

		return stacks;
	}
	
	@Override
	public Material getRecipeRepresentationMaterial() {
		return getPrintingPlateRepresentation(this.output, getName()).getType();
	}

	private ItemStack getBook(Inventory i) {
		for (ItemStack is : i.getContents()) {
			if (is != null && is.getType() == Material.WRITTEN_BOOK) {
				return is;
			}
		}

		return null;
	}

	@Override
	public String getTypeIdentifier() {
		return "PRINTINGPLATE";
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("Something");
	}
}
