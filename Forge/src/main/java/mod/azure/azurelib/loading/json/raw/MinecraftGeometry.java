package mod.azure.azurelib.loading.json.raw;

import net.minecraft.util.JSONUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for generic geometry information, only used in deserialization at startup
 */
public class MinecraftGeometry {
	private final Bone[] bones;
	private final String cape;
	private final ModelProperties modelProperties;

	public MinecraftGeometry(Bone[] bones, String cape, ModelProperties modelProperties) {
		this.bones = bones;
		this.cape = cape;
		this.modelProperties = modelProperties;
	}

	public Bone[] bones() {
		return bones;
	}

	public String cape() {
		return cape;
	}

	public ModelProperties modelProperties() {
		return modelProperties;
	}

	public static JsonDeserializer<MinecraftGeometry> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			Bone[] bones = JsonUtil.jsonArrayToObjectArray(JSONUtils.getAsJsonArray(obj, "bones", new JsonArray()), context, Bone.class);
			String cape = JSONUtils.getAsString(obj, "cape", null);
			ModelProperties modelProperties = JSONUtils.getAsObject(obj, "description", null, context, ModelProperties.class);

			return new MinecraftGeometry(bones, cape, modelProperties);
		};
	}
}
