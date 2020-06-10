package com.redhat.pantheon.extension;

import com.google.common.collect.Maps;
import com.ibm.icu.util.ULocale;
import com.redhat.pantheon.extension.events.ModuleVersionPublishStateEvent;
import com.redhat.pantheon.extension.events.ModuleVersionPublishedEvent;
import com.redhat.pantheon.extension.events.ModuleVersionUnpublishedEvent;
import com.redhat.pantheon.model.api.SlingModels;
import com.redhat.pantheon.model.module.ModuleVariant;
import com.redhat.pantheon.model.module.ModuleVersion;
import com.redhat.pantheon.servlet.ServletUtils;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.SslContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringSubstitutor;
import org.apache.jackrabbit.JcrConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;

import static com.redhat.pantheon.model.module.ModuleVariant.DEFAULT_VARIANT_NAME;
import static com.redhat.pantheon.servlet.ServletUtils.toLanguageTag;

/**
 * A Hydra message producer for Module post publish events.
 * 
 * A sample message for publish event:
 * {"id":"https://example.com/api/module?locale=en-us&module_id=fb8f7586-1b68-437c-94d9-bfc4f85866ed&variant=DEFAULT","event":"publish"}
 *
 * A sample message for unpublish event:
 * {"id":"","event":"unpublish", "view_uri":"https://example.com/topics/en-us/fb8f7586-1b68-437c-94d9-bfc4f85866ed?variant=DEFAULT"}
 *
 * @author Lisa Davidson
 */
@Component(
        service = EventProcessingExtension.class
        )
public class HydraIntegration implements EventProcessingExtension {
    // Environment variables.
    private static String messageBrokerUrl = "";
    private static String messageBrokerUsername = "";
    private static String messageBrokerUserPass = "";
    private static String pantheonHost = "";

    //@TODO: externalize the variables
    private static final String PANTHEON_MODULE_VERSION_API_PATH = "/api/module/variant.json/${variantUuid}";
    private static final String TLS_VERSION = "TLSv1.2";
    private static final String UUID_FIELD = "jcr:uuid";
    private static final String HYDRA_TOPIC = "VirtualTopic.eng.pantheon2.notifications";
    private static final String ID_KEY = "id";
    private static final String EVENT_KEY = "event";
    private static final String URI_KEY = "view_uri";
    private static final String EVENT_PUBLISH_VALUE = "publish";
    private static final String EVENT_UNPUBLISH_VALUE = "unpublish";
    public static final Locale DEFAULT_MODULE_LOCALE = Locale.US;
    public static final String PORTAL_URL = "PORTAL_URL";

    private ServiceResourceResolverProvider serviceResourceResolverProvider;
    private final Logger log = LoggerFactory.getLogger(HydraIntegration.class);


    @Activate
    public HydraIntegration(
            @Reference ServiceResourceResolverProvider serviceResourceResolverProvider) {
        this.serviceResourceResolverProvider = serviceResourceResolverProvider;
    }

    /**
     * Validate if we can processEvent.
     */
    public boolean canProcessEvent(Event event) {
        // Stop processEvent if broker properties are missing
        if (System.getenv("MESSAGE_BROKER_URL") == null 
                || System.getenv("HYDRA_USER") == null 
                || System.getenv("HYDRA_USER_PASS") == null 
                || System.getenv("PANTHEON_HOST") == null){
            return false;
        }

        return ModuleVersionPublishStateEvent.class.isAssignableFrom(event.getClass());
    }

    /**
     * Process ModuleVersionPublishedEvent. It sends a simple text message to the Message Broker.
     *
     */
    public void processEvent(Event event) throws Exception {
        ModuleVersionPublishStateEvent publishedEvent = (ModuleVersionPublishStateEvent) event;
        ModuleVersion moduleVersion = SlingModels.getModel(serviceResourceResolverProvider.getServiceResourceResolver(),
                publishedEvent.getModuleVersionPath(), ModuleVersion.class);

        Connection connection = createConnectionFactory().createConnection();
        try {
            connection.start();
            log.info("[" + HydraIntegration.class.getSimpleName() + "] connection started " );
        } catch (JMSException ex) {
            log.info("Exception: " + ex);
        }

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(session.createTopic(HYDRA_TOPIC));

        String eventValue = ModuleVersionPublishedEvent.class.equals(event.getClass()) ? EVENT_PUBLISH_VALUE : EVENT_UNPUBLISH_VALUE;
        String idValue = ModuleVersionPublishedEvent.class.equals(event.getClass()) ? buildModuleVersionUri(moduleVersion) : "";
        String uriValue = "";
        String msg = "";

        if (ModuleVersionPublishedEvent.class.equals(event.getClass())) {
            // TODO Use a json generation api for this
            msg = "{\""
                    + ID_KEY + "\":" + "\"" + idValue +"\","
                    + "\"" + EVENT_KEY + "\":" + "\"" + eventValue + "\"}";
        } else if (ModuleVersionUnpublishedEvent.class.equals(event.getClass())){
            if (System.getenv(PORTAL_URL) != null) {
                uriValue = getPortalUri(moduleVersion);

                // TODO Use a json generation api for this
                msg = "{\""
                    + ID_KEY + "\":" + "\"" + idValue +"\","
                    + "\"" + EVENT_KEY + "\":" + "\"" + eventValue + "\","
                    + "\"" + URI_KEY + "\":" + "\"" + uriValue + "\"}";
            }
        } else {
            log.warn("[" + HydraIntegration.class.getSimpleName() + "] unhandled event type: " + event.getClass());
        }
        if (!msg.isEmpty()) {
            producer.send(session.createTextMessage(msg));
            log.info("[" + HydraIntegration.class.getSimpleName() + "] message sent: " + session.createTextMessage(msg) );
        } else {
            log.info("[" + HydraIntegration.class.getSimpleName() + "] empty message!");
        }

        connection.close();
    }

