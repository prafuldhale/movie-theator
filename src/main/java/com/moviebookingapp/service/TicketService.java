package com.moviebookingapp.service;

import com.moviebookingapp.config.AppConstants;
import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final MovieRepository movieRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public Ticket bookTicket(Ticket ticket) {
        validateTicket(ticket);
        Movie movie = movieRepository.findByMovieNameAndTheatreName(ticket.getMovieName(), ticket.getTheatreName())
                .orElseThrow(() -> new IllegalArgumentException("Movie/Theatre not found"));
        Long alreadyBookedLong = ticketRepository.totalBookedForMovieAndTheatre(ticket.getMovieName(), ticket.getTheatreName());
        int alreadyBooked = alreadyBookedLong == null ? 0 : alreadyBookedLong.intValue();
        if (alreadyBooked + ticket.getNumberOfTickets() > movie.getTotalTickets()) {
            throw new IllegalArgumentException("Not enough tickets available");
        }
        Ticket saved = ticketRepository.save(ticket);
        kafkaTemplate.send(AppConstants.KAFKA_TOPIC_TICKETS,
                ticket.getMovieName() + "|" + ticket.getTheatreName() + "|" + ticket.getNumberOfTickets());
        return saved;
    }

    private void validateTicket(Ticket ticket) {
        if (ticket.getNumberOfTickets() <= 0) {
            throw new IllegalArgumentException("Number of tickets must be positive");
        }
        if (ticket.getSeatNumbers() == null || ticket.getSeatNumbers().isEmpty()) {
            throw new IllegalArgumentException("Seat numbers must be provided");
        }
        if (ticket.getSeatNumbers().size() != ticket.getNumberOfTickets()) {
            throw new IllegalArgumentException("Number of seat numbers must match number of tickets");
        }
        if (new HashSet<>(ticket.getSeatNumbers()).size() != ticket.getSeatNumbers().size()) {
            throw new IllegalArgumentException("Duplicate seat numbers are not allowed");
        }
    }
}
