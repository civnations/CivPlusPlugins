package vg.civcraft.mc.civmodcore.util;

import com.google.common.base.Strings;
import org.apache.commons.lang.reflect.MethodUtils;

/**
 * Class of enum utilities.
 */
public class EnumUtils {

	/**
	 * Retrieves a enum element from an enum class if the given slug matches.
	 *
	 * @param <T> The enum type.
	 * @param clazz The enum class.
	 * @param slug The slug of the intended enum element.
	 * @param caseInsensitive Set to true if you want to check to not care about case sensitivity.
	 * @return Returns the matched enum element, or null.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T fromSlug(Class<T> clazz, String slug, boolean caseInsensitive) {
		if (clazz == null || Strings.isNullOrEmpty(slug)) {
			return null;
		}
		T[] values = null;
		try {
			values = (T[]) MethodUtils.invokeExactStaticMethod(clazz, "values", null);
		}
		catch (Exception ignored) { }
		if (Iteration.isNullOrEmpty(values)) {
			return null;
		}
		for (T value : values) {
			if (caseInsensitive) {
				if (TextUtil.stringEqualsIgnoreCase(value.name(), slug)) {
					return value;
				}
			}
			else {
				if (TextUtil.stringEquals(value.name(), slug)) {
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * Null safe way of getting an enum elements' name.
	 *
	 * @param element The enum element to get the name of.
	 * @return Return an element's name or null.
	 */
	public static String getSlug(Enum<?> element) {
		if (element == null) {
			return null;
		}
		return element.name();
	}

}
