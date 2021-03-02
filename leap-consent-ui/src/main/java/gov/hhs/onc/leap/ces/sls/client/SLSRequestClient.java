package gov.hhs.onc.leap.ces.sls.client;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * TODO: Try to move this to a common library to be used from any project
 * @author duanedecouteau
 */
public class SLSRequestClient {
    private final static Logger LOGGER = Logger.getLogger(gov.hhs.onc.leap.ces.sls.client.SLSRequestClient.class.getName());
    private final String host;
    private final String endpoint = "/slsnlp/requestMessageProcessing";

    private static final Header SLS_CLIENT_HEADER_CONTENT = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
    private static final Header SLS_CLIENT_HEADER_ACCEPTS = new BasicHeader(HttpHeaders.ACCEPT, "application/json");

    public SLSRequestClient(String host) {
        this.host = host;
    }

    public String requestLabelingSecured(String id, String origin, String msgSource, String msgVersion, String msg) {
        String result = "";
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultHeaders(getDefaultHeaders()).build();
        try {
            List<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("id", id));
            postParameters.add(new BasicNameValuePair("origin", origin));
            postParameters.add(new BasicNameValuePair("msgSource", msgSource));
            postParameters.add(new BasicNameValuePair("msgVersion", msgVersion));

            URIBuilder uriBuilder = new URIBuilder(host + endpoint);
            uriBuilder.addParameters(postParameters);

            HttpPost postRequest = new HttpPost(uriBuilder.build());

            postRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

            StringEntity ent = new StringEntity(msg, "UTF-8");

            postRequest.setEntity(ent);

            HttpResponse response = httpClient.execute(postRequest);

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

            String output;
            StringBuffer sb = new StringBuffer();

            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            LOGGER.log(Level.INFO, String.format("SLS Response: ", sb.toString()));

            result = sb.toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, String.format("SLS Failure: ", ex.getMessage()));
            ex.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception exClose) {
                LOGGER.log(Level.WARNING, String.format("SLS Client Failed to Close: ", exClose.getMessage()));
            }
        }
        return result;
    }

    private List<Header> getDefaultHeaders() {
        List<Header> defaultHeaders = new ArrayList<Header>();
        defaultHeaders.add(SLS_CLIENT_HEADER_CONTENT);
        defaultHeaders.add(SLS_CLIENT_HEADER_ACCEPTS);
        return defaultHeaders;
    }
}
