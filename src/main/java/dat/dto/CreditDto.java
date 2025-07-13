package dat.dto;

public record CreditDto(
        Integer personId,
        String name,
        String job,
        String character,
        Integer rankInMovie) {
}