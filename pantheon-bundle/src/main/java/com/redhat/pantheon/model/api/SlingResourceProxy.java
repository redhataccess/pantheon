package com.redhat.pantheon.model.api;

import com.redhat.pantheon.model.api.util.Memoizer;
import org.apache.sling.api.resource.Resource;

import javax.inject.Named;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.function.Function;

import static com.redhat.pantheon.model.api.SlingModels.getModel;

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

    private enum MethodType {
        DefaultInterfaceMethod,
        FieldAccessor,
        ChildAccessor,
        ReferenceFieldAccessor,
        ParentAccessorOverride,
        Unknown
    }

    private static final Function<Method, MethodType> methodClassifier
            = Memoizer.memoize(method -> getMethodType(method));

    public SlingResourceProxy(Resource wrapped) {
        super(wrapped);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodType methodType = methodClassifier.apply(method);
        switch (methodType) {
            case DefaultInterfaceMethod: {
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

            case FieldAccessor: {
                String fieldName = extractFieldName(method);
                Class fieldType = extractParameterizedReturnType(method);
                return this.field(fieldName, fieldType);
            }

            case ChildAccessor: {
                String childName = extractFieldName(method);
                Class childType = extractParameterizedReturnType(method);
                return this.child(childName, childType);
            }

            case ReferenceFieldAccessor: {
                String referenceName = extractFieldName(method);
                Class referenceType = extractParameterizedReturnType(method);
                return this.reference(referenceName, referenceType);
            }

            case ParentAccessorOverride: {
                return getModel(getParent(), (Class<? extends SlingModel>) method.getReturnType());
            }

            default:
                return method.invoke(this, args);
        }
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

    private static boolean overridesParentAccessor(Method method) {
        return SlingModel.class.isAssignableFrom(method.getReturnType())
                && "getParent".equals(method.getName())
                && method.getParameters().length == 0;
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
        Type t = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0];
        if (t instanceof WildcardType) {
            WildcardType w = (WildcardType) t;
            return (Class) w.getUpperBounds()[0];
        }
        return (Class) t;
    }

    private static MethodType getMethodType(Method method) {
        // default interface methods
        // (default methods declared in the SlingModel interface themselves)
        if( method.isDefault() ) {
            return MethodType.DefaultInterfaceMethod;
        }
        // methods which access a Field
        else if( isFieldAccessor(method) ) {
            return MethodType.FieldAccessor;
        }
        // methods which access a Child
        else if( isChildAccessor(method) ) {
            return MethodType.ChildAccessor;
        }
        // methods which access a Reference field
        else if( isReferenceAccessor(method) ) {
            return MethodType.ReferenceFieldAccessor;
        }
        // methods overriding the 'getParent' method with specific types
        else if( overridesParentAccessor(method) ) {
            return MethodType.ParentAccessorOverride;
        }
        return MethodType.Unknown;
    }
}
