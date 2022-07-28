package ai.zuva;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.zuva.fields.Field;
import ai.zuva.fields.FieldAccuracy;
import ai.zuva.fields.FieldListElement;
import ai.zuva.fields.FieldMetadata;
import ai.zuva.fields.FieldValidation;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@WireMockTest
public class FieldTest {

  @Test
  void testListFields(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    int port = wmRuntimeInfo.getHttpPort();

    String responseBody = TestHelpers.resourceAsString(this, "fields-list.json");

    stubFor(
        get("/api/v2/fields")
            .withRequestBody(binaryEqualTo(new byte[] {}))
            .willReturn(ok().withBody(responseBody)));

    DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");

    try {
      FieldListElement result = Field.listFields(client)[0];
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
    stubFor(
        get(String.format("/api/v2/fields/%s/metadata", fieldId))
            .withRequestBody(binaryEqualTo(new byte[] {}))
            .willReturn(ok().withBody(responseBody)));

    DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
    Field field = new Field(client, fieldId);

    try {
      FieldMetadata result = field.getMetadata();
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
    stubFor(
        put(String.format("/api/v2/fields/%s/metadata", fieldId))
            .withRequestBody(equalToJson(requestBody))
            .willReturn(noContent().withBody("1")));

    DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
    Field field = new Field(client, fieldId);

    try {
      field.updateMetadata("Updated name", "Updated description");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testGetFieldAccuracy(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    int port = wmRuntimeInfo.getHttpPort();

    String responseBody = TestHelpers.resourceAsString(this, "field-accuracy.json");
    String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
    stubFor(
        get(String.format("/api/v2/fields/%s/accuracy", fieldId))
            .withRequestBody(binaryEqualTo(new byte[] {}))
            .willReturn(ok().withBody(responseBody)));

    DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
    Field field = new Field(client, fieldId);

    try {
      FieldAccuracy result = field.getAccuracy();
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
    stubFor(
        get(String.format("/api/v2/fields/%s/validation-details", fieldId))
            .withRequestBody(binaryEqualTo(new byte[] {}))
            .willReturn(ok().withBody(responseBody)));

    DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
    Field field = new Field(client, fieldId);

    try {
      FieldValidation[] result = field.getValidationDetails();
      assertEquals(1, result.length);
      assertEquals("c71pmdbo2ua691f0fkog", result[0].fileId);
      assertEquals("tp", result[0].type);
      assertArrayEquals(new int[] {26952, 27310}, result[0].location);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testCreateField(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    int port = wmRuntimeInfo.getHttpPort();

    String requestBody = TestHelpers.resourceAsString(this, "create-field.json");
    String responseBody = TestHelpers.resourceAsString(this, "field-created.json");

    stubFor(
        post("/api/v2/fields")
            .withRequestBody(equalToJson(requestBody))
            .willReturn(created().withBody(responseBody)));

    DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");

    try {
      Field field = Field.createField(client, "Test Field", "Test field description");
      assertEquals("f7d397b7-f541-4390-b8b9-0eaa6723aa9c", field.fieldId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
