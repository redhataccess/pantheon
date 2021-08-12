package com.redhat.pantheon.model;

import com.redhat.pantheon.model.api.SlingModelAdapterFactory;
import org.apache.sling.api.adapter.AdapterFactory;
import org.osgi.service.component.annotations.Component;

import static org.apache.sling.api.adapter.AdapterFactory.ADAPTABLE_CLASSES;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTER_CLASSES;

/**
 * Custom {@link SlingModelAdapterFactory} for the Pantheon system. It lists all the
 * model classes which will be converted from the adaptTo invocations.
 */
@Component(
        name = "Pantheon - SlingModel Adapter Factory",
        service = AdapterFactory.class,
        property = {
                ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.Content",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.Product",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.ProductVersion",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.api.SlingModel",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.api.FileResource",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.Assembly",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.AssemblyLocale",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.AssemblyMetadata",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.AssemblyPage",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.AssemblyVariant",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.AssemblyVariants",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.assembly.AssemblyVersion",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.document.Document",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.document.DocumentLocale",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.document.DocumentMetadata",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.document.DocumentVariant",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.document.DocumentVariants",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.document.DocumentVersion",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.Module",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleLocale",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleMetadata",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleVariant",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleVariants",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.module.ModuleVersion",
                ADAPTER_CLASSES + "=com.redhat.pantheon.validation.model.Validation",
                ADAPTER_CLASSES + "=com.redhat.pantheon.model.workspace.Workspace"
        }
)
public class CustomModelAdapterFactory extends SlingModelAdapterFactory {
}
