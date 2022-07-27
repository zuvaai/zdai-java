package ai.zuva;

import ai.zuva.api.DocAIClient;
import ai.zuva.exception.DocAIApiException;
import ai.zuva.files.File;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
public class FileTest {

  @Test
  void testSubmitFileWithContentType(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    int port = wmRuntimeInfo.getHttpPort();
    String content = "Sample text";
    String responseBody = TestHelpers.resourceAsString(this, "text-file-created-response.json");

    stubFor(
        post("/api/v2/files")
            .withHeader("Content-Type", containing("text/plain"))
            .withRequestBody(equalTo(content))
            .willReturn(created().withBody(responseBody)));

    DocAIClient zClient = new DocAIClient("http://localhost:" + port, "my-token");

    try {
      File result = File.submitFile(zClient, "Sample text", "text/plain");
      assertEquals("c5e40jn1qk1er7odm71g", result.fileId);
      assertEquals("text/plain", result.attributes.contentType);
      assertEquals("", result.permissions[0]);
      assertEquals("2021-10-07T12:08:46Z", result.expiration);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testSubmitFileNoContentType(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    int port = wmRuntimeInfo.getHttpPort();
    String content = "Sample text";
    String responseBody = TestHelpers.resourceAsString(this, "pdf-file-created-response.json");

    stubFor(
        post("/api/v2/files")
            .withRequestBody(equalTo(content))
            .willReturn(created().withBody(responseBody)));

    DocAIClient zClient = new DocAIClient("http://localhost:" + port, "my-token");

    try {
      File result = File.submitFile(zClient, content);
      assertEquals("c5e407f1qk1er7odm6tg", result.fileId);
      assertEquals("application/pdf", result.attributes.contentType);
      assertEquals("", result.permissions[0]);
      assertEquals("2021-10-07T12:07:57Z", result.expiration);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testSubmitFileBytes(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    int port = wmRuntimeInfo.getHttpPort();
    byte[] content = new byte[] {1, 2, 3, 4};
    String responseBody = TestHelpers.resourceAsString(this, "kiraocr-file-created-response.json");

    stubFor(
        post("/api/v2/files")
            .withHeader("Content-Type", containing("application/kiraocr"))
            .withRequestBody(binaryEqualTo(content))
            .willReturn(created().withBody(responseBody)));

    DocAIClient zClient = new DocAIClient("http://localhost:" + port, "my-token");

    File result =
        assertDoesNotThrow(() -> File.submitFile(zClient, content, "application/kiraocr"));
    assertEquals("c5e40of1qk1er7odm740", result.fileId);
    assertEquals("application/kiraocr", result.attributes.contentType);
    assertEquals("", result.permissions[0]);
    assertEquals("2021-10-07T12:09:05Z", result.expiration);
  }

  @Test
  void deleteFileSuccessTest(WireMockRuntimeInfo wmRuntimeInfo) {
    int port = wmRuntimeInfo.getHttpPort();

    stubFor(delete("/api/v2/files/123").willReturn(noContent().withBody("1")));

    DocAIClient zClient = new DocAIClient("http://localhost:" + port, "my-token");
    File file = new File(zClient, "123");

    // Implied success if no error is thrown
    assertDoesNotThrow(() -> file.delete());
  }

  @Test
  void deleteFileFailsTest(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
    int port = wmRuntimeInfo.getHttpPort();

    String responseBody = TestHelpers.resourceAsString(this, "file-not-found.json");
    stubFor(delete("/api/v2/files/123").willReturn(notFound().withBody(responseBody)));

    DocAIClient zClient = new DocAIClient("http://localhost:" + port, "my-token");
    File file = new File(zClient, "123");

    DocAIApiException thrown = assertThrows(DocAIApiException.class, () -> file.delete());
    assertEquals(404, thrown.statusCode);
  }
}
