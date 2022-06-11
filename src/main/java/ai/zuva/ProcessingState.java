package ai.zuva;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProcessingState {
    @JsonProperty("queued")
    QUEUED(){
        @Override
        public boolean isQueued() {
            return true;
        }
    },
    @JsonProperty("processing")
    PROCESSING{
        @Override
        public boolean isProcessing() {
            return true;
        }
    },
    @JsonProperty("complete")
    COMPLETE{
        @Override
        public boolean isComplete() {
            return true;
        }
    },
    @JsonProperty("failed")
    FAILED{
        @Override
        public boolean isFailed() {
            return true;
        }
    },
    // Future-proofing in case more intermediate processing states are exposed in the future
    @JsonEnumDefaultValue
    UNRECOGNIZED_STATUS;

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
}