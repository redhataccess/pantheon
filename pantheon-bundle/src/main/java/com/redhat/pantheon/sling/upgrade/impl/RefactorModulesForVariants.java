package com.redhat.pantheon.sling.upgrade.impl;

import com.redhat.pantheon.sling.upgrade.RepositoryUpgrade;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

/**
 * @author Carlos Munoz
 */
public class RefactorModulesForVariants implements RepositoryUpgrade {

    private static final Logger log = LoggerFactory.getLogger(RefactorModulesForVariants.class);

    @Override
    public String getId() {
        return "refactor-modules-for-variants";
    }

    @Override
    public String getDescription() {
        return "Refactors all modules adding a DEFAULT variant";
    }

    @Override
    public void executeUpgrade(ResourceResolver resourceResolver) throws Exception {
        Session session = resourceResolver.adaptTo(Session.class);

        // Process all workspaces
        Query query = session.getWorkspace().getQueryManager().createQuery("select * from [pant:workspace]", Query.JCR_SQL2);
        QueryResult qResults = query.execute();

        // Process all modules
        query = session.getWorkspace().getQueryManager().createQuery("select * from [pant:module]", Query.JCR_SQL2);
        qResults = query.execute();

        RowIterator rows = qResults.getRows();
        int upgradedCount = 0;
        while (rows.hasNext()) {
            Row row = rows.nextRow();
            Node moduleNode = row.getNode();
            refactorModuleNode(moduleNode);
            upgradedCount++;
        }

        log.info("Upgraded " + upgradedCount + " modules");
    }

    private void refactorModuleNode(Node moduleNode) throws RepositoryException {

        NodeIterator localeNodes = moduleNode.getNodes();
        while (localeNodes.hasNext()) {
            Node localeNode = localeNodes.nextNode();

            // Get the nodes for the draft and released versions
            Node draftVersionNode;
            Node releasedVersionNode;
            if(localeNode.hasProperty("draft")) {
                Property draftRef = localeNode.getProperty("draft");
                draftVersionNode = moduleNode.getSession().getNodeByIdentifier(draftRef.getString());
            } else {
                draftVersionNode = null;
            }
            if(localeNode.hasProperty("released")) {
                Property releasedRef = localeNode.getProperty("released");
                releasedVersionNode = moduleNode.getSession().getNodeByIdentifier(releasedRef.getString());
            } else {
                releasedVersionNode = null;
            }

            // create default source and variants folders
            Node sourceNode = localeNode.addNode("source", "sling:Folder");
            Node variantsNode = localeNode.addNode("variants", "sling:OrderedFolder");

            // create the default variant
            Node defaultVariantNode = variantsNode.addNode("DEFAULT", "pant:moduleVariant");

            if(draftVersionNode != null) {
                // Get the source content
                String sourceContent = draftVersionNode.getNode("content")
                        .getNode("asciidoc")
                        .getNode("jcr:content")
                        .getProperty("jcr:data")
                        .getString();

                // add a draft source node
                Node draftSourceNode = sourceNode.addNode("draft", "nt:file");
                draftSourceNode.addMixin("pant:hashable");

                // add a draft variant node
                defaultVariantNode.addNode("draft", "pant:moduleVersion");

                // copy over the content
                Node jcrContentNode = draftSourceNode.addNode("jcr:content", "nt:resource");
                jcrContentNode.setProperty("jcr:mimeType", "text/x-asciidoc");
                jcrContentNode.setProperty("jcr:data", sourceContent);

                // move over the ackStatus node to ack_status
                moduleNode.getSession().move(
                        draftVersionNode.getPath() + "/ackStatus",
                        defaultVariantNode.getPath() + "/draft/ack_status");
            }

            if(releasedVersionNode != null) {
                // Get the source content
                String sourceContent = releasedVersionNode.getNode("content")
                        .getNode("asciidoc")
                        .getNode("jcr:content")
                        .getProperty("jcr:data")
                        .getString();

                // add a released source node
                Node releasedSourceNode = sourceNode.addNode("released", "nt:file");
                releasedSourceNode.addMixin("pant:hashable");

                // add a released variant node
                defaultVariantNode.addNode("released", "pant:moduleVersion");

                // copy over the content
                Node jcrContentNode = releasedSourceNode.addNode("jcr:content", "nt:resource");
                jcrContentNode.setProperty("jcr:mimeType", "text/x-asciidoc");
                jcrContentNode.setProperty("jcr:data", sourceContent);

                // move over the ackStatus node to ack_status
                moduleNode.getSession().move(
                        releasedVersionNode.getPath() + "/ackStatus",
                        defaultVariantNode.getPath() + "/released/ack_status");
            }

            // cleanup nodes and properties which are no longer needed
            // 1. Remove the draft and released properties on the module locale
            if(localeNode.hasProperty("draft")) {
                localeNode.getProperty("draft").remove();
            }
            if(localeNode.hasProperty("released")) {
                localeNode.getProperty("released").remove();
            }

            // 2. Clean up all children of the module locale which are not called 'source' or 'variants'
            NodeIterator versionNodes = localeNode.getNodes();
            while(versionNodes.hasNext()) {
                Node versionNode = versionNodes.nextNode();
                if(!versionNode.getName().equals("source") && !versionNode.getName().equals("variants")) {
                    versionNode.remove();
                }
            }
        }
    }
}
