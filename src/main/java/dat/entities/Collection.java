package dat.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Collection {

    @Id
    private Integer id;

    private String name;  // name with "Collection" at the end removed

}
