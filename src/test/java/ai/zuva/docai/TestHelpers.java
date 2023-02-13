package ai.zuva.docai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

  static String listToQueryParams(String key, List<String> requestIds) {
    StringBuilder sb = new StringBuilder();
    for (String requestId : requestIds) {
      sb.append(String.format("%s=%s&", key, requestId));
    }
    // Remove trailing '&'
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
