package com.deoxservices.chipsarmorstand.utils;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.*;
import net.minecraft.util.Mth;

public class CodecUtil {
    public static final Codec<Object> OBJECT_CODEC = Codec.PASSTHROUGH.xmap(
        dyn -> fromDynamic(dyn.convert(NbtOps.INSTANCE)), // Dynamic to Object
        obj -> new Dynamic<>(NbtOps.INSTANCE, toTag(obj))  // Object to Dynamic
    );

    private static Object fromDynamic(Dynamic<?> dyn) {
        Tag tag = (Tag) dyn.getValue();
        if (tag == EndTag.INSTANCE) return null;
        return switch (tag.getId()) {
            case Tag.TAG_BYTE -> ((ByteTag) tag).getAsByte() != 0; // Boolean as byte
            case Tag.TAG_INT -> ((IntTag) tag).getAsInt();
            case Tag.TAG_FLOAT -> ((FloatTag) tag).getAsFloat();
            case Tag.TAG_LIST -> {
                ListTag list = (ListTag) tag;
                if (list.getElementType() == Tag.TAG_FLOAT) {
                    float[] array = new float[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        array[i] = list.getFloat(i);
                    }
                    yield array;
                }
                throw new IllegalArgumentException("Unsupported list type: " + list.getElementType());
            }
            case Tag.TAG_INT_ARRAY -> {
                int[] array = ((IntArrayTag) tag).getAsIntArray();
                if (array.length == 4) { // UUID as 4 ints
                    long mostSigBits = ((long) array[0] << 32) | (array[1] & 0xFFFFFFFFL);
                    long leastSigBits = ((long) array[2] << 32) | (array[3] & 0xFFFFFFFFL);
                    yield new UUID(mostSigBits, leastSigBits);
                }
                throw new IllegalArgumentException("Invalid UUID int array length: " + array.length);
            }
            default -> throw new IllegalArgumentException("Unsupported NBT type: " + tag.getId());
        };
    }

    private static Tag toTag(Object obj) {
        if (obj == null) return EndTag.INSTANCE;
        if (obj instanceof Boolean b) return ByteTag.valueOf(b ? (byte) 1 : (byte) 0); // Boolean as byte
        if (obj instanceof Integer i) return IntTag.valueOf(i);
        if (obj instanceof Float f) return FloatTag.valueOf(f);
        if (obj instanceof float[] fa) {
            ListTag list = new ListTag();
            for (float f : fa) {
                list.add(FloatTag.valueOf(Mth.clamp(f, -3.1416f, 3.1416f))); // Clamp angles like vanilla
            }
            return list;
        }
        if (obj instanceof UUID uuid) {
            long mostSigBits = uuid.getMostSignificantBits();
            long leastSigBits = uuid.getLeastSignificantBits();
            int[] array = new int[] {
                (int) (mostSigBits >> 32), (int) mostSigBits,
                (int) (leastSigBits >> 32), (int) leastSigBits
            };
            return new IntArrayTag(array);
        }
        throw new IllegalArgumentException("Unsupported type: " + obj.getClass());
    }
}