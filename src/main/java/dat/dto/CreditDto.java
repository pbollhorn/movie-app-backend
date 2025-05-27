package dat.dto;

import dat.enums.Gender;

public record CreditDto(
        Integer personId,
        String name,
        Gender gender,
        String job,
        String character,
        Integer rankInMovie) {
}