package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.util.UIUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.sql.Blob;
import java.time.ZoneId;

@Slf4j
@PageTitle("User Preferences")
@Route(value = "userpreferencesview", layout = MainLayout.class)
@CssImport("./styles/components/user-view.css")
public class UserPreferencesView extends ViewFrame {
    private TextField firstName = new TextField();
    private TextField middleName = new TextField();
    private TextField lastName = new TextField();
    private TextField maritalStatus = new TextField();
    private TextField eyeColor = new TextField();
    private TextField hairColor = new TextField();
    private TextField weight = new TextField();
    private TextField gender = new TextField();
    private TextField state = new TextField();
    private TextField ethnicity = new TextField();

    private TextField username = new TextField();
    private TextField prefix = new TextField();
    private DatePicker dateOfBirth = new DatePicker();
    private TextField height = new TextField();
    private TextField streetAddress1 = new TextField();
    private TextField streetAddress2 = new TextField();
    private TextField city = new TextField();
    private TextField zipCode = new TextField();
    private TextField phone = new TextField();
    private TextField mobile = new TextField();
    private TextField emailAddress = new TextField();
    private TextField languagePreference = new TextField();
    private TextField primaryPhysician = new TextField();
    private TextField primaryPhysicianPhoneNumber = new TextField();
    private TextField emergencyContact = new TextField();
    private TextField emergencyContactPhone = new TextField();
    private TextField relationship = new TextField();
    private Image image = new Image();


    public UserPreferencesView() {
        setId("userpreferencesview");
        FormLayout layoutWithFormItems = new FormLayout();
        firstName.setPlaceholder(getTranslation("UserPreferences-firstname_placeholder"));
        middleName.setPlaceholder(getTranslation("UserPreferences-middlename_placeholder"));
        lastName.setPlaceholder(getTranslation("UserPreferences-lastName_placeholder"));
        maritalStatus.setPlaceholder(getTranslation("UserPreferences-maritalstatus_.placeholder"));
        eyeColor.setPlaceholder(getTranslation("UserPreferences-eyecolor_placeholder"));
        hairColor.setPlaceholder(getTranslation("UserPreferences-haircolor_placeholder"));
        weight.setPlaceholder(getTranslation("UserPreferences-weight_placeholder"));
        gender.setPlaceholder(getTranslation("UserPreferences-gender_placeholder"));
        state.setPlaceholder(getTranslation("UserPreferences-state_placeholder"));
        ethnicity.setPlaceholder(getTranslation("UserPreferences-ethnicity_placeholder"));

        //DatePicker birthDate = new DatePicker();
        populateUSerFromSession();

        layoutWithFormItems.addFormItem(firstName, getTranslation("UserPreferences-firstName"));
        layoutWithFormItems.addFormItem(middleName, getTranslation("UserPreferences-middleName"));

        layoutWithFormItems.addFormItem(lastName, getTranslation("UserPreferences-lastName"));
        layoutWithFormItems.addFormItem(maritalStatus, getTranslation("UserPreferences-maritalStatus"));

        layoutWithFormItems.addFormItem(eyeColor, getTranslation("UserPreferences-eyeColor"));
        layoutWithFormItems.addFormItem(hairColor, getTranslation("UserPreferences-hairColor"));


        layoutWithFormItems.addFormItem(weight, getTranslation("UserPreferences-weight"));
        layoutWithFormItems.addFormItem(gender, getTranslation("UserPreferences-gender"));

        layoutWithFormItems.addFormItem(state, getTranslation("UserPreferences-state"));
        layoutWithFormItems.addFormItem(ethnicity, getTranslation("UserPreferences-ethnicity"));

        layoutWithFormItems.addFormItem(username, getTranslation("UserPreferences-username"));
        layoutWithFormItems.addFormItem(prefix, getTranslation("UserPreferences-prefix"));


        layoutWithFormItems.addFormItem(dateOfBirth, getTranslation("UserPreferences-date_of_birth"));
        layoutWithFormItems.addFormItem(height, getTranslation("UserPreferences-height"));

        layoutWithFormItems.addFormItem(streetAddress1, getTranslation("UserPreferences-street_address1"));
        layoutWithFormItems.addFormItem(streetAddress2, getTranslation("UserPreferences-street_address2"));

        layoutWithFormItems.addFormItem(city, getTranslation("UserPreferences-city"));
        layoutWithFormItems.addFormItem(zipCode, getTranslation("UserPreferences-zipCode"));

        layoutWithFormItems.addFormItem(phone, getTranslation("UserPreferences-phone"));
        layoutWithFormItems.addFormItem(mobile, getTranslation("UserPreferences-mobile"));

        layoutWithFormItems.addFormItem(emailAddress, getTranslation("UserPreferences-emailAddress"));
        layoutWithFormItems.addFormItem(languagePreference, getTranslation("UserPreferences-language_preference"));

        layoutWithFormItems.addFormItem(primaryPhysician, getTranslation("UserPreferences-primary_physician"));
        layoutWithFormItems.addFormItem(primaryPhysicianPhoneNumber, getTranslation("UserPreferences-primary_physician_phone_number"));

        layoutWithFormItems.addFormItem(emergencyContact, getTranslation("UserPreferences-emergency_contact"));
        layoutWithFormItems.addFormItem(emergencyContactPhone, getTranslation("UserPreferences-emergency_contact_phone"));

        layoutWithFormItems.addFormItem(relationship, getTranslation("UserPreferences-relationship"));
        layoutWithFormItems.addFormItem(image, getTranslation("UserPreferences-image"));
        setViewContent(layoutWithFormItems);
    }

