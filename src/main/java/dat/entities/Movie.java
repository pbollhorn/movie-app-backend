package dat.entities;

import java.time.LocalDate;
import java.util.Comparator;
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

    @Id
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String originalTitle;
    private Boolean adult;
    private Boolean video;
    private String originalLanguage;
    private Double voteAverage;
    private Integer voteCount;
    private LocalDate releaseDate;
    private String backdropPath;
    private String posterPath;
    private Integer runtime;
    private String tagline;
    private String status;

    @ToString.Exclude
    @Column(length = 1000)
    private String overview;

    @ToString.Exclude
    @OneToMany(mappedBy = "movie", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private Set<MovieGenre> movieGenres = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "movie", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Credit> credits = new HashSet<>();

    @ManyToOne(cascade = CascadeType.MERGE)
    private Collection collection;


    public Movie(TmdbMovieDto m, List<Genre> genresForThisMovie) {
        this.id = m.id();
        this.title = m.title();
        this.originalTitle = m.originalTitle();
        this.adult = m.adult();
        this.video = m.video();
        this.originalLanguage = m.originalLanguage();
        this.voteAverage = m.voteAverage();
        this.voteCount = m.voteCount();
        this.releaseDate = m.releaseDate();
        this.backdropPath = m.backdropPath();
        this.posterPath = m.posterPath();
        this.overview = m.overview();
        this.voteAverage = m.voteAverage();
        this.runtime = m.runtime();
        this.tagline = m.tagline();
        this.status = m.status();

        int rankInMovie = 0;
        for (Genre g : genresForThisMovie) {
            movieGenres.add(new MovieGenre(null, this, g, rankInMovie));
            rankInMovie++;
        }

        if (m.collection() != null) {
            this.collection = new Collection(m.collection().id(), m.collection().name());

        }

    }


    public void addCredit(String creditId, Person person, String job, String department, String character, Integer rankInMovie) {
        credits.add(new Credit(creditId, this, person, job, department, character, rankInMovie));
    }

    public String[] getGenresAsStringArray() {
        String[] genreArray = this.movieGenres.stream()
                .sorted(Comparator.comparingInt(MovieGenre::getRankInMovie))
                .map(mg -> mg.getGenre().getName())
                .toArray(String[]::new);
        return genreArray;
    }

}