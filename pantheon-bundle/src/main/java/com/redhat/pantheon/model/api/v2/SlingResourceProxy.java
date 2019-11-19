package com.redhat.pantheon.model.api.v2;

import org.apache.sling.api.resource.Resource;

import javax.inject.Named;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * A specialized {@link ResourceDecorator} extension which also acts as an {@link InvocationHandler}
 * to provide additional methods in {@link SlingModel} interfaces. It provides all the existing
 * methods in the base class, plus the ability to process additional ones which conform to the
 * model contract.
 *
 * The {@link InvocationHandler}'s invoke method is responsible for handling all the additional
 * methods which might come from the specific model interface.
 *
 * @author Carlos Munoz
 */
class SlingResourceProxy extends ResourceDecorator implements InvocationHandler {

    public SlingResourceProxy(Resource wrapped) {
        super(wrapped);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // default interface methods
        // (default methods declared in the SlingModel interface themselves)
        if( method.isDefault() ) {
            // FIXME This will need to change in Java8+ versions
            // See: https://blog.jooq.org/2018/03/28/correct-reflective-access-to-interface-default-methods-in-java-8-9-10/
            final Class<?> declaringClass = method.getDeclaringClass();
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            return constructor.newInstance(declaringClass)
                    .in(declaringClass)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }
        // methods which access an enum Field
        else if( isEnumFieldAccessor(method) ) {
            String fieldName = extractFieldName(method);
            Class fieldType = extractParameterizedReturnType(method);
            return new EnumFieldImpl(fieldName, fieldType, this);
        }
        // methods which access a Field
        else if( isFieldAccessor(method) ) {
            String fieldName = extractFieldName(method);
            Class fieldType = extractParameterizedReturnType(method);
            return new FieldImpl(fieldName, fieldType, this);
        }
        // methods which access a Child
        else if( isChildAccessor(method) ) {
            String childName = extractFieldName(method);
            Class childType = extractParameterizedReturnType(method);
            return new ChildImpl(childName, childType, this);
        }
        // methods which access a Reference field
        else if( isReferenceAccessor(method) ) {
            String referenceName = extractFieldName(method);
            Class referenceType = extractParameterizedReturnType(method);
            return new ReferenceFieldImpl(referenceName, referenceType, this);
        }
        // by default pass to the calling object
        // If the method isn't defined, there will be an exception
        return method.invoke(this, args);
    }

    private static boolean isEnumFieldAccessor(Method method) {
        return isFieldAccessor(method) && Enum.class.isAssignableFrom(extractParameterizedReturnType(method));
    }

    private static boolean isChildAccessor(Method method) {
        return method.getReturnType().equals(Child.class) && method.getParameters().length == 0;
    }

    private static boolean isFieldAccessor(Method method) {
        return method.getReturnType().equals(Field.class) && method.getParameters().length == 0;
    }

    private static boolean isReferenceAccessor(Method method) {
        return method.getReturnType().equals(Reference.class) && method.getParameters().length == 0;
    }

    private static String extractFieldName(Method method) {
        Named namedAnnotation = method.getAnnotation(Named.class);

        String fieldName = method.getName();
        if(namedAnnotation != null) {
            fieldName = namedAnnotation.value();
        }
        return fieldName;
    }

    private static Class<?> extractParameterizedReturnType(Method method) {
        // TODO This might fail if the type isn't parameterized
        return (Class)((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0];
    }
}
