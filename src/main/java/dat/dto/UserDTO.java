package dat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

//public record UserDTO(Integer id, String username, String password, Set<Roles> roles) {
//
//    public UserDTO(Integer id, String username, Set<Roles> roles) {
//        this(id, username, null, roles);
//    }
//
//}

@NoArgsConstructor
@Getter
public class UserDTO extends dk.bugelhartmann.UserDTO {

    private Integer id;

    public UserDTO(Integer id, String username, Set<String> roles) {
        super(username, roles);
        this.id = id;
    }

}