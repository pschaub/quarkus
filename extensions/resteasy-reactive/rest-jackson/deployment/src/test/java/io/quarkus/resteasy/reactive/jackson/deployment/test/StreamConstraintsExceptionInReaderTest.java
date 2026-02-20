package io.quarkus.resteasy.reactive.jackson.deployment.test;

import java.util.function.Supplier;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

/**
 * Jackson's {@link com.fasterxml.jackson.core.StreamReadConstraints} enforces a default maximum
 * number length of 1000 digits. Numbers exceeding this limit trigger a
 * {@link com.fasterxml.jackson.core.exc.StreamConstraintsException}, which extends
 * {@link com.fasterxml.jackson.core.JsonProcessingException} directly — not
 * {@link com.fasterxml.jackson.core.exc.StreamReadException}. This test verifies that the
 * exception is properly caught and results in a 400 Bad Request response.
 */
public class StreamConstraintsExceptionInReaderTest {

    @RegisterExtension
    static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    return ShrinkWrap.create(JavaArchive.class)
                            .addClasses(FroMage.class, FroMageEndpoint.class);
                }
            });

    @Test
    public void numberExceedingMaxLengthShouldReturn400() {
        // 1001 digits exceeds Jackson's DEFAULT_MAX_NUM_LEN of 1000 (check is: length > 1000)
        RestAssured.with().contentType("application/json")
                .body("{\"price\": " + "9".repeat(1001) + "}")
                .put("/fromage")
                .then().statusCode(400);
    }
}
