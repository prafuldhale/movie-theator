package com.moviebookingapp.service;

import com.moviebookingapp.config.AppConstants;
import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final MovieRepository movieRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public Ticket bookTicket(Ticket ticket) {
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
} 