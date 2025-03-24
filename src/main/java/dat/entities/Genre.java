package dat.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Genre {
    @Id
    Integer id;

    @Column(nullable = false, unique = true)
    String name;
}