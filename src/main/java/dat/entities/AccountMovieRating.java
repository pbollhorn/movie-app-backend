package dat.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_movie_rating")
public class AccountMovieRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Movie movie;

    @Setter
    @Column(nullable = false)
    private Boolean rating;

}