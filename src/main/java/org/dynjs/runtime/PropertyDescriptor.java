package org.dynjs.runtime;

import java.util.HashMap;
import java.util.Map;

import org.dynjs.exception.ThrowException;

public class PropertyDescriptor {

    private static final Map<String, Object> DEFAULTS = new HashMap<String, Object>() {
        {
            put("Value", Types.UNDEFINED);
            put("Set", Types.UNDEFINED);
            put("Get", Types.UNDEFINED);
            put("Writable", false);
            put("Configurable", false);
            put("Enumerable", false);
        }
    };

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public static PropertyDescriptor newAccessorPropertyDescriptor(final boolean withDefaults) {
        return new PropertyDescriptor() {
            {
                if (withDefaults) {
                    set("Value", DEFAULTS.get("Value"));
                    set("Writable", DEFAULTS.get("Writable"));
                    set("Configurable", DEFAULTS.get("Configurable"));
                    set("Enumerable", DEFAULTS.get("Enumerable"));
                }
            }
        };
    }

    public static PropertyDescriptor newDataPropertyDescriptor(final boolean withDefaults) {
        return new PropertyDescriptor() {
            {
                if (withDefaults) {
                    set("Set", DEFAULTS.get("Set"));
                    set("Get", DEFAULTS.get("Get"));
                    set("Configurable", DEFAULTS.get("Configurable"));
                    set("Enumerable", DEFAULTS.get("Enumerable"));
                }
            }
        };
    }

    public static PropertyDescriptor newPropertyDescriptorForObjectInitializer(Object value) {
        return newPropertyDescriptorForObjectInitializer(null, value);
    }

    public static PropertyDescriptor newPropertyDescriptorForObjectInitializer(String name, Object value) {
        if (name != null && (value instanceof JSFunction)) {
            ((JSFunction) value).setDebugContext("Object." + name);
        }
        PropertyDescriptor d = new PropertyDescriptor();
        d.set("Value", value);
        d.set("Writable", true);
        d.set("Configurable", true);
        d.set("Enumerable", true);
        return d;
    }

    public static PropertyDescriptor newPropertyDescriptorForObjectInitializerGet(Object orig, String name, JSFunction value) {
        value.setDebugContext("Object.get " + name);
        PropertyDescriptor d = null;

        if (orig == Types.UNDEFINED) {
            d = new PropertyDescriptor();
        } else {
            d = (PropertyDescriptor) orig;
        }
        d.set("Get", value);
        d.set("Configurable", true);
        d.set("Enumerable", true);
        return d;
    }

    public static PropertyDescriptor newPropertyDescriptorForObjectInitializerSet(Object orig, String name, JSFunction value) {
        value.setDebugContext("Object.set " + name);
        PropertyDescriptor d = null;

        if (orig == Types.UNDEFINED) {
            d = new PropertyDescriptor();
        } else {
            d = (PropertyDescriptor) orig;
        }
        d.set("Set", value);
        d.set("Configurable", true);
        d.set("Enumerable", true);
        return d;
    }

    public PropertyDescriptor() {
    }

    public String toString() {
        return "[PropertyDescriptor: attributes=" + this.attributes.keySet() + "]";
    }

    public boolean isWritable() {
        Object v = get("Writable");
        if (v == null) {
            return false;
        }
        return (Boolean) v;
    }

    public boolean hasWritable() {
        return this.attributes.containsKey("Writable");
    }

    public void setWritable(boolean writable) {
        set("Writable", writable);
    }

    public boolean isConfigurable() {
        Object v = get("Configurable");
        if (v == null) {
            return false;
        }

        if (v == Types.UNDEFINED) {
            return false;
        }

        return (Boolean) v;
    }

    public boolean hasConfigurable() {
        return this.attributes.containsKey("Configurable");
    }

    public void setConfigurable(boolean configurable) {
        set("Configurable", configurable);
    }

    public boolean isEnumerable() {
        Object v = get("Enumerable");
        if (v == null) {
            return false;
        }

        return (Boolean) v;
    }

    public void setEnumerable(boolean enumerable) {
        set("Enumerable", enumerable);
    }

    public boolean hasEnumerable() {
        return this.attributes.containsKey("Configurable");
    }

    public Object getValue() {
        return get("Value");
    }

    public void setValue(Object value) {
        set("Value", value);
    }

    public Object getSetter() {
        return get("Set");
    }

    public void setSetter(JSFunction setter) {
        set("Set", setter);
    }

    public Object getGetter() {
        return get("Get");
    }

    public void setGetter(JSFunction getter) {
        set("Get", getter);
    }

    public boolean isEmpty() {
        return this.attributes.isEmpty();
    }

