package com.redhat.pantheon.extension;

import static com.redhat.pantheon.extension.Events.MODULE_POST_PUBLISH_EVENT;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.osgi.service.component.annotations.Component;


/**
 * A messenger for Module post publish events.
 * This messenger sends a light message to Hydra broker for search integration. It will call on all registered
 *  OSGI components under the {@link ModulePostPublishExtension}
 * service interface
 */
@Component(
        service = JobConsumer.class,
        property = JobConsumer.PROPERTY_TOPICS + "=" + MODULE_POST_PUBLISH_EVENT
)
public class ModulePostPublishMessageProducer extends EventJobConsumer<ModulePostPublishExtension> {
	
	//TODO: environment variables.
	private static final String HYDRA_URL = "hydra-messaging-broker02.web.dev.ext.phx1.redhat.com";
	private static final String HYDRA_PORT = "61612";
	private static final String HYDRA_TOPIC = "VirtualTopic.eng.pantheon2.notifications";
	private static final String PANTHEON_USER = "pantheon2user";
	private static final String PANTHEON_PASS = "cGFudGhlMG4ydTVlcg==";
	
	private static final String TLS_VERSION = "TLSv1.2";
	
	private SSLContext sslContext;
	
	protected ModulePostPublishMessageProducer(Class<ModulePostPublishExtension> extensionClass) {
		super(extensionClass);
		// TODO Auto-generated constructor stub
	}
	
	protected ConnectionFactory createConnectionFactory() throws Exception {
		byte[] byteArray = Base64.decodeBase64(PANTHEON_PASS.getBytes());
		String decodedPass = new String(byteArray);
		sslContext = SSLContext.getInstance(TLS_VERSION);
		
		sslContext.init(null, null, new java.security.SecureRandom());
		 
        StompJmsConnectionFactory result = new StompJmsConnectionFactory();
        result.setBrokerURI("ssl://" + HYDRA_URL +":" + HYDRA_PORT);

        result.setUsername(PANTHEON_USER);
        result.setPassword(decodedPass);
        result.setSslContext(sslContext);
        result.setForceAsyncSend(true);
        result.setDisconnectTimeout(5000);
        return result;
    }
	
	public Object send( String msg) throws Exception {
		Connection connection = createConnectionFactory().createConnection();
		
		try {
			connection.setClientID(PANTHEON_USER);
			 try {
				 connection.start();	 
			 } catch (JMSException ex) {
				 System.out.println("Exception: " + ex);
			 }
		     
		     Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		     MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));
			 producer.send(session.createTextMessage(msg));
			 
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			connection.close();
		}		
		return null;
	}
 
}
