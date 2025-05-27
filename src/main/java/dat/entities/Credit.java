package dat.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Person person;

    private String job;
    private String character;
    private Integer rankInMovie;
}