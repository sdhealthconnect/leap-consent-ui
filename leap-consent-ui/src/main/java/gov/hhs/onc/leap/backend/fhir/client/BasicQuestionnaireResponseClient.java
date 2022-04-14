package gov.hhs.onc.leap.backend.fhir.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.hhs.onc.leap.medrec.client.MedicationSummaryClient;
import gov.hhs.onc.leap.medrec.model.MedicationSummaryList;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BasicQuestionnaireResponseClient {
    private final static Logger LOGGER = Logger.getLogger(BasicQuestionnaireResponseClient.class.getName());
    private String url;
    private static final Header MED_SUMMARY_HEADER_CONTENT = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
    private static final Header MED_SUMMARY_HEADER_ACCEPTS = new BasicHeader(HttpHeaders.ACCEPT, "application/json");

    public BasicQuestionnaireResponseClient(String url) {
        this.url = url;
    }

    public QuestionnaireResponse getQuestionnaireResponse() {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultHeaders(getDefaultHeaders()).build();
        QuestionnaireResponse results = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);

            HttpGet getRequest = new HttpGet(uriBuilder.build());

            HttpResponse response = httpClient.execute(getRequest);

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

            String output;
            StringBuffer sb = new StringBuffer();

            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();
            parser.setPrettyPrint(true);
            results = parser.parseResource(QuestionnaireResponse.class, sb.toString());
        }
        catch(Exception ex) {
            LOGGER.warning("ERROR in QuestionnaireResponse Request: "+ex.getMessage());
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
