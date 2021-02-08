package gov.hhs.onc.leap.backend.fhir.client;

import gov.hhs.onc.leap.backend.fhir.client.exceptions.HapiResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@Slf4j
public class HapiFhirLinkProcessor {
    private final RestTemplate restTemplate;
    private final HapiFhirServer hapiFhirServer;

    public HapiFhirLinkProcessor(RestTemplate restTemplate, HapiFhirServer hapiFhirServer) {
        this.restTemplate = restTemplate;
        this.hapiFhirServer = hapiFhirServer;
    }

    public Optional<Bundle> fetchBundleByUrl(String url) {
        return fetch(Bundle.class, url);
    }

    private <T extends Resource> Optional<T> fetch(Class<T> resourceClass, String url) {
        log.debug("Fetching HAPI_FHIR {} url: {}", resourceClass.getSimpleName(), url);
        Optional<String> optionalJson = fetchJson(url);

        if (!optionalJson.isPresent()) {
            return Optional.empty();
        }

        T resource = parseJsonToHapiFhirResource(resourceClass, optionalJson.get());

        if (resource == null) {
            return Optional.empty();
        } else {
            return Optional.of(resource);
        }
    }

    private <T extends Resource> T parseJsonToHapiFhirResource(Class<T> resourceClass, String resourceJson) {
        try {
            return hapiFhirServer.parseResource(resourceClass, resourceJson);
        } catch (Exception e) {
            log.debug("Cannot find {} json: {}", resourceClass.getSimpleName(), resourceJson);
            log.error("Cannot find {} parse hapi json: {}", resourceClass.getSimpleName(), e);
            return null;
        }
    }

    private Optional<String> fetchJson(String url) {
        try {
            return fetchRestJson(url);
        } catch (HttpClientErrorException e) {
            return handleRestClientError(url, e);
        } catch (Exception e) {
            throw new HapiResourceNotFoundException(url, e);
        }
    }

    private Optional<String> handleRestClientError(String url, HttpClientErrorException e) {
        log.debug("Cannot find hapiFhir object status: {} url: {}", e.getStatusCode(), url);

        return Optional.empty();
    }

    private Optional<String> fetchRestJson(String url) {
        String json = restTemplate.getForObject(url, String.class);

        if (StringUtils.isEmpty(json)) {
            throw new HapiResourceNotFoundException("Service returned empty json for: ", url);
        }

        return Optional.of(json);
    }
}
