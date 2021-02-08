package gov.hhs.onc.leap.backend.fhir.client.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class HapiFhirCreateException extends RuntimeException {
    private static final String MESSAGE = "Could not create hapi value set with oid:  %s";

    public HapiFhirCreateException(String measureId) {
        super(String.format(MESSAGE, measureId));
        log.warn(getMessage());
    }
}
