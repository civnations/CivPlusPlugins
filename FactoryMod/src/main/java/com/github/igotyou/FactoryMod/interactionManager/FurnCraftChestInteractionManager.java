package com.github.igotyou.FactoryMod.interactionManager;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.utility.FactoryModGUI;

import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class FurnCraftChestInteractionManager implements IInteractionManager {

	private FurnCraftChestFactory fccf;
	private HashMap<Clickable, InputRecipe> recipes = new HashMap<>();
	private DecimalFormat decimalFormatting;

	public FurnCraftChestInteractionManager(FurnCraftChestFactory fccf) {
		this();
		this.fccf = fccf;
	}

	public FurnCraftChestInteractionManager() {
		this.decimalFormatting = new DecimalFormat("#.#####");
	}

	public void setFactory(FurnCraftChestFactory fccf) {
		this.fccf = fccf;
	}

	@Override
	public void redStoneEvent(BlockRedstoneEvent e, Block factoryBlock) {
		int threshold = FactoryMod.getInstance().getManager().getRedstonePowerOn();
		if (!(factoryBlock.getLocation().equals(fccf.getFurnace().getLocation()) && e.getOldCurrent() >= threshold
				&& e.getNewCurrent() < threshold)) {
			return;
		}
		if (FactoryMod.getInstance().getManager().isCitadelEnabled()) {
			if (!MultiBlockStructure.citadelRedstoneChecks(e.getBlock())) {
				return;
			}
		}
		if (fccf.isActive()) {
			fccf.deactivate();
		} else {
			fccf.attemptToActivate(null, false);
		}
	}

	@Override
	public void blockBreak(Player p, Block b) {
		if (p != null && !fccf.getRepairManager().inDisrepair()) {
			p.sendMessage(ChatColor.DARK_RED + "You broke the factory, it is in disrepair now");
		}
		if (fccf.isActive()) {
			fccf.deactivate();
		}
		fccf.getRepairManager().breakIt();
	}

	@Override
	public void leftClick(Player p, Block b, BlockFace bf) {
		if (p.getInventory().getItemInMainHand().getType() != FactoryMod.getInstance().getManager().getFactoryInteractionMaterial()) {
			return;
		}
		if (FactoryMod.getInstance().getManager().isCitadelEnabled()) {
			Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(b);
			if (rein != null) {
				Group g = rein.getGroup();
				if (!NameAPI.getGroupManager().hasAccess(g.getName(), p.getUniqueId(),
						PermissionType.getPermission("USE_FACTORY"))) {
					p.sendMessage(ChatColor.RED + "You dont have permission to interact with this factory");
					return;
				}
			}
		}
		if (b.equals(((FurnCraftChestStructure) fccf.getMultiBlockStructure()).getChest())) { // chest interaction
			if (p.isSneaking()) { // sneaking, so showing detailed recipe stuff
				ClickableInventory ci = new ClickableInventory(54, fccf.getCurrentRecipe().getName());
				int index = 4;
				List<ItemStack> inp = ((InputRecipe) fccf.getCurrentRecipe())
						.getInputRepresentation(fccf.getInventory(), fccf);
				if (inp.size() > 18) {
					inp = new ItemMap(inp).getLoredItemCountRepresentation();
				}
				for (ItemStack is : inp) {
					Clickable c = new Clickable(is) {
						@Override
						public void clicked(Player arg0) {
							// nothing, just supposed to look nice
						}
					};
					ci.setSlot(c, index);
					// weird math to fill up the gui nicely
					if ((index % 9) == 4) {
						index++;
						continue;
					}
					if ((index % 9) > 4) {
						index -= (((index % 9) - 4) * 2);
					} else {
						if ((index % 9) == 0) {
							index += 13;
						} else {
							index += (((4 - (index % 9)) * 2) + 1);
						}
					}

				}
				index = 49;
				List<ItemStack> outp = ((InputRecipe) fccf.getCurrentRecipe())
						.getOutputRepresentation(fccf.getInventory(), fccf);
				if (outp.size() > 18) {
					outp = new ItemMap(outp).getLoredItemCountRepresentation();
				}
				for (ItemStack is : outp) {
					Clickable c = new Clickable(is) {
						@Override
						public void clicked(Player arg0) {
							// nothing, just supposed to look nice
						}
					};
					ci.setSlot(c, index);
					if ((index % 9) == 4) {
						index++;
						continue;
					}
					if ((index % 9) > 4) {
						index -= (((index % 9) - 4) * 2);
					} else {
						if ((index % 9) == 0) {
							index -= 13;
						} else {
							index += (((4 - (index % 9)) * 2) + 1);
						}
					}

				}
				ci.showInventory(p);

			} else { // not sneaking, so just a short sumup
				p.sendMessage(
						ChatColor.GOLD + fccf.getName() + " currently turned " + (fccf.isActive() ? "on" : "off"));
				if (fccf.isActive()) {
					p.sendMessage(ChatColor.GOLD
							+ String.valueOf((fccf.getCurrentRecipe().getProductionTime() - fccf.getRunningTime()) / 20)
							+ " seconds remaining until current run is complete");
				}
				p.sendMessage(ChatColor.GOLD + "Currently selected recipe: " + fccf.getCurrentRecipe().getName());
				p.sendMessage(ChatColor.GOLD + "Currently at " + fccf.getRepairManager().getHealth() + " health");
				if (fccf.getRepairManager().inDisrepair()) {
					PercentageHealthRepairManager rm = ((PercentageHealthRepairManager) fccf.getRepairManager());
					long leftTime = rm.getGracePeriod() - (System.currentTimeMillis() - rm.getBreakTime());
					long months = leftTime / (60L * 60L * 24L * 30L * 1000L);
					long days = (leftTime - (months * 60L * 60L * 24L * 30L * 1000L)) / (60L * 60L * 24L * 1000L);
					long hours = (leftTime - (months * 60L * 60L * 24L * 30L * 1000L)
							- (days * 60L * 60L * 24L * 1000L)) / (60L * 60L * 1000L);
					String time = (months != 0 ? months + " months, " : "") + (days != 0 ? days + " days, " : "")
							+ (hours != 0 ? hours + " hours" : "");
					if (time.equals("")) {
						time = " less than an hour";
					}
					p.sendMessage(ChatColor.GOLD + "It will break permanently in " + time);
				}
			}

			return;
		}
		if (b.equals(((FurnCraftChestStructure) fccf.getMultiBlockStructure()).getCraftingTable())) { // crafting table
																										// interaction
			int rows = ((fccf.getRecipes().size() + 2) / 9) + 1;
			if (fccf.getRecipes().size() > 52 || rows > 6) {
				p.sendMessage(ChatColor.RED
						+ "This factory has more than 52 recipes and the GUI for it can't be opened. Either complain to "
						+ "your admin to have them put less recipes in this factory or complain to /u/maxopoly to add "
						+ "scrollviews to this");
				return;
			}
			ClickableInventory ci = new ClickableInventory(rows * 9, "Select a recipe");
			for (IRecipe rec : fccf.getRecipes()) {
				InputRecipe recipe = (InputRecipe) (rec);
				ItemStack recStack = recipe.getRecipeRepresentation();
				int runcount = fccf.getRunCount(recipe);
				ItemAPI.addLore(recStack, "",ChatColor.AQUA + "Ran " + String.valueOf(runcount) + " times");
				if (rec == fccf.getCurrentRecipe()) {
					ItemAPI.addLore(recStack, ChatColor.GREEN + "Currently selected");
					ItemAPI.addGlow(recStack);
				}
				if (recipe instanceof ProductionRecipe) {
					ProductionRecipe prod = (ProductionRecipe) recipe;
					if (prod.getModifier() != null) {
						ItemAPI.addLore(recStack, ChatColor.BOLD + "   " + ChatColor.GOLD
								+ fccf.getRecipeLevel(recipe) + " ★");
						ItemAPI.addLore(recStack, ChatColor.GREEN + "Current output multiplier: " + decimalFormatting
								.format(prod.getModifier().getFactor(fccf.getRecipeLevel(recipe), runcount)));
					}
				}
				Clickable c = new Clickable(recStack) {

					@Override
					public void clicked(Player p) {
						if (fccf.isActive()) {
							p.sendMessage(ChatColor.RED + "You can't switch recipes while the factory is running");
						} else {
							fccf.setRecipe(recipes.get(this));
							p.sendMessage(ChatColor.GREEN + "Switched recipe to " + recipes.get(this).getName());
						}

					}
				};
				recipes.put(c, recipe);
				ci.addSlot(c);
			}
			ItemStack autoSelectStack = new ItemStack(Material.REDSTONE_BLOCK);
			ItemAPI.setDisplayName(autoSelectStack, "Toggle auto select");
			ItemAPI.addLore(autoSelectStack,
					ChatColor.GOLD + "Auto select will make the factory automatically select any "
							+ "recipe it can run whenever you activate it.",
					ChatColor.AQUA + "Click to turn it " + (fccf.isAutoSelect() ? "off" : "on"));
			Clickable autoClick = new Clickable(autoSelectStack) {

				@Override
				public void clicked(Player arg0) {
					arg0.sendMessage(ChatColor.GREEN + "Turned auto select " + (fccf.isAutoSelect() ? "off" : "on")
							+ " for " + fccf.getName());
					fccf.setAutoSelect(!fccf.isAutoSelect());
				}
			};
			ci.setSlot(autoClick, (rows * 9) - 2);
			ItemStack menuStack = new ItemStack(Material.PAINTING);
			ItemAPI.setDisplayName(menuStack, "Open menu");
			ItemAPI.addLore(menuStack, ChatColor.LIGHT_PURPLE + "Click to open a detailed menu");
			Clickable menuC = new Clickable(menuStack) {
				@Override
				public void clicked(Player p) {
					FactoryModGUI gui = new FactoryModGUI(p);
					gui.showForFactory((FurnCraftChestEgg)FactoryMod.getInstance().getManager().getEgg(fccf.getName()));
				}
			};
			ci.setSlot(menuC, (rows * 9) - 1);

			ci.showInventory(p);
			return;
		}
		if (b.equals(fccf.getFurnace())) { // furnace interaction
			if (fccf.isActive()) {
				fccf.deactivate();
				p.sendMessage(ChatColor.RED + "Deactivated " + fccf.getName());
			} else {
				fccf.attemptToActivate(p, false);
			}
		}
	}

	@Override
	public void rightClick(Player p, Block b, BlockFace bf) {
		// Nothing to do here, every block already has a right click
		// functionality
	}

}
