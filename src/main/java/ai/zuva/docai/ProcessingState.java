package ai.zuva.docai;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProcessingState {
  @JsonProperty("queued")
  QUEUED("queued") {
    @Override
    public boolean isQueued() {
      return true;
    }
  },
  @JsonProperty("processing")
  PROCESSING("processing") {
    @Override
    public boolean isProcessing() {
      return true;
    }
  },
  @JsonProperty("complete")
  COMPLETE("complete") {
    @Override
    public boolean isComplete() {
      return true;
    }
  },
  @JsonProperty("failed")
  FAILED("failed") {
    @Override
    public boolean isFailed() {
      return true;
    }
  },
  // Future-proofing in case more intermediate processing states are exposed in the future
  @JsonEnumDefaultValue
  UNRECOGNIZED_STATUS("unrecognized status");

  public boolean isQueued() {
    return false;
  }

  public boolean isProcessing() {
    return false;
  }

  public boolean isComplete() {
    return false;
  }

  public boolean isFailed() {
    return false;
  }

  private final String value;

  ProcessingState(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
