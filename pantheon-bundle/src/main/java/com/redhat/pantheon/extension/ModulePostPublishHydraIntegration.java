package com.redhat.pantheon.extension;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;

import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
import com.redhat.pantheon.model.module.Module;

/**
 * A Hydra message producer for Module post publish events.
 * 
 */
public class ModulePostPublishHydraIntegration implements EventProcessingExtension {
	// Environment variables.
	private static String message_broker_hostname = "";
	private static String message_broker_port = "";
	private static String message_broker_scheme = "";
	private static String message_broker_username = "";
	private static String message_broker_user_pass = "";
	private static String pantheon_host = "";
	
	private static final String PANTHEON_MODULE_API_PATH = "/api/module?locale=en-us&module_id=";
	private static final String TLS_VERSION = "TLSv1.2";
	private static final String UUID_FIELD = "jcr:uuid";
	private static final String HYDRA_TOPIC = "VirtualTopic.eng.pantheon2.notifications";
	private SSLContext sslContext;
	private ResourceResolverFactory resolverFactory;

	public boolean canProcessEvent(Event event) {
		//TODO: return true if resource is published
		return true;
	}
	
	public void processEvent(Event event) throws Exception {
        
		ModuleVersionPublishedEvent publishedEvent = (ModuleVersionPublishedEvent) event;
		if (canProcessEvent(publishedEvent)) {
    		Resource resource = null;
    		Module module = null;
    
    		// Get resourceResolver
    		Map<String, Object> serviceParams = new HashMap<String, Object>();
    
            serviceParams.put(ResourceResolverFactory.SUBSERVICE, "pantheon");
    
            try {
            
            	// get resource resolver for the system user set in user mapper
                ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);

                // get resource from path
                resource = resourceResolver.getResource(null, ResourceUtil.getParent(publishedEvent.getModuleVersionPath(), 2));
                module = resource.adaptTo(Module.class);
            } catch (Exception e) {
            	System.out.println(e.getMessage());
            }
    		
    		byte[] byteArray = Base64.decodeBase64(this.getMesasgeBrokerUserPass().getBytes());
    		String decodedPass = new String(byteArray);
    		sslContext = SSLContext.getInstance(TLS_VERSION);
    		
    		sslContext.init(null, null, new java.security.SecureRandom());
    		 
            StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
            factory.setBrokerURI(this.getMessageBrokerScheme() + "://" + this.getPanthoenHost() +":" + this.getMessageBrokerPort());
    
            factory.setUsername(this.getMesasgeBrokerUsername());
            factory.setPassword(decodedPass);
            factory.setSslContext(sslContext);
            factory.setForceAsyncSend(true);
            factory.setDisconnectTimeout(5000);
            
            Connection connection = factory.createConnection();
            try {
            	
    			connection.start(); 
    			  
    		    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    		    MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));
    		    String moduleUUID = module.getValueMap().get(UUID_FIELD, String.class);
    		    String msg = "{\"id\": " + this.getPanthoenHost() + PANTHEON_MODULE_API_PATH + moduleUUID +"}";
    			producer.send(session.createTextMessage(msg));
     
    		} catch (Throwable t) {
    			t.printStackTrace();
    		} finally {
    			connection.close();
    		}
    	}
	}
	
	public String getMessageBrokerHostname () {
		if (System.getenv("HYDRA_HOST") != null){
            message_broker_hostname = System.getenv("HYDRA_HOST");
        } else {
        	message_broker_hostname = "HYDRA_HOST is not set.";
            System.out.println("HYDRA_HOST environment variable is not set");
        }
		
		return message_broker_hostname;
	}
	
	public String getMessageBrokerPort () {
		if (System.getenv("HYDRA_PORT") != null) {
			message_broker_port = System.getenv("HYDRA_PORT");
		} else {
			message_broker_port = "HYDRA_PORT is not set.";
            System.out.println("HYDRA_PORT environment variable is not set");
		}
		
		return message_broker_port;
	}
	
	public String getMessageBrokerScheme() {
		if (System.getenv("HYDRA_SCHEME") != null) {
			message_broker_scheme = System.getenv("HYDRA_SCHEME");
		} else {
			message_broker_scheme = "HYDRA_SCHEME is not set.";
            System.out.println("HYDRA_SCHEME environment variable is not set");
		}
		
		return message_broker_scheme;
	}
	
	public String getMesasgeBrokerUsername() {
		if (System.getenv("HYDRA_USER") != null) {
			message_broker_username = System.getenv("HYDRA_USER");
		} else {
			message_broker_username = "HYDRA_USER is not set.";
            System.out.println("HYDRA_USER environment variable is not set");
		}

		return message_broker_username;
	}
	
	public String getMesasgeBrokerUserPass() {
		if (System.getenv("HYDRA_USER_PASS") != null) {
			message_broker_username = System.getenv("HYDRA_USER_PASS");
		} else {
			message_broker_username = "HYDRA_USER_PASS is not set.";
            System.out.println("HYDRA_USER_PASS environment variable is not set");
		}

		return message_broker_user_pass;
	}
	
	public String getPanthoenHost() {
		if (System.getenv("PANTHEON_USER") != null) {
			pantheon_host = System.getenv("PANTHEON_USER");
		} else {
			pantheon_host = "PANTHEON_USER is not set.";
            System.out.println("PANTHEON_USER environment variable is not set");
		}

		return pantheon_host;
	}
}
