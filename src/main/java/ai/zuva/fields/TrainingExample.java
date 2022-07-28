package ai.zuva.fields;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public class TrainingExample {
  @JsonProperty("file_id")
  public String fileId;

  @JsonProperty("locations")
  public Location[] locations;

  // Default constructor necessary for JSON deserialization
  public TrainingExample() {}

  public TrainingExample(String fileId) {
    this.fileId = fileId;
    this.locations = new Location[] {};
  }

  public void addLocation(long start, long end) {
    this.locations = Arrays.copyOf(this.locations, this.locations.length + 1);
    this.locations[this.locations.length - 1] = new Location(start, end);
  }
}
