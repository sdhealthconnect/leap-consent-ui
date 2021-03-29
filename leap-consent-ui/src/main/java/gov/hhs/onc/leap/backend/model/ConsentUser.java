package gov.hhs.onc.leap.backend.model;

import gov.hhs.onc.leap.security.model.User;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Entity
@Table(name = "consentuser")
public class ConsentUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "first_name")
    @NotEmpty(message = "*Please provide your first name")
    private String firstName;                   // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "middle_name")
    private String middleName;                  // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "last_name")
    @NotEmpty(message = "*Please provide your last name")
    private String lastName;                    // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "marital_status")
    @NotEmpty(message = "*Please provide your marital status")
    private String maritalStatus;               // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "eye_color")
    @NotEmpty(message = "*Please provide your eye color")
    private String eyeColor;                    // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "hair_color")
    @NotEmpty(message = "*Please provide your hair color")
    private String hairColor;                   // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "weight")
    @NotEmpty(message = "*Please provide your weight")
    private Double weight;                      // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "gender")
    @NotEmpty(message = "*Please provide your gender")
    private String gender;                      // Fullfilled with DB, overwritten with Fhir information
    @Column(name = "state")
    @NotEmpty(message = "*Please provide your state")
    private String state;                       // Fullfilled with DB, default "Arizona"
    @Column(name = "ethnicity")
    @NotEmpty(message = "*Please provide your ethnicity")
    private String ethnicity;                   // Fullfilled with DB

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "fhir_patient_id", referencedColumnName = "fhir_patient_id")
    private User user;                          // Security attribute to link with Fhir information

    @Transient
    private String userName;                    // Security attribute
    @Transient
    private String prefix;                      // Fhir attribute
    @Transient
    private Date dateOfBirth;                   // Fhir attribute
    @Transient
    private String height;                      // Manual entry attribute
    @Transient
    private String streetAddress1;              // Fhir attribute
    @Transient
    private String streetAddress2;              // Fhir attribute
    @Transient
    private String city;                        // Fhir attribute
    @Transient
    private String zipCode;                     // Fhir attribute
    @Transient
    private String phone;                       // Fhir attribute
    @Transient
    private String mobile;                      // Fhir attribute
    @Transient
    private String emailAddress;                // Security attribute
    @Transient
    private String languagePreference;          // Manual entry attribute
    @Transient
    private String primaryPhysician;            // Manual entry attribute
    @Transient
    private String primaryPhysicianPhoneNumber; // Manual entry attribute
    @Transient
    private String emergencyContact;            // Manual entry attribute
    @Transient
    private String getEmergencyContactPhone;    // Manual entry attribute
    @Transient
    private String relationship;                // Manual entry attribute

}