    private void populateUSerFromSession() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        ConsentUser user = consentSession.getConsentUser();
        this.firstName.setValue(user.getFirstName());
        this.firstName.setEnabled(false);
        this.middleName.setValue(user.getMiddleName());
        this.middleName.setEnabled(false);
        this.lastName.setValue(user.getLastName());
        this.lastName.setEnabled(false);
        this.maritalStatus.setValue(user.getMaritalStatus());
        this.maritalStatus.setEnabled(false);
        this.eyeColor.setValue(user.getEyeColor());
        this.eyeColor.setEnabled(false);
        this.hairColor.setValue(user.getHairColor());
        this.hairColor.setEnabled(false);
        this.weight.setValue(String.valueOf(user.getWeight()));
        this.weight.setEnabled(false);
        this.gender.setValue(user.getGender());
        this.gender.setEnabled(false);
        this.state.setValue(user.getState());
        this.state.setEnabled(false);
        this.ethnicity.setValue(user.getEthnicity());
        this.ethnicity.setEnabled(false);
        //Transient values
        this.username.setValue(user.getUserName());
        this.username.setEnabled(false);
        this.prefix.setValue(user.getPrefix());
        this.prefix.setEnabled(false);
        this.dateOfBirth.setValue(user.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        this.dateOfBirth.setEnabled(false);

        if (user.getHeight()!=null) {
            this.height.setValue(user.getHeight());
        }
        this.height.setEnabled(false);
        if (user.getStreetAddress1()!=null) {
            this.streetAddress1.setValue(user.getStreetAddress1());
        }
        this.streetAddress1.setEnabled(false);
        if (user.getStreetAddress2()!=null) {
            this.streetAddress2.setValue(user.getStreetAddress2());
        }
        this.streetAddress2.setEnabled(false);
        if (user.getCity()!=null) {
            this.city.setValue(user.getCity());
        }
        this.city.setEnabled(false);
        if (user.getZipCode()!=null) {
            this.zipCode.setValue(user.getZipCode());
        }
        this.zipCode.setEnabled(false);
        if (user.getPhone()!=null) {
            this.phone.setValue(user.getPhone());
        }
        this.phone.setEnabled(false);
        if (user.getPhone() != null) {
            this.mobile.setValue(user.getPhone());  //todo get this from fhir patient resource
        }
        this.mobile.setEnabled(false);
        if (user.getEmailAddress()!=null) {
            this.emailAddress.setValue(user.getEmailAddress());
        }
        this.emailAddress.setEnabled(false);
        if (user.getLanguagePreference()!=null) {
            this.languagePreference.setValue(user.getLanguagePreference());
        }
        this.languagePreference.setEnabled(false);
        if (user.getPrimaryPhysician()!=null) {
            this.primaryPhysician.setValue(user.getPrimaryPhysician());
        }
        this.primaryPhysician.setEnabled(false);
        if (user.getPrimaryPhysicianPhoneNumber()!=null) {
            this.primaryPhysicianPhoneNumber.setValue(user.getPrimaryPhysicianPhoneNumber());
        }
        this.primaryPhysicianPhoneNumber.setEnabled(false);
        if (user.getEmergencyContact()!=null) {
            this.emergencyContact.setValue(user.getEmergencyContact());
        }
        this.emergencyContact.setEnabled(false);
        if (user.getEmergencyContactPhone()!=null) {
            this.emergencyContactPhone.setValue(user.getEmergencyContactPhone());
        }
        this.emergencyContactPhone.setEnabled(false);
        if (user.getRelationship()!=null) {
            this.relationship.setValue(user.getRelationship());
        }
        this.relationship.setEnabled(false);

        //this.image = UIUtils.createImageFromText("S.G");
        byte[] b = user.getUser().getPhoto();
        try {
            if ( b != null) {
                this.image = UIUtils.createImage(b, "avatar.png", "avatar");
                this.image.setMaxHeight("45px");
                this.image.setMaxWidth("45px");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @PostConstruct
    public void setup(){
        log.info("on setup");
    }

    public void onAttach(AttachEvent attachEvent) {
        MainLayout.get().getAppBar().setTitle(getTranslation("UserPreferences"));
    }

}
