package dat.entities;

import dat.dto.TmdbGenreDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Genre {
    @Id
    Integer id;

    @Column(nullable = false, unique = true)
    String name;

    // Calling "Science Fiction" for "Sci-Fi" is business logic in this app,
    // and therefore allowed to be here in this entity
    public Genre(TmdbGenreDto genreDto) {
        this.id = genreDto.id();

        if ("Science Fiction".equals(genreDto.name())) {
            this.name = "Sci-Fi";
        } else {
            this.name = genreDto.name();
        }

    }
}