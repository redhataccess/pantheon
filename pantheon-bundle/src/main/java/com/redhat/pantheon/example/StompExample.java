package com.redhat.pantheon.example;

import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;


public class StompExample {

	// TODO: environment variables
	private static final String HYDRA_URL = "hydra-messaging-broker02.web.dev.ext.phx1.redhat.com";
	private static final String HYDRA_PORT = "61612";
	private static final String HYDRA_TOPIC = "VirtualTopic.eng.pantheon2.notifications";
	private static final String PANTHEON_USER = "pantheon2user";
	private static final String PANTHEON_PASS = "cGFudGhlMG4ydTVlcg==";
	
	private static final String TLS_VERSION = "TLSv1.2";
	private static final Destination Destination = null;
	private static SSLContext sslContext;
	
	protected static ConnectionFactory createConnectionFactory() throws Exception {
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

	public static void main(final String[] args) throws Exception {
		
		 Connection connection = createConnectionFactory().createConnection();
		 connection.setClientID("pantheon2user");
		 
		 try {
			 connection.start();
		 } catch (JMSException ex) {
			 System.out.println("Exception: " + ex);
		 }
	     
	     Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	     MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));

	     producer.send(session.createTextMessage("{\"id\":\"http://pantheon2-stage.int.us-east.aws.preprod.paas.redhat.com/api/module?locale=en-us&module_id=ebc84786-3b10-4152-8415-cc824a7e69f5\"}"));
	     
	  
	     connection.close();
	   }
}
