//package com.redhat.pantheon.auth.login;
//
//import org.apache.sling.api.SlingHttpServletRequest;
//import org.apache.sling.api.SlingHttpServletResponse;
//import org.apache.sling.api.servlets.SlingAllMethodsServlet;
//import org.osgi.service.component.annotations.Component;
//
//import javax.servlet.Servlet;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.text.ParseException;
//import java.util.logging.Logger;
//
//@Component(
//        service = Servlet.class,
//        property = {
//                "sling.servlet.methods={GET, POST}",
//                "sling.servlet.paths=/sso/login"
//        },
//        immediate = true)
//public class LoginEndpoint extends SlingAllMethodsServlet {
//    private final Logger log = Logger.getLogger(LoginEndpoint.class.getName());
//
//    @Override
//    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
//
//            try {
//
//                URI uri = createAuthenticationRequest();
//
//                if (uri != null) {
//                    response.setStatus(302);
//                    response.setHeader("Location",uri.toString());
//
//                } else {
//                    log.info("[" + LoginEndpoint.class.getSimpleName() + "] Error occurred while creating the Authentication request.");
//                }
//
//            } catch (URISyntaxException e) {
//                log.info("Error occurred while creating the Authentication request.");
//            } catch (ParseException e) {
//                log.info("Error occurred while creating the Authentication request.");
//            }
//
//    }
//
//    private URI createAuthenticationRequest() throws URISyntaxException, ParseException {
//
//        return null;
//    }
//}
