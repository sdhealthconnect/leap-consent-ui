package gov.hhs.onc.leap.backend.fhir.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class HapiResourceNotFoundException extends RuntimeException {
    private static final String ID_MESSAGE = "Could not find hapi %s with id: %s";
    private static final String URL_MESSAGE = "Could not find hapi object with id: %s";

    public HapiResourceNotFoundException(String id, String type) {
        super(String.format(ID_MESSAGE, type, id));
        log.warn(getMessage());
    }

    public HapiResourceNotFoundException(String url, Exception cause) {
        super(String.format(URL_MESSAGE, url), cause);
        log.warn(getMessage());
    }
}
