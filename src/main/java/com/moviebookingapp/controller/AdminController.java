package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.service.MovieService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
public class AdminController {
    private final MovieService movieService;

    @GetMapping("/{moviename}/booked/{theatre}")
    public ResponseEntity<BookedInfo> booked(@PathVariable("moviename") String moviename,
                                             @PathVariable("theatre") String theatre) {
        int booked = movieService.bookedCount(moviename, theatre);
        Movie movie = movieService.searchMovies(moviename).stream()
                .filter(m -> m.getTheatreName().equalsIgnoreCase(theatre))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        int remaining = movie.getTotalTickets() - booked;
        String status = remaining <= 0 ? "SOLD OUT" : "BOOK ASAP";
        return ResponseEntity.ok(new BookedInfo(booked, Math.max(remaining, 0), status));
    }

    @Data
    @AllArgsConstructor
    static class BookedInfo {
        private int booked;
        private int remaining;
        private String status;
    }
} 