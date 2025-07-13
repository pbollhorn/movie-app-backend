package dat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbCreditDto(@JsonProperty("credit_id")
                            String creditId,
                            @JsonProperty("id")
                            Integer personId,
                            String name,
                            String job,
                            String department,
                            String character) {


}
