package dat.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Rating.RatingId.class)
public class Rating {

    // This is the composite primary key
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RatingId implements Serializable {
        private Integer account;
        private Integer movie;
    }

    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account account;

    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    private Movie movie;

    @Setter
    @Column(nullable = false)
    private Boolean rating;

}