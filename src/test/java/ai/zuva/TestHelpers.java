package ai.zuva;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Objects;

class TestHelpers {
    static String resourceAsString(Object obj, String resourceName) throws IOException {
        return IOUtils.toString(Objects.requireNonNull(obj.getClass().getClassLoader().getResource(resourceName)), "UTF-8");
    }

    static byte[] resourceAsByteArray(Object obj, String resourceName) throws IOException {
        return IOUtils.toByteArray(obj.getClass().getClassLoader().getResource(resourceName));
    }
}

