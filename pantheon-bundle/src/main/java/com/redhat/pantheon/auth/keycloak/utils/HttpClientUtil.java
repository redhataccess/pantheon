package com.redhat.pantheon.auth.keycloak.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {

    /**
     * Send an http post request, the parameters are submitted in the form of a form form key-value pair.
     */
    public static HttpResponse httpPostForm(String url,Map<String,String> params, Map<String,String> headers,String encode){
        HttpResponse response = new HttpResponse();
        if(encode == null){
            encode = "utf-8";
        }
        CloseableHttpClient closeableHttpClient =
                HttpClients.custom().setConnectionManager(createConnManager()).build();
        HttpPost httpost = new HttpPost(url);

        // Set the header
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpost.setHeader(entry.getKey(),entry.getValue());
            }
        }
        // Organization request parameters
        List<NameValuePair> paramList = new ArrayList <NameValuePair>();
        if(params != null && params.size() > 0){
            Set<String> keySet = params.keySet();
            for(String key : keySet) {
                paramList.add(new BasicNameValuePair(key, params.get(key)));
            }
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(paramList, encode));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String content = null;
        CloseableHttpResponse  httpResponse = null;
        try {
            httpResponse = closeableHttpClient.execute(httpost);
            HttpEntity entity = httpResponse.getEntity();
            content = EntityUtils.toString(entity, encode);
            response.setBody(content);
            response.setHeaders(httpResponse.getAllHeaders());
            response.setReasonPhrase(httpResponse.getStatusLine().getReasonPhrase());
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try { //Close the connection, release the resource
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    private static PoolingHttpClientConnectionManager createConnManager(){

        SSLContext sslcontext = null;
        try {
            sslcontext = createIgnoreVerifySSL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext,NoopHostnameVerifier.INSTANCE))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        return connManager;
    }

    private static SSLContext createIgnoreVerifySSL()
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3"); // equivalent to TLS
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new X509TrustManager[] { trustManager }, null);
        return sc;
    }

}
