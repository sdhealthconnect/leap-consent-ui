package gov.hhs.onc.leap.backend;

import gov.hhs.onc.leap.session.ConsentSession;

public interface ConsentDecorator {
    ConsentSession decorate();
}
