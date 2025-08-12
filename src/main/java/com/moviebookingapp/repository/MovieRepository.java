package com.moviebookingapp.repository;

import com.moviebookingapp.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByMovieNameContainingIgnoreCase(String movieName);
    Optional<Movie> findByMovieNameAndTheatreName(String movieName, String theatreName);
    long deleteByMovieNameAndTheatreName(String movieName, String theatreName);
} 