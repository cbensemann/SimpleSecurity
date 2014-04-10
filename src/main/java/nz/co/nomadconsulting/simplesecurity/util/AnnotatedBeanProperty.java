package nz.co.nomadconsulting.simplesecurity.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


public class AnnotatedBeanProperty<T extends Annotation> {

    private Field propertyField;

    private Method propertyGetter;

    private Method propertySetter;

    private String name;

    private Type propertyType;

    private T annotation;

    private boolean isFieldProperty;

    private boolean set = false;


    public AnnotatedBeanProperty(final Class<?> cls,
            final Class<T> annotationClass) {
        // First check declared fields
        for (final Field f : cls.getDeclaredFields()) {
            if (f.isAnnotationPresent(annotationClass)) {
                setupFieldProperty(f);
                this.annotation = f.getAnnotation(annotationClass);
                set = true;
                return;
            }
        }

        // Then check public fields, in case it's inherited
        for (final Field f : cls.getFields()) {
            if (f.isAnnotationPresent(annotationClass)) {
                this.annotation = f.getAnnotation(annotationClass);
                setupFieldProperty(f);
                set = true;
                return;
            }
        }

        // Then check public methods (we ignore private methods)
        for (final Method m : cls.getMethods()) {
            if (m.isAnnotationPresent(annotationClass)) {
                this.annotation = m.getAnnotation(annotationClass);
                final String methodName = m.getName();

                if (m.getName().startsWith("get")) {
                    this.name = Introspector.decapitalize(m.getName()
                            .substring(3));
                }
                else if (methodName.startsWith("is")) {
                    this.name = Introspector.decapitalize(m.getName()
                            .substring(2));
                }

                if (this.name != null) {
                    this.propertyGetter = Reflections.getGetterMethod(cls,
                            this.name);
                    this.propertySetter = Reflections.getSetterMethod(cls,
                            this.name);
                    this.propertyType = this.propertyGetter
                            .getGenericReturnType();
                    isFieldProperty = false;
                    set = true;
                }
                else {
                    throw new IllegalStateException(
                            "Invalid accessor method, must start with 'get' or 'is'.  "
                                    + "Method: " + m + " in class: " + cls);
                }
            }
        }
    }


    private void setupFieldProperty(final Field propertyField) {
        this.propertyField = propertyField;
        isFieldProperty = true;
        this.name = propertyField.getName();
        this.propertyType = propertyField.getGenericType();
    }


    public void setValue(final Object bean, final Object value) {
        if (isFieldProperty) {
            Reflections.setAndWrap(propertyField, bean, value);
        }
        else {
            Reflections.invokeAndWrap(propertySetter, bean, value);
        }
    }


    public Object getValue(final Object bean) {
        if (isFieldProperty) {
            return Reflections.getAndWrap(propertyField, bean);
        }
        else {
            return Reflections.invokeAndWrap(propertyGetter, bean);
        }
    }


    public String getName() {
        return name;
    }


    public T getAnnotation() {
        return annotation;
    }


    public Type getPropertyType() {
        return propertyType;
    }


    public boolean isSet() {
        return set;
    }
}