    public void set(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object get(String name) {
        Object v = this.attributes.get(name);
        if (v == null) {
            return Types.UNDEFINED;
        }

        return v;
    }

    public boolean isPresent(String name) {
        return this.attributes.containsKey(name);
    }

    public Object getWithDefault(String name) {
        Object v = this.attributes.get(name);
        if (v == null) {
            return DEFAULTS.get(name);
        }

        return v;
    }

    public PropertyDescriptor duplicate(String... attributes) {
        PropertyDescriptor d = new PropertyDescriptor();
        for (int i = 0; i < attributes.length; ++i) {
            d.set(attributes[i], get(attributes[i]));
        }
        return d;
    }

    public PropertyDescriptor duplicateWithDefaults(String... attributes) {
        PropertyDescriptor d = new PropertyDescriptor();
        for (int i = 0; i < attributes.length; ++i) {
            d.set(attributes[i], getWithDefault(attributes[i]));
        }
        return d;
    }

    public void copyAll(PropertyDescriptor from) {
        attributes.putAll(from.attributes);
    }

    public boolean isAccessorDescriptor() {
        // 8.10.1
        if (attributes.get("Get") == null && attributes.get("Set") == null) {
            return false;
        }

        return true;
    }

    public boolean isDataDescriptor() {
        // 8.10.2
        if (attributes.get("Value") == null && attributes.get("Writable") == null) {
            return false;
        }

        return true;
    }

    public boolean isGenericDescriptor() {
        // 8.10.3
        if (isAccessorDescriptor() == false && isDataDescriptor() == false) {
            return true;
        }

        return false;
    }

    public static Object fromPropertyDescriptor(ExecutionContext context, Object d) {
        // 8.10.4
        if (d == Types.UNDEFINED) {
            return Types.UNDEFINED;
        }

        final PropertyDescriptor desc = (PropertyDescriptor) d;

        DynObject obj = new DynObject( context.getGlobalObject() );

        if (desc.isDataDescriptor()) {
            obj.defineOwnProperty(context, "value", new PropertyDescriptor() {
                {
                    set("Value", desc.get("Value"));
                    set("Writable", true);
                    set("Configurable", true);
                    set("Enumerable", true);
                }
            }, false);
            obj.defineOwnProperty(context, "writable", new PropertyDescriptor() {
                {
                    set("Value", desc.get("Writable"));
                    set("Writable", true);
                    set("Configurable", true);
                    set("Enumerable", true);
                }
            }, false);
        } else {
            obj.defineOwnProperty(context, "get", new PropertyDescriptor() {
                {
                    set("Value", desc.get("Get"));
                    set("Writable", true);
                    set("Configurable", true);
                    set("Enumerable", true);
                }
            }, false);
            obj.defineOwnProperty(context, "set", new PropertyDescriptor() {
                {
                    set("Value", desc.get("Set"));
                    set("Writable", true);
                    set("Configurable", true);
                    set("Enumerable", true);
                }
            }, false);
        }

        obj.defineOwnProperty(context, "enumerable", new PropertyDescriptor() {
            {
                set("Value", desc.get("Enumerable"));
                set("Writable", true);
                set("Configurable", true);
                set("Enumerable", true);
            }
        }, false);
        obj.defineOwnProperty(context, "configurable", new PropertyDescriptor() {
            {
                set("Value", desc.get("Configurable"));
                set("Writable", true);
                set("Configurable", true);
                set("Enumerable", true);
            }
        }, false);

        return obj;
    }

    public static PropertyDescriptor toPropertyDescriptor(ExecutionContext context, Object o) {
        // 8.10.5
        if (!(o instanceof JSObject)) {
            throw new ThrowException(context.createTypeError("attribtues must be an object"));
        }

        JSObject obj = (JSObject) o;
        PropertyDescriptor d = new PropertyDescriptor();

        if (obj.hasProperty(context, "enumerable")) {
            d.set("Enumerable", Types.toBoolean(obj.get(context, "enumerable")));
        }
        if (obj.hasProperty(context, "configurable")) {
            d.set("Configurable", Types.toBoolean(obj.get(context, "configurable")));
        }
        if (obj.hasProperty(context, "value")) {
            d.set("Value", obj.get(context, "value"));
        }
        if (obj.hasProperty(context, "writable")) {
            d.set("Writable", Types.toBoolean(obj.get(context, "writable")));
        }
        if (obj.hasProperty(context, "get")) {
            Object getter = obj.get(context, "get");
            if ((!Types.isCallable(getter)) && (getter != Types.UNDEFINED)) {
                throw new ThrowException(context.createTypeError("get must be callable"));
            }
            d.set("Get", getter);
        }
        if (obj.hasProperty(context, "set")) {
            Object setter = obj.get(context, "set");
            if ((!Types.isCallable(setter)) && (setter != Types.UNDEFINED)) {
                throw new ThrowException(context.createTypeError("set must be callable"));
            }
            d.set("Set", setter);
        }

        if ((d.get("Get") != Types.UNDEFINED) || (d.get("Set") != Types.UNDEFINED)) {
            if ((d.get("Writable") != Types.UNDEFINED) || (d.get("Value") != Types.UNDEFINED)) {
                throw new ThrowException(context.createTypeError("may not be both a data property and an accessor property"));
            }
        }

        return d;
    }
}
