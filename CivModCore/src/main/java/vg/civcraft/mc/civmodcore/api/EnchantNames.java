package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.enchantments.Enchantment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * Class that loads and store enchantments names.
 */
public final class EnchantNames {

	private static final Logger LOGGER = LoggerFactory.getLogger(EnchantNames.class.getSimpleName());

	private static final Set<SearchResult> enchantmentDetails = new HashSet<>();

	/**
	 * Resets all enchantments names and initials.
	 */
	public static void resetEnchantmentNames() {
		enchantmentDetails.clear();
	}

	/**
	 * Loads enchantment names and initials from the config.
	 */
	@SuppressWarnings("deprecation")
	public static void loadEnchantmentNames() {
		resetEnchantmentNames();
		// Load enchantment names from enchantments.csv
		InputStream enchantmentsCSV = CivModCorePlugin.class.getResourceAsStream("/enchantments.csv");
		if (enchantmentsCSV != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(enchantmentsCSV));
				String line = reader.readLine();
				lineReader:
				while (line != null) {
					String[] values = line.split(",");
					// If there's not at least three values (slug, abbreviation, display name) then skip
					if (values.length != 3) {
						LOGGER.warn("[Config] This enchantment row is corrupted: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If the Enchantment cannot be found by the slug given, then skip
					Enchantment enchantment = Enchantment.getByName(values[0].toUpperCase());
					if (enchantment == null) {
						LOGGER.warn("[Config] Could not find an enchantment on this line: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If the abbreviation or display name doesn't exist, then skip
					String abbreviation = values[1];
					String displayName = values[2];
					if (abbreviation.isEmpty() || displayName.isEmpty()) {
						LOGGER.warn("[Config] Could not find an abbreviation on this line: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// Check if any of these details have already been registered
					for (SearchResult details : enchantmentDetails) {
						if (details.enchantment == enchantment) {
							LOGGER.warn("[Config] This enchantment already has a name registered: " + line);
							// Go to the next line
							line = reader.readLine();
							continue lineReader;
						}
						if (TextUtil.stringEqualsIgnoreCase(details.abbreviation, abbreviation)) {
							LOGGER.warn("[Config] This abbreviation already has a name registered: " + line);
							// Go to the next line
							line = reader.readLine();
							continue lineReader;
						}
						if (TextUtil.stringEqualsIgnoreCase(details.displayName, displayName)) {
							LOGGER.warn("[Config] This display name already has a name registered: " + line);
							// Go to the next line
							line = reader.readLine();
							continue lineReader;
						}
					}
					// Put the enchantment and name into the system
					enchantmentDetails.add(new SearchResult(enchantment, abbreviation, displayName));
					LOGGER.info(String.format("[Config] Enchantment parsed: %s = %s = %s", enchantment.getName(),
							abbreviation,
							displayName));
					line = reader.readLine();
				}
				reader.close();
			}
			catch (IOException error) {
				LOGGER.warn("[Config] Could not load enchantments from enchantments.csv");
				error.printStackTrace();
			}
		}
		else {
			LOGGER.warn("[Config] Could not load enchantments from enchantments.csv as the file does not exist.");
		}
	}

	/**
	 * <p>Attempts to match an enchantment with a set of details.</p>
	 *
	 * <p>Note: If a valid enchantment was entered but null was returned, the server needs to update its enchantment
	 * list, which is located in [server]/plugins/CivModCore/enchantments.csv</p>
	 *
	 * @param enchantment The enchantment to search with.
	 * @return The enchantment details, or null.
	 */
	public static SearchResult findByEnchantment(Enchantment enchantment) {
		if (enchantment == null) {
			return null;
		}
		for (SearchResult details : enchantmentDetails) {
			if (details.enchantment == enchantment) {
				return details;
			}
		}
		return null;
	}

	/**
	 * <p>Attempts to match an abbreviation with a set of enchantment details, for example by default "E" will match
	 * with {@link Enchantment#DIG_SPEED} because "Efficiency"</p>
	 *
	 * <p>Note: The abbreviation search is not case sensitive.</p>
	 *
	 * @param abbreviation The abbreviation to search with.
	 * @return The enchantment details, or null.
	 */
	public static SearchResult findByAbbreviation(String abbreviation) {
		if (Strings.isNullOrEmpty(abbreviation)) {
			return null;
		}
		for (SearchResult details : enchantmentDetails) {
			if (TextUtil.stringEqualsIgnoreCase(details.abbreviation, abbreviation)) {
				return details;
			}
		}
		return null;
	}

	/**
	 * <p>Attempts to match a display name with a set of enchantment details, for example by default "Efficiency" will
	 * match with {@link Enchantment#DIG_SPEED}</p>
	 *
	 * <p>Note: The display name search is not case sensitive.</p>
	 *
	 * @param displayName The display name to search with.
	 * @return The enchantment details, or null.
	 */
	public static SearchResult findByDisplayName(String displayName) {
		if (Strings.isNullOrEmpty(displayName)) {
			return null;
		}
		for (SearchResult details : enchantmentDetails) {
			if (TextUtil.stringEqualsIgnoreCase(details.displayName, displayName)) {
				return details;
			}
		}
		return null;
	}

	/**
	 * This class represents a data set for a particular enchantment.
	 */
	public static final class SearchResult {

		private final Enchantment enchantment;

		private final String abbreviation;

		private final String displayName;

		private SearchResult(Enchantment enchantment, String abbreviation, String displayName) {
			this.enchantment = enchantment;
			this.abbreviation = abbreviation;
			this.displayName = displayName;
		}

		/**
		 * @return Returns the enchantment itself.
		 */
		public Enchantment getEnchantment() {
			return this.enchantment;
		}

		/**
		 * @return Returns the enchantment's official abbreviation.
		 */
		public String getAbbreviation() {
			return this.abbreviation;
		}

		/**
		 * @return Returns the enchantment's official display name.
		 */
		public String getDisplayName() {
			return this.displayName;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.enchantment, this.displayName, this.abbreviation);
		}

	}

}
