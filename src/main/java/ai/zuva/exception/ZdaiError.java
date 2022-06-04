package ai.zuva.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZdaiError {
    public String code;
    public String message;
}
