package mod.azure.azurelib.loading.json.raw;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.GsonHelper;

/**
 * Container class for generic geometry information, only used in deserialization at startup
 */
public class MinecraftGeometry {
	public Bone[] bones;
	@Nullable
	public String cape;
	@Nullable
	public ModelProperties modelProperties;

	public MinecraftGeometry(Bone[] bones, @Nullable String cape, @Nullable ModelProperties modelProperties) {
		this.bones = bones;
		this.cape = cape;
		this.modelProperties = modelProperties;
	}

	public Bone[] bones() {
		return bones;
	}

	@Nullable
	public String cape() {
		return cape;
	}

	@Nullable
	public ModelProperties modelProperties() {
		return modelProperties;
	}

	public static JsonDeserializer<MinecraftGeometry> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			Bone[] bones = JsonUtil.jsonArrayToObjectArray(GsonHelper.getAsJsonArray(obj, "bones", new JsonArray()), context, Bone.class);
			String cape = GsonHelper.getAsString(obj, "cape", null);
			ModelProperties modelProperties = GsonHelper.getAsObject(obj, "description", null, context, ModelProperties.class);

			return new MinecraftGeometry(bones, cape, modelProperties);
		};
	}
}
