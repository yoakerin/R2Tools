package com.yoake.r2base.helper;

import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class R2ReflectHelper {
    private static final String TAG = "R2ReflectHelper";

    private R2ReflectHelper() {
    }
    @SuppressWarnings("unchecked")
    public static <T> T getStaticFieldValue(final Class<?> cls, final String name) {
        if (null != cls && null != name) {
            try {
                final Field field = getField(cls, name);
                if (null != field) {
                    field.setAccessible(true);
                    return (T) field.get(cls);
                }
            } catch (final Throwable t) {
                Log.w(TAG, "get static field " + name + " of " + cls + " error", t);
            }
        }

        return null;
    }

    public static boolean setStaticFieldValue(final Class<?> cls, final String name, final Object value) {
        if (null != cls && null != name) {
            try {
                final Field field = getField(cls, name);
                if (null != field) {
                    field.setAccessible(true);
                    field.set(cls, value);
                    return true;
                }
            } catch (final Throwable t) {
                Log.w(TAG, "set static field " + name + " of " + cls + " error", t);
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T>  T  getFieldValue(final Object obj, final String name) {
        if (null != obj && null != name) {
            try {
                final Field field = getField(obj.getClass(), name);
                if (null != field) {
                    field.setAccessible(true);
                    return (T) field.get(obj);
                }
            } catch (final Throwable t) {
                Log.w(TAG, "get field " + name + " of " + obj + " error", t);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(final Object obj, final Class<?> type) {
        if (null != obj && null != type) {
            try {
                final Field field = getField(obj.getClass(), type);
                if (null != field) {
                    field.setAccessible(true);
                    return (T) field.get(obj);
                }
            } catch (final Throwable t) {
                Log.w(TAG, "get field with type " + type + " of " + obj + " error", t);
            }
        }

        return null;
    }

    public static boolean setFieldValue(final Object obj, final String name, final Object value) {
        if (null != obj && null != name) {
            try {
                final Field field = getField(obj.getClass(), name);
                if (null != field) {
                    field.setAccessible(true);
                    field.set(obj, value);
                    return true;
                }
            } catch (final Throwable t) {
                Log.w(TAG, "set field " + name + " of " + obj + " error", t);
            }
        }

        return false;
    }

    public static <T> T newInstance(final String className, final Object... args) {
        try {
            return newInstance(Class.forName(className), args);
        } catch (final ClassNotFoundException e) {
            Log.w(TAG, "new instance of " + className + " error", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(final Class<?> clazz, Object... args) {
        final Constructor<?>[] ctors = clazz.getDeclaredConstructors();

        loop:
        for (final Constructor<?> ctor : ctors) {
            final Class<?>[] types = ctor.getParameterTypes();
            if (types.length == args.length) {
                for (int i = 0; i < types.length; i++) {
                    if (null != args[i] && !types[i].isAssignableFrom(args[i].getClass())) {
                        continue loop;
                    }
                }

                try {
                    ctor.setAccessible(true);
                    return (T) ctor.newInstance(args);
                } catch (final Throwable t) {
                    Log.w(TAG, "Invoke constructor " + ctor + " error", t);
                    return null;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeStaticMethod(final Class<?> klass, final String name) {
        return invokeStaticMethod(klass, name, new Class[0], new Object[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeStaticMethod(final Class<?> klass, final String name, final Class[] types, final Object[] args) {
        if (null != klass && null != name && null != types && null != args && types.length == args.length) {
            try {
                final Method method = getMethod(klass, name, types);
                if (null != method) {
                    method.setAccessible(true);
                    return (T) method.invoke(klass, args);
                }
            } catch (final Throwable e) {
                Log.w(TAG, "Invoke " + name + "(" + Arrays.toString(types) + ") of " + klass + " error", e);
            }
        }

        return null;
    }


    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(final Object obj, final String name) {
        return invokeMethod(obj, name, new Class[0], new Object[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(final Object obj, final String name, final Class[] types, final Object[] args) {
        if (null != obj && null != name && null != types && null != args && types.length == args.length) {
            try {
                final Method method = getMethod(obj.getClass(), name, types);
                if (null != method) {
                    method.setAccessible(true);
                    return (T) method.invoke(obj, args);
                }
            } catch (final Throwable e) {
                Log.w(TAG, "Invoke " + name + "(" + Arrays.toString(types) + ") of " + obj + " error", e);
            }
        }

        return null;
    }

    public static Field getField(final Class<?> cls, final String name) {
        try {
            return cls.getDeclaredField(name);
        } catch (final NoSuchFieldException e) {
            final Class<?> parent = cls.getSuperclass();
            if (null == parent) {
                return null;
            }
            return getField(parent, name);
        }
    }

    public static Field getField(final Class<?> cls, final Class<?> type) {
        final Field[] fields = cls.getDeclaredFields();
        if (fields.length <= 0) {
            final Class<?> parent = cls.getSuperclass();
            if (null == parent) {
                return null;
            }
            return getField(parent, type);
        }

        for (final Field field : fields) {
            if (field.getType() == type) {
                return field;
            }
        }

        return null;
    }

    private static Method getMethod(final Class<?> cls, final String name, final Class<?>[] types) {
        try {
            return cls.getDeclaredMethod(name, types);
        } catch (final NoSuchMethodException e) {
            final Class<?> parent = cls.getSuperclass();
            if (null == parent) {
                return null;
            }
            return getMethod(parent, name, types);
        }
    }
}
