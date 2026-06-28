package dat.dto;

public record CreditDto(String id,
                        Integer personId,
                        String name,
                        String jobs, // Multiple jobs in same department are joined with ", "
                        String department,
                        String characters) // Multiple characters are joined with " / "
{
}
