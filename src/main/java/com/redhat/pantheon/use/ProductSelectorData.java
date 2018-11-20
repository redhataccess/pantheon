package com.redhat.pantheon.use;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.scripting.sightly.pojo.Use;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.script.Bindings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ProductSelectorData implements Use {

    private ResourceResolver resolver;
    private Resource currentResource;

    @Override
    public void init(Bindings bindings) {
        resolver = (ResourceResolver) bindings.get("resolver");
        currentResource = (Resource) bindings.get("resource");
    }

    public List<Option> getAvailableProducts() {
        Resource productsNode = resolver.getResource("/content/products");
        List<Option> returnVal = new ArrayList<>();

        productsNode.getChildren().forEach(product -> {
            String productUUID = product.getValueMap().get("jcr:uuid", String.class);
            returnVal.add(new SelectedOption(
                    product.getValueMap().get("pant:canonicalName", String.class),
                    productUUID,
                    Arrays.stream(currentResource.getValueMap().get("pant:relatedProducts", new String[]{})).anyMatch( productUUID::equals )
            ));
        });

        return returnVal;
    }

    public List<Option> getRelatedProducts() {
        ArrayList<Option> returnVal = new ArrayList<>();
        if( currentResource.getValueMap().containsKey("pant:relatedProducts") ) {
            Session session = resolver.adaptTo(Session.class);
            Arrays.stream(currentResource.getValueMap().get("pant:relatedProducts", String[].class)).forEach( i -> {
                try {
                    // switch to use the JCR API directly (vs the Sling one)
                    Node n = session.getNodeByUUID(i);
                    returnVal.add(
                            new Option(n.getProperty("pant:canonicalName").getString(), n.getUUID())
                    );
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            } );
        }
        return returnVal;
    }

    public static class Option {

        private String name;
        private String value;

        public Option(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SelectedOption extends Option {

        private boolean selected;


        public SelectedOption(String name, String value, boolean selected) {
            super(name, value);
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }
    }
}
