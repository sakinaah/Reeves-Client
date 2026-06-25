package com.reevesclient.core.module.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * A single configurable setting attached to a module.
 * Subclass for each primitive type: BooleanSetting, IntSetting, etc.
 */
public abstract class ModuleSetting<T> {

    protected final String key;
    protected final String displayName;
    protected final String description;
    protected T value;
    protected final T defaultValue;

    protected ModuleSetting(String key, String displayName, String description, T defaultValue) {
        this.key          = key;
        this.displayName  = displayName;
        this.description  = description;
        this.defaultValue = defaultValue;
        this.value        = defaultValue;
    }

    public String getKey()         { return key; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public T      getValue()       { return value; }
    public T      getDefault()     { return defaultValue; }

    public void setValue(T value)  { this.value = value; }
    public void reset()            { this.value = defaultValue; }

    public abstract JsonElement serialize();
    public abstract void deserialize(JsonElement element);

    // ── Concrete types ──────────────────────────────────────────────────────

    public static class BooleanSetting extends ModuleSetting<Boolean> {
        public BooleanSetting(String key, String displayName, String description, boolean defaultValue) {
            super(key, displayName, description, defaultValue);
        }

        @Override public JsonElement serialize()                  { return new JsonPrimitive(value); }
        @Override public void deserialize(JsonElement element)    { value = element.getAsBoolean(); }

        public void toggle() { value = !value; }
    }

    public static class IntSetting extends ModuleSetting<Integer> {
        private final int min, max;

        public IntSetting(String key, String displayName, String description,
                          int defaultValue, int min, int max) {
            super(key, displayName, description, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override public JsonElement serialize()                  { return new JsonPrimitive(value); }
        @Override public void deserialize(JsonElement element)    { setValue(element.getAsInt()); }

        @Override
        public void setValue(Integer value) {
            this.value = Math.max(min, Math.min(max, value));
        }

        public int getMin() { return min; }
        public int getMax() { return max; }
    }

    public static class FloatSetting extends ModuleSetting<Float> {
        private final float min, max;

        public FloatSetting(String key, String displayName, String description,
                            float defaultValue, float min, float max) {
            super(key, displayName, description, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override public JsonElement serialize()                  { return new JsonPrimitive(value); }
        @Override public void deserialize(JsonElement element)    { setValue(element.getAsFloat()); }

        @Override
        public void setValue(Float value) {
            this.value = Math.max(min, Math.min(max, value));
        }

        public float getMin() { return min; }
        public float getMax() { return max; }
    }

    public static class StringSetting extends ModuleSetting<String> {
        public StringSetting(String key, String displayName, String description, String defaultValue) {
            super(key, displayName, description, defaultValue);
        }

        @Override public JsonElement serialize()                  { return new JsonPrimitive(value); }
        @Override public void deserialize(JsonElement element)    { value = element.getAsString(); }
    }

    public static class ColorSetting extends ModuleSetting<Integer> {
        public ColorSetting(String key, String displayName, String description, int defaultArgb) {
            super(key, displayName, description, defaultArgb);
        }

        @Override public JsonElement serialize()                  { return new JsonPrimitive(value); }
        @Override public void deserialize(JsonElement element)    { value = element.getAsInt(); }

        public int getRed()   { return (value >> 16) & 0xFF; }
        public int getGreen() { return (value >> 8)  & 0xFF; }
        public int getBlue()  { return  value        & 0xFF; }
        public int getAlpha() { return (value >> 24) & 0xFF; }
    }

    public static class EnumSetting<E extends Enum<E>> extends ModuleSetting<E> {
        private final Class<E> enumClass;

        public EnumSetting(String key, String displayName, String description,
                           E defaultValue, Class<E> enumClass) {
            super(key, displayName, description, defaultValue);
            this.enumClass = enumClass;
        }

        @Override public JsonElement serialize()                  { return new JsonPrimitive(value.name()); }
        @Override public void deserialize(JsonElement element)    {
            try { value = Enum.valueOf(enumClass, element.getAsString()); }
            catch (IllegalArgumentException ignored) { value = defaultValue; }
        }

        public void cycle() {
            E[] constants = enumClass.getEnumConstants();
            value = constants[(value.ordinal() + 1) % constants.length];
        }
    }
}
