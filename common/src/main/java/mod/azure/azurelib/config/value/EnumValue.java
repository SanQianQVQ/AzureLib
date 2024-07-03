/**
 * This class is a fork of the matching class found in the Configuration repository.
 * Original source: https://github.com/Toma1O6/Configuration
 * Copyright © 2024 Toma1O6.
 * Licensed under the MIT License.
 */
package mod.azure.azurelib.config.value;

import mod.azure.azurelib.config.adapter.TypeAdapter;
import mod.azure.azurelib.config.exception.ConfigValueMissingException;
import mod.azure.azurelib.config.format.IConfigFormat;
import net.minecraft.network.FriendlyByteBuf;

public class EnumValue<E extends Enum<E>> extends ConfigValue<E> {

    public EnumValue(ValueData<E> valueData) {
        super(valueData);
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeEnum(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readEnum(this.getId(), getValueType()));
    }

    public static final class Adapter<E extends Enum<E>> extends TypeAdapter {

        @SuppressWarnings("unchecked")
        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new EnumValue<>(ValueData.of(name, (E) value, context, comments));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            buffer.writeEnum((E) value.get());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            Class<E> type = (Class<E>) value.getValueType();
            return buffer.readEnum(type);
        }
    }
}
