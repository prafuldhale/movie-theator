package com.moviebookingapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "movies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_movie_theatre", columnNames = {"movie_name", "theatre_name"})
})
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "movie_name", nullable = false)
    private String movieName;

    @NotBlank
    @Column(name = "theatre_name", nullable = false)
    private String theatreName;

    @Min(0)
    @Column(name = "total_tickets", nullable = false)
    private int totalTickets;

    @Column(name = "status")
    private String status; // SOLD OUT / BOOK ASAP
} 