/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Container class for UV face information, only used in deserialization at startup
 */
public record UVFaces(
        @Nullable FaceUV north,
        @Nullable FaceUV south,
        @Nullable FaceUV east,
        @Nullable FaceUV west,
        @Nullable FaceUV up,
        @Nullable FaceUV down
) {

    public static JsonDeserializer<UVFaces> deserializer() {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            FaceUV north = GsonHelper.getAsObject(obj, "north", null, context, FaceUV.class);
            FaceUV south = GsonHelper.getAsObject(obj, "south", null, context, FaceUV.class);
            FaceUV east = GsonHelper.getAsObject(obj, "east", null, context, FaceUV.class);
            FaceUV west = GsonHelper.getAsObject(obj, "west", null, context, FaceUV.class);
            FaceUV up = GsonHelper.getAsObject(obj, "up", null, context, FaceUV.class);
            FaceUV down = GsonHelper.getAsObject(obj, "down", null, context, FaceUV.class);

            return new UVFaces(north, south, east, west, up, down);
        };
    }

    public FaceUV fromDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> north;
            case SOUTH -> south;
            case EAST -> east;
            case WEST -> west;
            case UP -> up;
            case DOWN -> down;
        };
    }
}
