package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.service.MovieService;
import com.moviebookingapp.dto.BookedInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final MovieService movieService;

    @GetMapping("/{moviename}/booked/{theatre}")
    public ResponseEntity<BookedInfoDTO> booked(@PathVariable("moviename") String moviename,
                                             @PathVariable("theatre") String theatre) {
        log.info("Admin request for booked info - movie: {}, theatre: {}", moviename, theatre);

        try {
            int booked = movieService.bookedCount(moviename, theatre);
            log.debug("Booked count for movie: {} at theatre: {} is {}", moviename, theatre, booked);

            Movie movie = movieService.searchMovies(moviename).stream()
                    .filter(m -> m.getTheatreName().equalsIgnoreCase(theatre))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Movie not found"));

            int remaining = movie.getTotalTickets() - booked;
            String status = remaining <= 0 ? "SOLD OUT" : "BOOK ASAP";

            BookedInfoDTO bookedInfo = new BookedInfoDTO(booked, Math.max(remaining, 0), status);

            log.info("Booked info retrieved successfully - movie: {}, theatre: {}, booked: {}, remaining: {}, status: {}",
                    moviename, theatre, booked, remaining, status);

            return ResponseEntity.ok(bookedInfo);
        } catch (Exception e) {
            log.error("Error retrieving booked info for movie: {} at theatre: {}, error: {}",
                     moviename, theatre, e.getMessage(), e);
            throw e;
        }
    }
}
