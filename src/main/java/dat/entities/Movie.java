package dat.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

import dat.dto.TmdbMovieDto;

@ToString
@Getter
@NoArgsConstructor
@Entity
public class Movie {

    private static final int MINIMUM_VOTES_FOR_RATING = 10;

    @Id
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String originalTitle;
    private Boolean adult;
    private String originalLanguage;
    private Double voteAverage;
    private Integer voteCount;
    private Double rating;
    private LocalDate releaseDate;
    private String backdropPath;
    private String posterPath;

    @ToString.Exclude
    @Column(length = 1000)
    private String overview;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<MovieGenre> genres = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "movie", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Credit> credits = new HashSet<>();


    public Movie(TmdbMovieDto m, List<Genre> genreList) {
        this.id = m.id();
        this.title = m.title();
        this.originalTitle = m.originalTitle();
        this.adult = m.adult();
        this.originalLanguage = m.originalLanguage();
        this.voteAverage = m.voteAverage();
        this.voteCount = m.voteCount();
        this.releaseDate = m.releaseDate();
        this.backdropPath = m.backdropPath();
        this.posterPath = m.posterPath();
        this.overview = m.overview();

        if (m.voteCount() >= MINIMUM_VOTES_FOR_RATING) {
            this.rating = m.voteAverage();
        } else {
            this.rating = null;
        }

        int rankInMovie = 0;
        for (Genre g : genreList) {
            genres.add(new MovieGenre(null, this, g, rankInMovie));
            rankInMovie++;
        }

    }


    public void addCredit(Person person, String job, String character, Integer rankInMovie) {
        credits.add(new Credit(null, this, person, job, character, rankInMovie));
    }

}