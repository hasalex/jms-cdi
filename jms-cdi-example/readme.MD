# In WildFly

    mvn wildfly:run

WildFly is starting with standalone-full.xml, in order to have the messaging subsystem.

Then you can open the home page in your favorite browser at http://localhost:8080/jms-cdi-example/

Or you can call the application with

    curl http://127.0.0.1:8080/jms-cdi-example/?text=Hello

The received message will appear in the standard output WildFly.

# Standalone

**TODO**

    mvn exec:java -Pstandalone
