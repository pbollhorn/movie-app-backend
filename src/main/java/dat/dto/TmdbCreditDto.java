package dat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dat.enums.Gender;

public record TmdbCreditDto(@JsonProperty("id")
                            Integer personId,
                            String name,
                            Gender gender,
                            Double popularity,
                            String job,
                            String character) {


}
