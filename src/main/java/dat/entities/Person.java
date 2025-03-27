package dat.entities;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

import dat.dto.CreditDto;
import dat.enums.Gender;

@ToString
@Getter
@NoArgsConstructor
@Entity
public class Person {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ToString.Exclude
    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    private Set<Credit> credits;

    public Person(CreditDto c) {
        this.id = c.personId();
        this.name = c.name();
        this.gender = c.gender();
    }

}