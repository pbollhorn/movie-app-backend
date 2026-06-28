package dat.dto;

import java.util.List;

public record CreditDto(String id, // String with this format "department_personId"
                        Integer personId,
                        String name,
                        String department,
                        List<String> jobsInDepartment,
                        List<String> characters) {
}
