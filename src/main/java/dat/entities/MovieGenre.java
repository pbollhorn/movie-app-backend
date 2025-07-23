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

    // This is the composite primary key
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MovieGenreId implements Serializable {
        private Integer movie;
        private Integer genre;
    }


}
