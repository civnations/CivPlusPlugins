package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.auto;

import org.bukkit.Location;

import com.google.gson.JsonObject;

public abstract class JsonableDataObject<D extends JsonableDataObject<D>> extends SerializableDataObject<D> {
	
	public JsonableDataObject(Location location, boolean isNew) {
		super(location, isNew);
	}

	public abstract void concreteSerialize(JsonObject base);

	public String serialize() {
		JsonObject json = new JsonObject();
		concreteSerialize(json);
		return json.toString();
	}

}
