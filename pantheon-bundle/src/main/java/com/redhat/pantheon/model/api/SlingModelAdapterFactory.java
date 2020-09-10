package com.redhat.pantheon.model.api;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter factory for model objects of type {@link SlingModel}. This component makes sure that
 * calls to {@link Resource#adaptTo(Class)} with a sub-interface of {@link SlingModel} as a parameter
 * will return an object.
 *
 * Types need to registered as adapter classes by simply adding a property in the
 * {@link org.osgi.service.component.annotations.Component} definition at the top of this class.
 *
 * @author Carlos Munoz
 */
public abstract class SlingModelAdapterFactory implements AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(SlingModelAdapterFactory.class);

    @Override
    public final <AdapterType> @Nullable AdapterType getAdapter(@NotNull Object adaptable, @NotNull Class<AdapterType> type) {
        // ensure the adapter type is a subclass of SlingResource
        if(!SlingModel.class.isAssignableFrom(type)) {
            log.error("Error in " + this.getClass().getSimpleName() + ": adapter type not a SlingModel interface ("
                    + type.getName() + ")");
            return null;
        }

        // Ensure the adaptable object is of an appropriate type
        if (!(adaptable instanceof Resource) || (adaptable == null)) {
            log.error("Trying to adapt object {" + adaptable + "} to " + type.getName()
                    + ", but object is null or not of type Resource");
            return null;
        }

        Resource resource = (Resource) adaptable;
        Class<? extends SlingModel> slingResourceType = (Class<? extends SlingModel>)type;

        // If the adapter interface is just SlingModel
        if(SlingModel.class.equals(type)) {
            return (AdapterType) new ResourceDecorator(resource);
        }

        // the adapter type (sublass of SlingResource) should have a one arg constructor which takes a resource
        SlingModel adapter = SlingModels.getModel(resource, slingResourceType);

        return (AdapterType) adapter;
    }
}
