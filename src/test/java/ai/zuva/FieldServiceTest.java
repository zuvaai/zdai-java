package ai.zuva;

import ai.zuva.fields.*;
import ai.zuva.http.ZdaiHttpClient;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest
public class FieldServiceTest {

    @Test
    void testListFields(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        int port = wmRuntimeInfo.getHttpPort();

        String responseBody = TestHelpers.resourceAsString(this, "fields-list.json");

        stubFor(get("/fields")
                .withRequestBody(binaryEqualTo(new byte[]{}))
                .willReturn(ok().withBody(responseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        FieldService fs = new FieldService(client);

        try {
            FieldListElement result = fs.listFields()[0];
            assertEquals("e", result.name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testFieldMetadata(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        int port = wmRuntimeInfo.getHttpPort();

        String responseBody = TestHelpers.resourceAsString(this, "field-metadata.json");
        String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
        stubFor(get(String.format("/fields/%s/metadata", fieldId))
                .withRequestBody(binaryEqualTo(new byte[]{}))
                .willReturn(ok().withBody(responseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        FieldService fs = new FieldService(client);

        try {
            FieldMetadata result = fs.getFieldMetadata(fieldId);
            assertEquals("Test Field", result.name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testUpdateFieldMetadata(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        int port = wmRuntimeInfo.getHttpPort();

        String requestBody = TestHelpers.resourceAsString(this, "field-metadata-update.json");
        String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
        stubFor(put(String.format("/fields/%s/metadata", fieldId))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(noContent().withBody("1")));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        FieldService fs = new FieldService(client);

        try {
            fs.updateFieldMetadata(fieldId, "Updated name", "Updated description");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetFieldAccuracy(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        int port = wmRuntimeInfo.getHttpPort();

        String responseBody = TestHelpers.resourceAsString(this, "field-accuracy.json");
        String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
        stubFor(get(String.format("/fields/%s/accuracy", fieldId))
                .withRequestBody(binaryEqualTo(new byte[]{}))
                .willReturn(ok().withBody(responseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        FieldService fs = new FieldService(client);

        try {
            FieldAccuracy result = fs.getFieldAccuracy(fieldId);
            assertEquals(0.95, result.fScore, 0.0001);
            assertEquals(1.0, result.recall, 0.0001);
            assertEquals(0.9, result.precision, 0.0001);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetFieldValidationDetails(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        int port = wmRuntimeInfo.getHttpPort();

        String responseBody = TestHelpers.resourceAsString(this, "field-validation-details.json");
        String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
        stubFor(get(String.format("/fields/%s/validation-details", fieldId))
                .withRequestBody(binaryEqualTo(new byte[]{}))
                .willReturn(ok().withBody(responseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        FieldService fs = new FieldService(client);

        try {
            FieldValidation[] result = fs.getFieldValidationDetails(fieldId);
            assertEquals(1, result.length);
            assertEquals("c71pmdbo2ua691f0fkog", result[0].fileId);
            assertEquals("tp", result[0].type);
            assertArrayEquals(new int[]{26952, 27310}, result[0].location);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateField(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        int port = wmRuntimeInfo.getHttpPort();

        String requestBody = TestHelpers.resourceAsString(this, "create-field.json");
        String responseBody = TestHelpers.resourceAsString(this, "field-created.json");

        stubFor(post("/fields")
                .withRequestBody(equalToJson(requestBody))
                .willReturn(created().withBody(responseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        FieldService fs = new FieldService(client);

        try {
            String fieldId = fs.createField("Test Field", "Test field description");
            assertEquals("f7d397b7-f541-4390-b8b9-0eaa6723aa9c", fieldId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
