package dat.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MovieGenre.MovieGenreId.class)
@Table(name = "movie_genre")
public class MovieGenre {

    // This is the composite primary key
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MovieGenreId implements Serializable {
        private Integer movie;
        private Integer genre;
    }

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


}
