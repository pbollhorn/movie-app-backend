package dat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MovieGenre.MovieGenreId.class)
@Table(name = "movie_genre")
public class MovieGenre {

    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    private Genre genre;

    @Column(nullable = false)
    private Integer rankInMovie;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MovieGenreId implements Serializable {
        private Movie movie;
        private Genre genre;
    }


}
