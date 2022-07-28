package ai.zuva.docai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.IOUtils;

class TestHelpers {
  static String resourceAsString(Object obj, String resourceName) throws IOException {
    return IOUtils.toString(
        Objects.requireNonNull(obj.getClass().getClassLoader().getResource(resourceName)),
        StandardCharsets.UTF_8);
  }

  static byte[] resourceAsByteArray(Object obj, String resourceName) throws IOException {
    return IOUtils.toByteArray(obj.getClass().getClassLoader().getResource(resourceName));
  }
}
