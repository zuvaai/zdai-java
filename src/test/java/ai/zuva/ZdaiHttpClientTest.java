package ai.zuva;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest
class ZdaiHttpClientTest {

    private String resourceAsString (String resourceName) throws IOException {
        return  IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResource(resourceName)), "UTF-8");
    }

    @Test
    void requestClassification() {
    }

    @Test
    void getClassificationResult() {
    }

    @Test
    void requestLanguage() {
    }

    @Test
    void getLanguageResult() {
    }

    @Test
    void listFields() {
    }
}