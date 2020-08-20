package com.redhat.pantheon.model.api;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.sling.api.adapter.AdapterFactory.ADAPTABLE_CLASSES;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTER_CLASSES;

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
@Component(
        name = "Pantheon - SlingModel Adapter Factory",
        service = AdapterFactory.class,
        property = {
                ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.api.SlingModel",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.api.FileResource",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.document.Document",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.Module",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleVersion",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.Content",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleMetadata",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleLocale",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.Product",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.ProductVersion",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.workspace.Workspace",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleVariant",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.Assembly",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.AssemblyVersion",
        }
)
public class SlingModelAdapterFactory implements AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(SlingModelAdapterFactory.class);

    @Override
    public <AdapterType> @Nullable AdapterType getAdapter(@NotNull Object adaptable, @NotNull Class<AdapterType> type) {
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
