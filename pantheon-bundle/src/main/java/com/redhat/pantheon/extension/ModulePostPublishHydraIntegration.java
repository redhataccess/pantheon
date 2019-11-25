package com.redhat.pantheon.extension;

import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
import com.redhat.pantheon.model.module.Module;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;

/**
 * A Hydra message producer for Module post publish events.
 * 
 * @author Lisa Davidson
 */
@Component(
        service = EventProcessingExtension.class
        )
public class ModulePostPublishHydraIntegration implements EventProcessingExtension {
    // Environment variables.
    private static String message_broker_hostname = "";
    private static String message_broker_port = "";
    private static String message_broker_scheme = "";
    private static String message_broker_username = "";
    private static String message_broker_user_pass = "";
    private static String pantheon_host = "";

    //@TODO: externalize the variables
    private static final String PANTHEON_MODULE_API_PATH = "/api/module?locale=en-us&module_id=";
    private static final String TLS_VERSION = "TLSv1.2";
    private static final String UUID_FIELD = "jcr:uuid";
    private static final String HYDRA_TOPIC = "VirtualTopic.eng.pantheon2.notifications";
    public static final Locale DEFAULT_MODULE_LOCALE = Locale.US;

    private SSLContext sslContext;
    private ServiceResourceResolverProvider serviceResourceResolverProvider;
    private final Logger log = LoggerFactory.getLogger(ModulePostPublishHydraIntegration.class);


    @Activate
    public ModulePostPublishHydraIntegration(
            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    /**
     * Validate if we can processEvent.
     */
    public boolean canProcessEvent(Event event) {
        // Stop processEvent if broker properties are missing
        if (System.getenv("HYDRA_HOST") == null || System.getenv("HYDRA_PORT") == null || System.getenv("HYDRA_SCHEME") == null
                || System.getenv("HYDRA_USER") == null || System.getenv("HYDRA_USER_PASS") == null || System.getenv("PANTHEON_HOST") == null){
            return false;
        }

        return ModuleVersionPublishedEvent.class.equals(event.getClass());
    }

    /**
     * Process ModuleVersionPublishedEvent. It sends a simple text message to the Message Broker.
     *
     */
    public void processEvent(Event event) throws Exception {
        ModuleVersionPublishedEvent publishedEvent = (ModuleVersionPublishedEvent) event;
        Resource resource = null;
        Module module = null;

        // Get resource from path
        resource = serviceResourceResolverProvider.getServiceResourceResolver().getResource(ResourceUtil.getParent(publishedEvent.getModuleVersionPath(), 2));
        module = resource.adaptTo(Module.class);

        Connection connection = createConnectionFactory().createConnection();
        try {
            connection.start();
            log.info("[" + ModulePostPublishHydraIntegration.class.getSimpleName() + "] connection started " );
        } catch (JMSException ex) {
            log.info("Exception: " + ex);
        }

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));
        String moduleUUID = module.getValueMap().get(UUID_FIELD, String.class);
        String msg = "{\"id\": " + "\"" + this.getPantheonHost() + PANTHEON_MODULE_API_PATH + moduleUUID +"\"}";
        producer.send(session.createTextMessage(msg));
        log.info("[" + ModulePostPublishHydraIntegration.class.getSimpleName() + "] message sent: " + session.createTextMessage(msg) );

        connection.close();
    }

    /**
     * Broker hostname can be set as an environment variable
     * @return message_broker_hostname.
     */
    public String getMessageBrokerHostname () {
        if (System.getenv("HYDRA_HOST") != null){
            message_broker_hostname = System.getenv("HYDRA_HOST");
        } else {
            log.info("HYDRA_HOST environment variable is not set");
        }

        return message_broker_hostname;
    }

    /**
     * Broker port can be set as an environment variable
     * @return message_broker_port. Default: '61612'
     */
    public String getMessageBrokerPort () {
        if (System.getenv("HYDRA_PORT") != null) {
            message_broker_port = System.getenv("HYDRA_PORT");
        } else {
            log.info("HYDRA_PORT environment variable is not set");
        }

        return message_broker_port;
    }

    /**
     * Broker scheme can be set as an environment variable
     * @return message_broker_scheme. Default: 'ssl'
     */
    public String getMessageBrokerScheme() {
        if (System.getenv("HYDRA_SCHEME") != null) {
            message_broker_scheme = System.getenv("HYDRA_SCHEME");
        } else {
            log.info("HYDRA_SCHEME environment variable is not set");
        }

        return message_broker_scheme;
    }

    /**
     * Broker user can be set as an environment variable
     * @return message_broker_username
     */
    public String getMesasgeBrokerUsername() {
        if (System.getenv("HYDRA_USER") != null) {
            message_broker_username = System.getenv("HYDRA_USER");
        } else {
            log.info("HYDRA_USER environment variable is not set");
        }

        return message_broker_username;
    }

    /**
     * Broker user pass can be set as an environment variable
     * @return message_broker_user_pass
     */
    public String getMesasgeBrokerUserPass() {
        if (System.getenv("HYDRA_USER_PASS") != null) {
            message_broker_user_pass = System.getenv("HYDRA_USER_PASS");
        } else {
            log.info("HYDRA_USER_PASS environment variable is not set");
        }

        return message_broker_user_pass;
    }

    /**
     * Pantheon host can be set as an environment variable
     * @return pantheon_host
     */
    public String getPantheonHost() {
        if (System.getenv("PANTHEON_HOST") != null) {
            pantheon_host = System.getenv("PANTHEON_HOST");
        } else {
            log.info("PANTHEON_HOST environment variable is not set");
        }

        return pantheon_host;
    }

    /**
     * Set up Remote connection properties for STOMP protocol with ssl support
     *
     * @return StompJmsConnectionFactory
     * @throws Exception
     */
    private StompJmsConnectionFactory createConnectionFactory() throws Exception {
        byte[] byteArray = Base64.decodeBase64(this.getMesasgeBrokerUserPass().getBytes());
        String decodedPass = new String(byteArray);
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        sslContext = SSLContext.getInstance(TLS_VERSION);
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        factory.setBrokerURI(this.getMessageBrokerScheme() + "://" + this.getMessageBrokerHostname() +":" + this.getMessageBrokerPort());

        factory.setUsername(this.getMesasgeBrokerUsername());
        factory.setPassword(decodedPass);
        factory.setSslContext(sslContext);
        factory.setForceAsyncSend(true);
        factory.setDisconnectTimeout(5000);

        return factory;
    }

}
