package com.redhat.pantheon.model.api;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

import static org.apache.sling.api.adapter.AdapterFactory.ADAPTABLE_CLASSES;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTER_CLASSES;

/**
 * An adapter factory for model objects of type JcrModel. This component makes sure that
 * calls to {@link Resource#adaptTo(Class)} with a subtype of {@link JcrModel} as a parameter
 * will return an object.
 *
 * Types need to registered as adapter classes by simply adding a property in the @{@link Component}
 * definition at the top of this class.
 */
@Component(
        name = "Pantheon - JCRModel Adapter Factory",
        service = AdapterFactory.class,
        property = {
                ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.Module",
        }
)
public class JcrModelAdapterFactory implements AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(JcrModelAdapterFactory.class);

    @Override
    public <AdapterType> @Nullable AdapterType getAdapter(@NotNull Object adaptable, @NotNull Class<AdapterType> type) {

        // ensure the adapter type is a subclass of JcrModel
        if(!JcrModel.class.isAssignableFrom(type)) {
            log.error("Error in " + this.getClass().getSimpleName() + ": adapter type not a subclass of JcrModel ("
                    + type.getName() + ")");
            return null;
        }

        // Ensure the adaptable object is of an appropriate type
        if (!(adaptable instanceof Resource) || (adaptable == null)) {
            log.error("Trying to adapt object {" + adaptable + "} to JcrModel, but object is null or not of type Resource");
            return null;
        }

        Resource resource = ((Resource) adaptable);

        // the adapter type (sublass of JcrModel) should have a one arg constructor which takes a resource
        AdapterType adapter = null;
        try {
            adapter = type.getConstructor(Resource.class)
                    .newInstance(resource);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error when adapting object to JcrModel",  e);
        }

        return adapter;
    }
}
