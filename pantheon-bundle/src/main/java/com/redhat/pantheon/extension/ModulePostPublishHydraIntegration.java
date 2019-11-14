//package com.redhat.pantheon.extension;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.jms.Connection;
//import javax.jms.JMSException;
//import javax.jms.MessageProducer;
//import javax.jms.Session;
//import javax.net.ssl.SSLContext;
//
//import org.apache.commons.codec.binary.Base64;
//import org.apache.sling.api.resource.Resource;
//import org.apache.sling.api.resource.ResourceResolver;
//import org.apache.sling.api.resource.ResourceResolverFactory;
//import org.apache.sling.api.resource.ResourceUtil;
//import org.fusesource.stomp.jms.StompJmsConnectionFactory;
//import org.osgi.service.component.annotations.Component;
//
//import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
//import com.redhat.pantheon.model.module.Module;
//
///**
// * A Hydra message producer for Module post publish events.
// * It makes sure that the events are processed, and any possible errors
// * are reported. It will call on all registered OSGI components under the {@link EventProcessingExtension}
// * service interface and will report if there is a failure. If any extension fails its processing,
// * no retries will be attempted. It is up to the extension developer to make sure their extensions are
// * working properly.
// */
//@Component(
//        service = ModuleVersionPublishedEvent.class
//)
//
//public class ModulePostPublishHydraIntegration implements EventProcessingExtension {
//	//TODO: environment variables.
//	private String hydra_host = "";
//	private static final String HYDRA_PORT = "61612";
//	private static final String HYDRA_SCHEME = "ssl";
//	
//	private static final String HYDRA_USER = "pantheon2user";
//	private static final String HYDRA_USER_PASS = "cGFudGhlMG4ydTVlcg==";
//	
//	private static final String PANTHEON_HOST = "http://pantheon2-dev.int.us-east.aws.preprod.paas.redhat.com";
//	
//	private static final String PANTHEON_MODULE_API_PATH = PANTHEON_HOST + "/api/module?locale=en-us&module_id=";
//	private static final String TLS_VERSION = "TLSv1.2";
//	private static final String UUID_FIELD = "jcr:uuid";
//	private static final String HYDRA_TOPIC = "VirtualTopic.eng.pantheon2.notifications";
//	private SSLContext sslContext;
//	private ResourceResolverFactory resolverFactory;
//
//	public boolean canProcessEvent(Event event) {
//		//TODO: return true if resource is published
//		return true;
//	}
//	
//	public void processEvent(Event event) throws Exception {
//		if (System.getenv("HYDRA_HOST") != null){
//            hydra_host = System.getenv("HYDRA_HOST");
//        } else {
//            hydra_host = "HYDRA_HOST is not set, this might not be an OpenShift environment.";
//        }
//		ModuleVersionPublishedEvent publishedEvent = (ModuleVersionPublishedEvent) event;
//		Resource resource = null;
//		Module module = null;
//
//		// Get resourceResolver
//		Map<String, Object> serviceParams = new HashMap<String, Object>();
//
//        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "pantheon");
//
//        try {
//
//        	// get resource resolver for the system user set in user mapper
//            ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);
////          Session slingSession = resourceResolver.adaptTo(Session.class);
//            // get resource from path
//            resource = resourceResolver.getResource(null, ResourceUtil.getParent(publishedEvent.getModuleVersionPath(), 2));
//            module = resource.adaptTo(Module.class);
//        } catch (Exception e) {
//        	System.out.println(e.getMessage());
//        } 
//		
//		byte[] byteArray = Base64.decodeBase64(HYDRA_USER_PASS.getBytes());
//		String decodedPass = new String(byteArray);
//		sslContext = SSLContext.getInstance(TLS_VERSION);
//		
//		sslContext.init(null, null, new java.security.SecureRandom());
//		 
//        StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
//        factory.setBrokerURI(HYDRA_SCHEME + "://" + hydra_host +":" + HYDRA_PORT);
//
//        factory.setUsername(HYDRA_USER);
//        factory.setPassword(decodedPass);
//        factory.setSslContext(sslContext);
//        factory.setForceAsyncSend(true);
//        factory.setDisconnectTimeout(5000);
//        
//        Connection connection = factory.createConnection();
//        try {
//			connection.setClientID(HYDRA_USER);
//			 try {
//				 connection.start();	 
//			 } catch (JMSException ex) {
//				 System.out.println("Exception: " + ex);
//			 }
//		     
//		     Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//		     MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));
//		     String moduleUUID = module.getValueMap().get(UUID_FIELD, String.class);
//		     String msg = "{\"id\": " + PANTHEON_MODULE_API_PATH + moduleUUID +"}";
//			 producer.send(session.createTextMessage(msg));
//			 
//		} catch (Throwable t) {
//			t.printStackTrace();
//		} finally {
//			connection.close();
//		}
//	}
//	
//	 
//}
