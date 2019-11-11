package com.redhat.pantheon.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;

//import com.redhat.pantheon.servlet.ModuleJsonServlet;

public class StompExample {
//	private final Logger log = LoggerFactory.getLogger(ModuleJsonServlet.class);
	private static final String HYDRA_URL = "hydra-messaging-broker02.web.dev.ext.phx1.redhat.com";
	private static final String HYDRA_PORT = "61612";
	private static final String HYDRA_TOPIC = "/topic/VirtualTopic.eng.pantheon2.notifications";
	private static final String PANTHEON_USER = "pantheon2user";
	private static final String PANTHEON_PASS = "cGFudGhlMG4ydTVlcg==";
	private static final String TLS_VERSION = "TLSv1.2";
	private static SSLContext sslContext;
	
	protected static ConnectionFactory createConnectionFactory() throws Exception {
		byte[] byteArray = Base64.decodeBase64(PANTHEON_PASS.getBytes());
		String decodedPass = new String(byteArray);
		sslContext = SSLContext.getInstance(TLS_VERSION);
		sslContext.init(null, null, new java.security.SecureRandom());
		 
        StompJmsConnectionFactory result = new StompJmsConnectionFactory();
        result.setBrokerURI("tcp://" + HYDRA_URL +":" + HYDRA_PORT);

        result.setUsername(PANTHEON_USER);
        result.setPassword(decodedPass);
        result.setSslContext(sslContext);
        result.setDisconnectTimeout(5000);
        return result;
    }
	public static void main(final String[] args) throws Exception {
		
		 
		 Connection connection = createConnectionFactory().createConnection();
		 connection.setClientID("pantheon2user");
	     connection.start();
	     Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
	     MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));

	     producer.send(session.createTextMessage("1"));
//	     // Disconnect the durable sub..
////	        connection.close();
	     producer.send(session.createTextMessage("2"));
//		 StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
//////		 factory.setBrokerURI("ssl:"+ HYDRA_URL +":" + HYDRA_PORT);
//		 factory.setBrokerURI("tcp://" + HYDRA_URL +":" + HYDRA_PORT);
//		 factory.setUsername(PANTHEON_USER);
//		 factory.setPassword(decodedPass);
//		 factory.setSslContext(sslContext);
//      
//	     factory.setDisconnectTimeout(5000);
//	      
//	     Connection connection = factory.createConnection();
//	     
////	     connection.start();
//	     Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//	     
////	     Topic topic = session.createTopic(HYDRA_TOPIC);
//	     Queue queue = session.createQueue("send_pantheon2_updates");
//	     MessageProducer producer = session.createProducer(queue);
//	     producer.send(session.createTextMessage("Hello"));
//
////	     System.out.println("Waiting 10 seconds");
////	     Thread.sleep(10000); // increase this and it will fail
////	     System.out.println("waited");
//
//	     MessageConsumer consumer = session.createConsumer(queue);
////
//	     TextMessage message = (TextMessage) consumer.receive(5000);
////
//	     System.out.println("The content of the message is " + message.getText());
////
//	     if (!message.getText().equals("Hello")) {
//	         throw new IllegalStateException("the content of the message was different than expected!");
//	     }

	     connection.close();
	   }
}