    /**
     * Broker user can be set as an environment variable
     * @return message_broker_username
     */
    public String getMesasgeBrokerUsername() {
        if (System.getenv("HYDRA_USER") != null) {
            messageBrokerUsername = System.getenv("HYDRA_USER");
        } else {
            log.info("HYDRA_USER environment variable is not set");
        }

        return messageBrokerUsername;
    }

    /**
     * Broker user pass can be set as an environment variable
     * @return message_broker_user_pass
     */
    public String getMesasgeBrokerUserPass() {
        if (System.getenv("HYDRA_USER_PASS") != null) {
            messageBrokerUserPass = System.getenv("HYDRA_USER_PASS");
        } else {
            log.info("HYDRA_USER_PASS environment variable is not set");
        }

        return messageBrokerUserPass;
    }

    /**
     * Pantheon host can be set as an environment variable
     * @return pantheon_host
     */
    public String getPantheonHost() {
        if (System.getenv("PANTHEON_HOST") != null) {
            pantheonHost = System.getenv("PANTHEON_HOST");
        } else {
            log.info("PANTHEON_HOST environment variable is not set");
        }

        return pantheonHost;
    }

    /**
     * Broker hostname can be set as an environment variable
     * @return message_broker_hostname.
     */
    public String getMessageBrokerUrl () {
        if (System.getenv("MESSAGE_BROKER_URL") != null){
            messageBrokerUrl = System.getenv("MESSAGE_BROKER_URL");
        } else {
            log.info("MESSAGE_BROKER_URL environment variable is not set");
        }

        return messageBrokerUrl;
    }

    /**
     * Set up Remote connection properties for STOMP protocol with ssl support
     *
     * @return StompJmsConnectionFactory
     * @throws Exception
     */
    private ConnectionFactory createConnectionFactory() throws Exception {
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

        SslContext sContext = new SslContext(new KeyManager[0], trustAllCerts, new java.security.SecureRandom());
        SslContext.setCurrentSslContext(sContext);
        ActiveMQSslConnectionFactory factory = new ActiveMQSslConnectionFactory();

        factory.setBrokerURL(this.getMessageBrokerUrl());
        factory.setUserName(this.getMesasgeBrokerUsername());
        factory.setPassword(decodedPass);

        factory.setUseAsyncSend(true);
        factory.setConnectResponseTimeout(5000);

        return factory;
    }

    private String buildModuleVersionUri(ModuleVersion moduleVersion) {
        HashMap values = Maps.newHashMap();
        values.put("variantUuid", moduleVersion.getParent().getValueMap().containsKey(JcrConstants.JCR_UUID) ?
                moduleVersion.getParent().getValueMap().get(JcrConstants.JCR_UUID) : "");
        StringSubstitutor strSubs = new StringSubstitutor(values);

        String replacedUri = strSubs.replace(PANTHEON_MODULE_VERSION_API_PATH);
        return this.getPantheonHost() + replacedUri;
    }


    private String getPortalUri(ModuleVersion moduleVersion) {
        final String uriTemplate = System.getenv(PORTAL_URL) + "/topics/${localeId}/${variantUuid}";

        HashMap values = Maps.newHashMap();
        // TODO Clean this up, lots of locale transformations to make sure this aligns
        values.put("localeId", toLanguageTag(
                ULocale.createCanonical(
                        moduleVersion.getParent().getParent().getParent().getName())
                        .toLocale()));
        values.put("variantUuid", moduleVersion.getParent().getValueMap().containsKey(JcrConstants.JCR_UUID) ?
                moduleVersion.getParent().getValueMap().get(JcrConstants.JCR_UUID) : "");
        StringSubstitutor strSubs = new StringSubstitutor(values);

        return strSubs.replace(uriTemplate);
    }
}
