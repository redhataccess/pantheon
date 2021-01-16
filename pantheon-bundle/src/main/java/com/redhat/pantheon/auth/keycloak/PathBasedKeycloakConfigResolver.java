package com.redhat.pantheon.auth.keycloak;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;

import java.io.File;
import java.io.InputStream;

public class PathBasedKeycloakConfigResolver implements KeycloakConfigResolver {

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        KeycloakDeployment deployment = new KeycloakDeployment();
            File file = new File( System.getProperty( "karaf.etc" ) + File.separator + "keycloak.json" );
            // load config from the file system
            InputStream is = getClass().getResourceAsStream(file.getPath());
            if (is == null) {
                throw new IllegalStateException("[" + PathBasedKeycloakConfigResolver.class.getSimpleName() +"] Not able to find the config file " + file.getPath());
            }
            deployment = KeycloakDeploymentBuilder.build(is);

        return deployment;
    }
}
