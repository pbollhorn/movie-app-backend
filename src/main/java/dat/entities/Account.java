package dat.entities;

import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.mindrot.jbcrypt.BCrypt;

import dat.enums.Role;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String email;

    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ToString.Exclude
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<Rating> ratings;


    public Account(String email, String password) {
        this.email = email;
        this.hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        this.role = Role.USER;
    }

    public boolean verifyPassword(String password) {
        return BCrypt.checkpw(password, this.hashedPassword);
    }


}
