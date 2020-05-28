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
public class IntroduceModuleVariantRepositories implements RepositoryUpgrade {

    private static final Logger log = LoggerFactory.getLogger(IntroduceModuleVariantRepositories.class);

    @Override
    public String getId() {
        return "introduce-module-variant-repositories";
    }

    @Override
    public String getDescription() {
        return "Refactors repositories for module variants";
    }

    @Override
    public void executeUpgrade(ResourceResolver resourceResolver) throws Exception {

        Session session = resourceResolver.adaptTo(Session.class);

        // Process all workspaces
        Query query = session.getWorkspace().getQueryManager().createQuery("select * from [pant:workspace]", Query.JCR_SQL2);
        QueryResult qResults = query.execute();

        RowIterator rows = qResults.getRows();
        int upgradedCount = 0;
        while (rows.hasNext()) {
            Row row = rows.nextRow();
            Node workspaceNode = row.getNode();
            refactorWorkspaceNode(workspaceNode);
            upgradedCount++;
        }

        log.info("Upgraded " + upgradedCount + " workspaces");
    }

    private void refactorWorkspaceNode(Node workspaceNode) throws RepositoryException {
        log.debug("Refactoring workspace: " + workspaceNode.getName());
        Node variantsNode = workspaceNode.addNode("module_variants", "sling:OrderedFolder");
        Node defaultVariantNode = variantsNode.addNode("DEFAULT");
        defaultVariantNode.setProperty("pant:canonical", true);

        if(workspaceNode.hasProperty("pant:attributeFile")) {
            Property oldAttFileProperty = workspaceNode.getProperty("pant:attributeFile");
            defaultVariantNode.setProperty("pant:attributesFilePath", oldAttFileProperty.getString());

            oldAttFileProperty.remove();
        }

        // move all other children (aka folders and files) to the entities folder
        Node entitiesNode = workspaceNode.addNode("entities", "sling:Folder");
        NodeIterator workspaceChildIterator = workspaceNode.getNodes();
        while(workspaceChildIterator.hasNext()) {
            Node workspaceChildNode = workspaceChildIterator.nextNode();

            if(!workspaceChildNode.getName().equals("entities")
                    && !workspaceChildNode.getName().equals("module_variants")) {
                workspaceNode.getSession().move(
                        workspaceChildNode.getPath(),
                        entitiesNode.getPath() + "/" + workspaceChildNode.getName());
            }
        }
    }
}
