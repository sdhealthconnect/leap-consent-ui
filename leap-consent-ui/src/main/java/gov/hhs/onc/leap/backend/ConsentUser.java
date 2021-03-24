package gov.hhs.onc.leap.backend;

import lombok.Data;

import java.util.Date;

@Data
public class ConsentUser {
    private String userName;                    // Security attribute
    private String firstName;                   // Fhir attribute
    private String middleName;                  // Fhir attribute
    private String lastName;                    // Fhir attribute
    private Date dateOfBirth;                   // Fhir attribute
    private String maritalStatus;               // Fhir attribute
    private String eyeColor;                    // Manual entry attribute
    private String hairColor;                   // Manual entry attribute
    private String height;                      // Manual entry attribute
    private String weight;                      // Manual entry attribute
    private String gender;                      // Fhir attribute
    private String streetAddress1;              // Fhir attribute
    private String streetAddress2;              // Fhir attribute
    private String city;                        // Fhir attribute
    private String state;                       // Fhir attribute
    private String zipCode;                     // Fhir attribute
    private String phone;                       // Fhir attribute
    private String mobile;                      // Fhir attribute
    private String emailAddress;                // Security attribute
    private String languagePreference;          // Manual entry attribute
    private String primaryPhysician;            // Manual entry attribute
    private String primaryPhysicianPhoneNumber; // Manual entry attribute
    private String emergencyContact;            // Manual entry attribute
    private String getEmergencyContactPhone;    // Manual entry attribute
    private String relationship;                // Manual entry attribute
    private String ethnicity;                   // Fhir attribute
    private String fhirPatientId;               // Security attribute to link with Fhir information

}
