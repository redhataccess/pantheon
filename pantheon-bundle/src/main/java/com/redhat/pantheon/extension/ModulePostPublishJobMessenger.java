package com.redhat.pantheon.extension;

import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;

import static com.redhat.pantheon.extension.Events.MODULE_POST_PUBLISH_EVENT;

/**
 * A messenger for Module post publish events.
 * This messenger sends a light message to hydra broker for search integration. It will call on all registered
 *  OSGI components under the {@link ModulePostPublishExtension}
 * service interface
 */
@Component(
        service = JobMessenger.class,
        property = JobMessenger.PROPERTY_TOPICS + "=" + MODULE_POST_PUBLISH_EVENT
)
public class ModulePostPublishJobMessenger extends EventJobConsumer<ModulePostPublishExtension> {
	private static final HYDRA_URL = "hydra-messaging-broker02.web.dev.ext.phx1.redhat.com";
	private static final HYDRA_PORT = "61612";
	private static final HYDRA_TOPIC = "/topic/VirtualTopic.eng.pantheon2.notifications";
	private static final PANTHEON_USER = "pantheon2user";
	private static final PANTHEON_PASS = "cGFudGhlMG4ydTVlcg==";
	
	public ModulePostPublishJobMessenger() {
        super(ModulePostPublishExtension.class);
    }

	public Object send( string msg) throws Exception {

		byte[] byteArray = Base64.decodeBase64(PANTHEON_PASS.getBytes());
		String decodedPass = new String(byteArray);
		
		// Get module UUID from ModuleVersion
		StompConnection connection = new StompConnection();
		try {
			connection.open(HYDRA_URL, HYDRA_PORT);
			connection.connect(PANTHEON_USER,decodedPass, use_ssl=true, wait=false);
			
			StompFrame connect = connection.receive();
			if (!connect.getAction().equals(Stomp.Responses.CONNECTED)) {
			    throw new Exception ("Not connected");
			}
			
			connection.begin("tx1");
			connection.send( HYDRA_TOPIC, msg, "tx1", null);
			connection.commit("tx1");
			
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			connection.close();
		}		
		return null;
	}
 
}
