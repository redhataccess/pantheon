package com.redhat.pantheon.extension;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.osgi.service.component.annotations.Component;

import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
import com.redhat.pantheon.model.module.Module;

/**
 * A Hydra message producer for Module post publish events.
 * It makes sure that the events are processed, and any possible errors
 * are reported. It will call on all registered OSGI components under the {@link EventProcessingExtension}
 * service interface and will report if there is a failure. If any extension fails its processing,
 * no retries will be attempted. It is up to the extension developer to make sure their extensions are
 * working properly.
 */
@Component(
        service = ModuleVersionPublishedEvent.class
)

public class ModulePostPublishHydraIntegration implements EventProcessingExtension {
	//TODO: environment variables.
	private static final String HYDRA_HOST = "hydra-messaging-broker02.web.dev.ext.phx1.redhat.com";
	private static final String HYDRA_PORT = "61612";
	private static final String BROKER_SCHEME = "ssl";
	private static final String HYDRA_TOPIC = "VirtualTopic.eng.pantheon2.notifications";
	private static final String PANTHEON_USER = "pantheon2user";
	private static final String PANTHEON_PASS = "cGFudGhlMG4ydTVlcg==";
	private static final String PANTHEON_HOST = "http://pantheon2-stage.int.us-east.aws.preprod.paas.redhat.com";
	
	private static final String PANTHEON_MODULE_API_PATH = PANTHEON_HOST + "/api/module?locale=en-us&module_id=";
	private static final String TLS_VERSION = "TLSv1.2";
	private static final String UUID_FIELD = "jcr:uuid";
	
	private SSLContext sslContext;
	private ResourceResolverFactory resolverFactory;

	@Override
	public boolean canProcessEvent(Event event) {
		//TODO: return true if resource is published
		return true;
	}
	
	@Override
	public void processEvent(Event event) throws Exception {

		ModuleVersionPublishedEvent publishedEvent = (ModuleVersionPublishedEvent) event;
		Resource resource = null;
		Module module = null;

		// Get resourceResolver
		Map<String, Object> serviceParams = new HashMap<String, Object>();

        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "pantheon");

        try {

        	// get resource resolver for the system user set in user mapper
            ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);
//          Session slingSession = resourceResolver.adaptTo(Session.class);
            // get resource from path
            resource = resourceResolver.getResource(null, ResourceUtil.getParent(publishedEvent.getModuleVersionPath(), 2));
            module = resource.adaptTo(Module.class);
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        } 
		
		byte[] byteArray = Base64.decodeBase64(PANTHEON_PASS.getBytes());
		String decodedPass = new String(byteArray);
		sslContext = SSLContext.getInstance(TLS_VERSION);
		
		sslContext.init(null, null, new java.security.SecureRandom());
		 
        StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        factory.setBrokerURI(BROKER_SCHEME + "://" + HYDRA_HOST +":" + HYDRA_PORT);

        factory.setUsername(PANTHEON_USER);
        factory.setPassword(decodedPass);
        factory.setSslContext(sslContext);
        factory.setForceAsyncSend(true);
        factory.setDisconnectTimeout(5000);
        
        Connection connection = factory.createConnection();
        try {
			connection.setClientID(PANTHEON_USER);
			 try {
				 connection.start();	 
			 } catch (JMSException ex) {
				 System.out.println("Exception: " + ex);
			 }
		     
		     Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		     MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));
		     String moduleUUID = module.getValueMap().get(UUID_FIELD, String.class);
		     String msg = "{\"id\": " + PANTHEON_MODULE_API_PATH + moduleUUID +"}";
			 producer.send(session.createTextMessage(msg));
			 
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			connection.close();
		}
	}    
}
