package dat.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Entity
public class Collection {

    @Id
    private Integer id;

    private String name;  // name with "Collection" at the end removed

}
