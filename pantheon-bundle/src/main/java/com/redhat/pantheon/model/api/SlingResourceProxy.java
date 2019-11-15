package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.v2.Child;
import com.redhat.pantheon.model.api.v2.Field;
import org.apache.sling.api.resource.Resource;

import javax.inject.Named;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * A specialized {@link SlingResource} extension which also acts as an {@link InvocationHandler}
 * to provide additional methods in {@link SlingModel} interfaces. It provides all the existing
 * methods in the base class, plus the ability to process additional ones which conform to the
 * model contract.
 *
 * The {@link InvocationHandler}'s invoke method is responsible for handling all the additional
 * methods which might come from the specific model interface.
 *
 * @author Carlos Munoz
 */
class SlingResourceProxy extends SlingResource implements InvocationHandler {

    public SlingResourceProxy(Resource wrapped) {
        super(wrapped);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // methods which access a Field
        if( isFieldAccessor(method) ) {
            String fieldName = extractFieldName(method);
            Class fieldType = extractParameterizedReturnType(method);
            return this.field(fieldName, fieldType);
        }
        // methods which access a Child
        else if( isChildAccessor(method) ) {
            String childName = extractFieldName(method);
            Class childType = extractParameterizedReturnType(method);
            return this.child(childName, childType);
        }
        // by default pass to the calling object
        // If the method isn't defined, there will be an exception
        return method.invoke(this, args);
    }

    private static boolean isChildAccessor(Method method) {
        return method.getReturnType().equals(Child.class) && method.getParameters().length == 0;
    }

    private static boolean isFieldAccessor(Method method) {
        return method.getReturnType().equals(Field.class) && method.getParameters().length == 0;
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
