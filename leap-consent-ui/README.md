# LEAP Consent UI
More details to follow...

## Build Project
'mvn clean install -DskipTests'

## Create/Change self signed certificates

`keytool -genkey -alias leap_certificate -keyalg RSA -sigalg SHA256withRSA -keysize 2048 -validity 365000 -keystore keystore.jks`

The certificate will need all this fields to be filled out:
* Keystore password:
* What is your first and last name?
* What is the name of your organizational unit?
* What is the name of your organization?
* What is the name of your City or Locality?
* What is the name of your State or Province?
* What is the two-letter country code for this unit?

### Configuring application yaml 
On this configuration file we have the keystore password, path and te certificate alias that were provided above.
All this can be configured on this file or can be provided using application parameters as presented below:

```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dkeystore.path=/home/user/leap-consent-ui/leap-consent-ui/keystore.jks -Dkeystore.password=changeme -Dkeystore.certificate-alias=leap-certificate"
```

## Databae schema changes:Liquibase
This library is being executed directly from spring boot when the application start but it can be executed as a mvn command if necessary.

All the changes should be in a new "changeset" that are in the file: src/main/resources/db/changelog/changelog.xml

### Liquibase changeset
Liquibase uses changesets to represent a single change to your database. Each changeset has an “id” and “author” attribute which, along with the directory and file name of the changelog file, uniquely identify it.
Please remember that any change in the DB should be wrapped into a liquibase changeset.
To check if there is new changes are available you can run: 

```mvn liquibase:update```

A changeset is immutable, so the idea is to not edit it since liquibase generates a md5sum for the whole changeset; if someone change or hack it the changeset is discarded and will not be applied.

### Liquibase rollback
Typically used when you want to revert changes in your database. There are three rollback modes:
* rollbackCount
* rollbackTag
* rollbackDate

Example:
```mvn liquibase:rollback -Dliquibase.rollbackCount=2```

### Liquibase metadata

Liquibase generate two metadata tables, please ensure that tables DATABASECHANGELOG and DATABASECHANGELOGLOCK will never be manipulated manualli, this tables must be changed only through the library   

## Running the Project in Development Mode

`mvn spring-boot:run`

Wait for the application to start

Open http://localhost:8080/ to view the application.

## Running the Project in Production Mode

`mvn spring-boot:run -Dvaadin.productionMode`

The default mode when the application is built or started is 'development'. The 'production' mode is turned on by setting the `vaadin.productionMode` system property when building or starting the app.

Note that if you switch between running in production mode and development mode, you need to do
```
mvn clean
```
before running in the other mode.


