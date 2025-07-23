package dat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbCreditDto(
        @JsonProperty("credit_id") String id, // id for the credit
        @JsonProperty("id") Integer personId, // id for the person
        String name, // name for the person
        String job,
        String department,
        String character) {


}
