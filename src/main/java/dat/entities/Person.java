package dat.entities;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

import dat.dto.TmdbCreditDto;


@ToString
@Getter
@NoArgsConstructor
@Entity
public class Person {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ToString.Exclude
    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private Set<Credit> credits;

    public Person(TmdbCreditDto c) {
        this.id = c.personId();
        this.name = c.name();
    }

}