package mod.azure.azurelib.loading.json.raw;

import net.minecraft.util.JSONUtils;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for texture mesh information, only used in deserialization at startup
 */
public class TextureMesh {
	private final double[] localPivot;
	private final double[] position;
	private final double[] rotation;
	private final double[] scale;
	private final String texture;

	public TextureMesh(double[] localPivot, double[] position, double[] rotation, double[] scale, String texture) {
		this.localPivot = localPivot;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.texture = texture;
	}

	public double[] localPivot() {
		return localPivot;
	}

	public double[] position() {
		return position;
	}

	public double[] rotation() {
		return rotation;
	}

	public double[] scale() {
		return scale;
	}

	public String texture() {
		return texture;
	}

	public static JsonDeserializer<TextureMesh> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			double[] pivot = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "local_pivot", null));
			double[] position = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "position", null));
			double[] rotation = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "rotation", null));
			double[] scale = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "scale", null));
			String texture = JSONUtils.getAsString(obj, "texture", null);
			return new TextureMesh(pivot, position, rotation, scale, texture);
		};
	}
}
