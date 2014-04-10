package nz.co.nomadconsulting.eesecurity.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Reflections {

    public static Object invoke(final Method method, final Object target,
            final Object... args) throws Exception {
        try {
            return method.invoke(target, args);
        }
        catch (final IllegalArgumentException iae) {
            String message = "Could not invoke method by reflection: "
                    + toString(method);
            if (args != null && args.length > 0) {
                message += " with parameters: ("
                        + Strings.toClassNameString(", ", args) + ')';
            }
            message += " on: " + target.getClass().getName();
            throw new IllegalArgumentException(message, iae);
        }
        catch (final InvocationTargetException ite) {
            if (ite.getCause() instanceof Exception) {
                throw (Exception) ite.getCause();
            }
            else {
                throw ite;
            }
        }
    }


    public static Object get(final Field field, final Object target)
            throws Exception {
        final boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            return field.get(target);
        }
        catch (final IllegalArgumentException iae) {
            final String message = "Could not get field value by reflection: "
                    + toString(field) + " on: " + target.getClass().getName();
            throw new IllegalArgumentException(message, iae);
        }
        finally {
            field.setAccessible(accessible);
        }
    }


    public static void set(final Field field, final Object target,
            final Object value) throws Exception {
        try {
            field.set(target, value);
        }
        catch (final IllegalArgumentException iae) {
            // target may be null if field is static so use
            // field.getDeclaringClass() instead
            String message = "Could not set field value by reflection: "
                    + toString(field) + " on: "
                    + field.getDeclaringClass().getName();
            if (value == null) {
                message += " with null value";
            }
            else {
                message += " with value: " + value.getClass();
            }
            throw new IllegalArgumentException(message, iae);
        }
    }


    public static Object getAndWrap(final Field field, final Object target) {
        final boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            return get(field, target);
        }
        catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            else {
                throw new IllegalArgumentException("exception setting: "
                        + field.getName(), e);
            }
        }
        finally {
            field.setAccessible(accessible);
        }
    }


    public static void setAndWrap(final Field field, final Object target,
            final Object value) {
        final boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            set(field, target, value);
        }
        catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            else {
                throw new IllegalArgumentException("exception setting: "
                        + field.getName(), e);
            }
        }
        finally {
            field.setAccessible(accessible);
        }
    }


    public static Object invokeAndWrap(final Method method,
            final Object target, final Object... args) {
        try {
            return invoke(method, target, args);
        }
        catch (final RuntimeException re) {
            throw re;
        }
        catch (final Exception e) {
            throw new RuntimeException("exception invoking: "
                    + method.getName(), e);
        }
    }


    public static String toString(final Method method) {
        return Strings.unqualify(method.getDeclaringClass().getName()) + '.'
                + method.getName() + '('
                + Strings.toString(", ", method.getParameterTypes()) + ')';
    }


    public static String toString(final Member member) {
        return Strings.unqualify(member.getDeclaringClass().getName()) + '.'
                + member.getName();
    }


    public static Class<?> classForName(final String name)
            throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader()
                    .loadClass(name);
        }
        catch (final Exception e) {
            return Class.forName(name);
        }
    }


    /**
     * Return's true if the Class<?>can be loaded using
     * Reflections.classForName()
     */
    public static boolean isClassAvailable(final String name) {
        try {
            classForName(name);
        }
        catch (final ClassNotFoundException e) {
            return false;
        }
        return true;
    }


    public static Class<?> getCollectionElementType(final Type collectionType) {
        if (!(collectionType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "collection type not parameterized");
        }
        final Type[] typeArguments = ((ParameterizedType) collectionType)
                .getActualTypeArguments();
        if (typeArguments.length == 0) {
            throw new IllegalArgumentException(
                    "no type arguments for collection type");
        }
        Type typeArgument = typeArguments.length == 1 ? typeArguments[0]
                : typeArguments[1]; // handle
        // Maps
        if (typeArgument instanceof ParameterizedType) {
            typeArgument = ((ParameterizedType) typeArgument).getRawType();
        }
        if (!(typeArgument instanceof Class)) {
            throw new IllegalArgumentException("type argument not a class");
        }
        return (Class<?>) typeArgument;
    }


    public static Class<?> getMapKeyType(final Type collectionType) {
        if (!(collectionType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "collection type not parameterized");
        }
        final Type[] typeArguments = ((ParameterizedType) collectionType)
                .getActualTypeArguments();
        if (typeArguments.length == 0) {
            throw new IllegalArgumentException(
                    "no type arguments for collection type");
        }
        final Type typeArgument = typeArguments[0];
        if (!(typeArgument instanceof Class)) {
            throw new IllegalArgumentException("type argument not a class");
        }
        return (Class<?>) typeArgument;
    }


    public static Method getSetterMethod(final Class<?> clazz, final String name) {
        final Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            final String methodName = method.getName();
            if (methodName.startsWith("set")
                    && method.getParameterTypes().length == 1) {
                if (Introspector.decapitalize(methodName.substring(3)).equals(
                        name)) {
                    return method;
                }
            }
        }
        throw new IllegalArgumentException("no such setter method: "
                + clazz.getName() + '.' + name);
    }


    public static Method getGetterMethod(final Class<?> clazz, final String name) {
        final Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            final String methodName = method.getName();
            if (method.getParameterTypes().length == 0) {
                if (methodName.startsWith("get")) {
                    if (Introspector.decapitalize(methodName.substring(3))
                            .equals(name)) {
                        return method;
                    }
                }
                else if (methodName.startsWith("is")) {
                    if (Introspector.decapitalize(methodName.substring(2))
                            .equals(name)) {
                        return method;
                    }
                }
            }
        }
        throw new IllegalArgumentException("no such getter method: "
                + clazz.getName() + '.' + name);
    }


    /**
     * Get all the getter methods annotated with the given annotation. Returns
     * an empty list if none are found
     */
    public static List<Method> getGetterMethods(final Class<?> clazz,
            final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        for (final Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                methods.add(method);
            }
        }
        return methods;
    }


    public static Field getField(final Class<?> clazz, final String name) {
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(name);
            }
            catch (final NoSuchFieldException nsfe) {
            }
        }
        throw new IllegalArgumentException("no such field: " + clazz.getName()
                + '.' + name);
    }


    /**
     * Get all the fields which are annotated with the given annotation. Returns
     * an empty list if none are found
     */
    public static List<Field> getFields(final Class<?> clazz,
            final Class<? extends Annotation> annotation) {
        final List<Field> fields = new ArrayList<Field>();
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            for (final Field field : superClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }


    public static Method getMethod(final Annotation annotation,
            final String name) {
        try {
            return annotation.annotationType().getMethod(name);
        }
        catch (final NoSuchMethodException nsme) {
            return null;
        }
    }


    public static Method getMethod(final Class<?> clazz, final String name) {
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(name);
            }
            catch (final NoSuchMethodException nsme) {
            }
        }
        throw new IllegalArgumentException("no such method: " + clazz.getName()
                + '.' + name);
    }


    /**
     * Check to see if clazz is an instance of name
     */
    public static boolean isInstanceOf(final Class<?> clazz, final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            if (instanceOf(c, name)) {
                return true;
            }
        }
        return false;
    }


    private static boolean instanceOf(final Class<?> clazz, final String name) {
        if (name.equals(clazz.getName())) {
            return true;
        }
        else {
            boolean found = false;
            final Class<?>[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length && !found; i++) {
                found = instanceOf(interfaces[i], name);
            }
            return found;
        }
    }
}
