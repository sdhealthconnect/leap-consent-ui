package gov.hhs.onc.leap.backend.fhir.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.util.BundleUtil;
import gov.hhs.onc.leap.backend.fhir.client.exceptions.HapiFhirCreateException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class HapiFhirServer {

    @Getter
    FhirContext ctx;

    @Getter
    IGenericClient hapiClient;

    @Value("${hapi-fhir.url:http://34.94.253.50:8080/hapi-fhir-jpaserver/fhir/}")
    private String baseURL;

    @PostConstruct
    public void setUp() {
        ctx = FhirContext.forR4();
        hapiClient = ctx.newRestfulGenericClient(baseURL);
        hapiClient.registerInterceptor(createLoggingInterceptor());

        log.info("Created hapi client for server: {} ", baseURL);
    }


    public <T extends Resource> Optional<T> findResourceInBundle(Bundle bundle, Class<T> clazz) {
        if (bundle.hasEntry()) {
            if (bundle.getEntry().size() > 1) {
                log.error("Hapi-Fhir Resource for {} returned more than one resource count: {}",
                        clazz.getSimpleName(), bundle.getEntry().size());
                return Optional.empty();
            } else {
                return findResourceFromBundle(bundle, clazz);
            }
        } else {
            log.debug("Hapi-Fhir Resource for {} NOT found in DB.",
                    clazz.getSimpleName());
            return Optional.empty();
        }
    }

    public <T extends Resource> Optional<T> findResourceFromBundle(Bundle bundle, Class<T> clazz) {
        Resource resource = bundle.getEntry().get(0).getResource();

        if (clazz.isInstance(resource)) {
            log.debug("Hapi-Fhir Resource for {} found in DB.",
                    clazz.getSimpleName());
            return Optional.of((T) resource);
        } else {
            log.error("Hapi-Fhir Resource is of wrong type expected: {} found in bundle: {}",
                    clazz.getSimpleName(),
                    resource.getClass().getSimpleName());
            return Optional.empty();
        }
    }


    public Optional<String> processBundleLink(Bundle bundle) {
        if (bundle.hasEntry()) {
            if (bundle.getEntry().isEmpty()) {
                return Optional.of(bundle.getLink().get(0).getUrl());
            } else {
                return Optional.of(bundle.getEntry().get(0).getFullUrl());
            }
        } else {
            return Optional.empty();
        }
    }

    public String persist(Resource resource) {
        log.debug("Persisting resource {} with id {}",
                resource.getResourceType() != null ? resource.getResourceType().name() : "null",
                resource.getId());
        Bundle bundle = createAndExecuteBundle(resource);

        validatePersistedBundle(resource, bundle);

        //todo this is wrong without it we cannot find object after we create
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            log.debug("InterruptedException", e);
        }

        return buildResourceUrl(resource);
    }

    public String buildResourceUrl(Resource resource) {
        return buildHapiFhirUrl(resource.fhirType(), resource.getId());
    }

    public String buildHapiFhirUrl(String type, String id) {
        return new StringBuilder(hapiClient.getServerBase())
                .append(type)
                .append('/')
                .append(id)
                .toString();
    }

    private void validatePersistedBundle(Resource resource, Bundle bundle) {
        if (CollectionUtils.isEmpty(bundle.getEntry()) || bundle.getEntry().size() > 1) {
            log.error("Bundle size is invalid: {}", bundle.getEntry() != null ? bundle.getEntry().size() : null);
            throw new HapiFhirCreateException(resource.getIdElement().getValue());
        }

        Bundle.BundleEntryComponent bundleEntryComponent = bundle.getEntry().get(0);

        if (!bundleEntryComponent.hasResponse()) {
            log.error("Bundle does not contain a response");
            throw new HapiFhirCreateException(resource.getIdElement().getValue());
        }

        if (bundleEntryComponent.getResponse().getStatus() != null &&
                bundleEntryComponent.getResponse().getStatus().startsWith("20")) {
            log.debug("Successfully (OK) Persisted resource {} with id {}",
                    resource.getResourceType() != null ? resource.getResourceType().name() : "null",
                    resource.getId());
        } else {
            log.error("FAILED Persisted resource: {} with id: {} status:{}",
                    resource.getResourceType().name(), resource.getId(),
                    bundleEntryComponent.getResponse().getStatus());
            throw new HapiFhirCreateException(resource.getIdElement().getValue());
        }
    }

    public Bundle createAndExecuteBundle(Resource resource) {
        Bundle bundle = buildBundle(resource);

        return hapiClient.transaction()
                .withBundle(bundle)
                .execute();
    }

    public Bundle buildBundle(Resource resource) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        bundle.addEntry().setResource(resource)
                .getRequest()
                .setUrl(baseURL + resource.getResourceType().name() + "/" + resource.getId())
                .setMethod(Bundle.HTTPVerb.PUT);
        return bundle;
    }

    private LoggingInterceptor createLoggingInterceptor() {
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLogger(log);

        // Optionally you may configure the interceptor (by default only summary info is logged)
        loggingInterceptor.setLogRequestBody(false);
        loggingInterceptor.setLogRequestHeaders(false);
        loggingInterceptor.setLogRequestSummary(true);

        loggingInterceptor.setLogResponseBody(false);
        loggingInterceptor.setLogResponseHeaders(false);
        loggingInterceptor.setLogResponseSummary(true);

        return loggingInterceptor;
    }

    public IBaseOperationOutcome delete(IBaseResource resource) {
        return hapiClient.delete()
                .resource(resource)
                .prettyPrint()
                .encodedJson()
                .execute();
    }

    public int count(Class<? extends Resource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .totalMode(SearchTotalModeEnum.ACCURATE)
                .returnBundle(Bundle.class)
                .execute()
                .getTotal();
    }

    public Bundle getAll(Class<? extends Resource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .returnBundle(Bundle.class)
                .execute();
    }

    public Bundle getNextPage(Bundle bundle) {
        return hapiClient.loadPage()
                .next(bundle)
                .execute();
    }

    public String toJson(Resource resource) {
        return getCtx().newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(resource);
    }

    public <T extends Resource> T parseResource(Class<T> resourceClass, String resourceJson) {
        return getCtx()
                .newJsonParser()
                .parseResource(resourceClass, resourceJson);
    }

    public Bundle getConsentById(String id) {
        Bundle bundle = hapiClient
                .search()
                .forResource(Consent.class)
                .where(Consent.IDENTIFIER.exactly().systemAndValues("http://sdhealthconnect.github.io/leap/samples/ids", id))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public Bundle getMedicationRequestById(String id) {
        Bundle bundle = hapiClient
                .search()
                .forResource(MedicationRequest.class)
                .where(Resource.RES_ID.exactly().code(id))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public List<IBaseResource> getAllConsentsForPatient(String id) {
        List<IBaseResource> consents = new ArrayList<>();
        Bundle bundle = hapiClient
                .search()
                .forResource(Consent.class)
                .where(Consent.PATIENT.hasId(id))
                .returnBundle(Bundle.class)
                .execute();
        consents.addAll(BundleUtil.toListOfResources(ctx, bundle));
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = hapiClient
                    .loadPage()
                    .next(bundle)
                    .execute();
            consents.addAll(BundleUtil.toListOfResources(ctx, bundle));
        }
        return consents;
    }

    public List<IBaseResource> getAllOrganizations(String state) {
        //todo implement by state whereclause when more data is available
            /* Bundle bundle = hapiClient
                    .search()
                    .forResource(Organization.class)
                    .where(Organization.ADDRESS_STATE.matches().value(state))
                    .returnBundle(Bundle.class)
                    .execute();

             */
        SortSpec sortSpec = new SortSpec();
        sortSpec.setParamName("name");
        sortSpec.setOrder(SortOrderEnum.ASC);
        List<IBaseResource> organizations = new ArrayList<>();
        Bundle bundle = hapiClient
                .search()
                .forResource(Organization.class)
                .sort(sortSpec)
                .returnBundle(Bundle.class)
                .execute();
        organizations.addAll(BundleUtil.toListOfResources(ctx, bundle));
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = hapiClient
                    .loadPage()
                    .next(bundle)
                    .execute();
            organizations.addAll(BundleUtil.toListOfResources(ctx, bundle));
        }

        return organizations;
    }

    public List<IBaseResource> getAllPractitioners(String state) {
        //todo implement by state whereclause when more data is available
            /*Bundle bundle = hapiClient
                    .search()
                    .forResource(Practitioner.class)
                    .where(Practitioner.ADDRESS_STATE.matches().value(state))
                    .returnBundle(Bundle.class)
                    .execute();

             */
        SortSpec sortSpec = new SortSpec();
        sortSpec.setParamName("family");
        sortSpec.setOrder(SortOrderEnum.ASC);
        List<IBaseResource> practitioners = new ArrayList<>();
        Bundle bundle = hapiClient
                .search()
                .forResource(Practitioner.class)
                .sort(sortSpec)
                .returnBundle(Bundle.class)
                .execute();
        practitioners.addAll(BundleUtil.toListOfResources(ctx, bundle));
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = hapiClient
                    .loadPage()
                    .next(bundle)
                    .execute();
            practitioners.addAll(BundleUtil.toListOfResources(ctx, bundle));
        }
        return practitioners;
    }

    public Bundle getAllPatientAuditEvents(String id) {
        Bundle bundle = hapiClient
                .search()
                .forResource(AuditEvent.class)
                .where(AuditEvent.PATIENT.hasId(id))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public Bundle getQuestionnaireResponse(String questionnaireId, String patientId) {
        Bundle bundle = hapiClient
                .search()
                .forResource(QuestionnaireResponse.class)
                .where(QuestionnaireResponse.PATIENT.hasId(patientId))
                .and(QuestionnaireResponse.QUESTIONNAIRE.hasId(questionnaireId))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public Bundle getPatientBundle(String id) {
        Bundle bundle = hapiClient
                .search()
                .forResource(Patient.class)
                .where(Resource.RES_ID.exactly().code(id))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public List<IBaseResource> getAuditEvents(final String patientId) {
        SortSpec sortSpec = new SortSpec();
        sortSpec.setParamName("date");
        sortSpec.setOrder(SortOrderEnum.DESC);
        List<IBaseResource> auditEvents = new ArrayList<>();
        Bundle bundle = hapiClient
                .search()
                .forResource(AuditEvent.class)
                .sort(sortSpec)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        auditEvents.addAll(BundleUtil.toListOfResources(ctx, bundle));
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = hapiClient
                    .loadPage()
                    .next(bundle)
                    .execute();
            auditEvents.addAll(BundleUtil.toListOfResources(ctx, bundle));
        }
        return auditEvents;
    }

    public Bundle getOrganization(Identifier organizationId) {
        Bundle bundle = hapiClient
                .search()
                .forResource(Organization.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode(organizationId.getSystem(), organizationId.getValue()))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public Bundle getPractitioner(Identifier practitionerId) {
        Bundle bundle = hapiClient
                .search()
                .forResource(Practitioner.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode(practitionerId.getSystem(), practitionerId.getValue()))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public List<IBaseResource> getMedicationRequests(final String patientId) {
        List<IBaseResource> medList = new ArrayList<>();
        Bundle bundle = hapiClient
                .search()
                .forResource(MedicationRequest.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
        medList.addAll(BundleUtil.toListOfResources(ctx, bundle));
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = hapiClient
                    .loadPage()
                    .next(bundle)
                    .execute();
            medList.addAll(BundleUtil.toListOfResources(ctx, bundle));
        }
        return medList;
    }

    public Bundle getServiceRequests(final String patientId) {
        Bundle bundle = hapiClient
                .search()
                .forResource(ServiceRequest.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public List<Reference> getSubjectsWithSpecificCondition(String system, String code) {
        List<Reference> refList = new ArrayList<>();
        Bundle bundle = hapiClient
                .search()
                .forResource(Condition.class)
                .where(Condition.CODE.exactly().systemAndCode(system, code))
                .and(Condition.CLINICAL_STATUS.exactly().code("active"))
                .returnBundle(Bundle.class)
                .execute();

        List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Bundle.BundleEntryComponent entryComponent = (Bundle.BundleEntryComponent) iter.next();
            Condition condition = (Condition)entryComponent.getResource();
            Reference ref = condition.getSubject();
            refList.add(ref);
        }
        return refList;
    }

    public List<IBaseResource> getSubjectsForSpecificPatientReference(String reference) {
        List<IBaseResource> subjectList = new ArrayList<>();
        Bundle bundle = hapiClient
                .search()
                .forResource(ResearchSubject.class)
                .where(new ReferenceClientParam("patient").hasId(reference))
                .returnBundle(Bundle.class)
                .execute();
        subjectList.addAll(BundleUtil.toListOfResources(ctx, bundle));
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = hapiClient
                    .loadPage()
                    .next(bundle)
                    .execute();
            subjectList.addAll(BundleUtil.toListOfResources(ctx, bundle));
        }
        return subjectList;
    }

    public Bundle getResearchStudyById(String id) {
        Bundle res = hapiClient
                .search()
                .forResource(ResearchStudy.class)
                .where(Resource.RES_ID.exactly().code(id))
                .returnBundle(Bundle.class)
                .execute();
        return res;
    }

    public Bundle getResearchSubjectById(String id) {
        Bundle bundle = hapiClient
                .search()
                .forResource(ResearchSubject.class)
                .where(Resource.RES_ID.exactly().code(id))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public Bundle getOrganizationById(String id) {
        Bundle bundle = hapiClient
                .search()
                .forResource(Organization.class)
                .where(Resource.RES_ID.exactly().code(id))
                .returnBundle(Bundle.class)
                .execute();
        return bundle;
    }

    public Questionnaire getQuestionnaire(String id) {
        Bundle bundle = hapiClient
                .search()
                .forResource(Questionnaire.class)
                .where(Resource.RES_ID.exactly().code(id))
                .returnBundle(Bundle.class)
                .execute();
        Questionnaire res = null;
        try {
            res = (Questionnaire) bundle.getEntry().get(0).getResource();
        }
        catch (Exception ex) {
            log.info("Questionnaire note found");
        }
        return res;
    }

    public Collection<MedicationStatement> getMedicationStatementByPatientid(String patientId) {
        Collection<MedicationStatement> mList = new ArrayList<>();
        return mList;
    }
}
