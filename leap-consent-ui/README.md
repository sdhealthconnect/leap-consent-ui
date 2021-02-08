# LEAP Consent UI
More details to follow...

## Build Project
'mvn clean install -DskipTests'

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


