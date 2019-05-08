package com.redhat.pantheon.sling;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.function.Consumer;

@Component(
        service = ServiceResourceResolverProvider.class
)
public class ServiceResourceResolverProvider {

    private ResourceResolverFactory resourceResolverFactory;

    @Activate
    public ServiceResourceResolverProvider(
            @Reference ResourceResolverFactory resourceResolverFactory) {
        this.resourceResolverFactory = resourceResolverFactory;
    }

    public ResourceResolver getServiceResourceResolver() {
        try {
            return resourceResolverFactory.getServiceResourceResolver(null);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }

    public void withServiceResourceResolver(Consumer<ResourceResolver> function) {
        final ResourceResolver serviceResourceResolver = this.getServiceResourceResolver();
        function.accept(serviceResourceResolver);
        serviceResourceResolver.close();
    }
}
