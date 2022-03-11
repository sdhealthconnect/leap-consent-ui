package gov.hhs.onc.leap.medrec.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.hhs.onc.leap.medrec.model.MedicationSummaryList;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
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

public class MedicationSummaryClient {
    private final static Logger LOGGER = Logger.getLogger(MedicationSummaryClient.class.getName());
    private final String host;
    private final String endpoint = "/medicationsummary/";

    private static final Header MED_SUMMARY_HEADER_CONTENT = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
    private static final Header MED_SUMMARY_HEADER_ACCEPTS = new BasicHeader(HttpHeaders.ACCEPT, "application/json");

    public MedicationSummaryClient(String host) {
        this.host = host;
    }

    public MedicationSummaryList getMedicationSummary(String fhirPatientId) {
        MedicationSummaryList results = null;
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultHeaders(getDefaultHeaders()).build();
        try {
            List<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("subject", fhirPatientId));

            URIBuilder uriBuilder = new URIBuilder(host + endpoint);
            uriBuilder.addParameters(postParameters);

            HttpGet getRequest = new HttpGet(uriBuilder.build());

            HttpResponse response = httpClient.execute(getRequest);

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

            String output;
            StringBuffer sb = new StringBuffer();

            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            ObjectMapper mapper = new ObjectMapper();
            results = mapper.readValue(sb.toString(), MedicationSummaryList.class);

            LOGGER.log(Level.INFO, String.format("SLS Response: ", sb.toString()));
        }
        catch (Exception ex) {
            LOGGER.warning("Failed Medication Summary Get "+ex.getMessage());
        }
        return results;
    }

    private List<Header> getDefaultHeaders() {
        List<Header> defaultHeaders = new ArrayList<Header>();
        defaultHeaders.add(MED_SUMMARY_HEADER_CONTENT);
        defaultHeaders.add(MED_SUMMARY_HEADER_ACCEPTS);
        return defaultHeaders;
    }
}
