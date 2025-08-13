package com.moviebookingapp.service;

import com.moviebookingapp.config.AppConstants;
import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final MovieRepository movieRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public Ticket bookTicket(Ticket ticket) {
        log.debug("Starting ticket booking process for movie: {} at theatre: {} by user: {}", 
                ticket.getMovieName(), ticket.getTheatreName(), ticket.getUserLoginId());
        log.debug("Ticket details - numberOfTickets: {}, seatNumbers: {}", 
                ticket.getNumberOfTickets(), ticket.getSeatNumbers());
        
        try {
            log.debug("Validating ticket data");
            validateTicket(ticket);
            
            log.debug("Finding movie: {} at theatre: {}", ticket.getMovieName(), ticket.getTheatreName());
            Movie movie = movieRepository.findByMovieNameAndTheatreName(ticket.getMovieName(), ticket.getTheatreName())
                    .orElseThrow(() -> {
                        log.warn("Movie/Theatre not found for ticket booking - movie: {}, theatre: {}", 
                                ticket.getMovieName(), ticket.getTheatreName());
                        return new IllegalArgumentException("Movie/Theatre not found");
                    });
            
            log.debug("Calculating already booked tickets for movie: {} at theatre: {}", 
                    ticket.getMovieName(), ticket.getTheatreName());
            Long alreadyBookedLong = ticketRepository.totalBookedForMovieAndTheatre(ticket.getMovieName(), ticket.getTheatreName());
            int alreadyBooked = alreadyBookedLong == null ? 0 : alreadyBookedLong.intValue();
            
            log.debug("Ticket availability check - totalTickets: {}, alreadyBooked: {}, requested: {}, available: {}", 
                    movie.getTotalTickets(), alreadyBooked, ticket.getNumberOfTickets(), 
                    movie.getTotalTickets() - alreadyBooked);
            
            if (alreadyBooked + ticket.getNumberOfTickets() > movie.getTotalTickets()) {
                log.warn("Insufficient tickets available - requested: {}, available: {}", 
                        ticket.getNumberOfTickets(), movie.getTotalTickets() - alreadyBooked);
                throw new IllegalArgumentException("Not enough tickets available");
            }
            
            log.debug("Saving ticket to database");
            Ticket saved = ticketRepository.save(ticket);
            
            log.debug("Sending ticket booking message to Kafka");
            String kafkaMessage = ticket.getMovieName() + "|" + ticket.getTheatreName() + "|" + ticket.getNumberOfTickets();
            kafkaTemplate.send(AppConstants.KAFKA_TOPIC_TICKETS, kafkaMessage);
            
            log.info("Ticket booked successfully - id: {}, movie: {}, theatre: {}, user: {}, tickets: {}", 
                    saved.getId(), ticket.getMovieName(), ticket.getTheatreName(), 
                    ticket.getUserLoginId(), ticket.getNumberOfTickets());
            
            return saved;
        } catch (Exception e) {
            log.error("Ticket booking failed for movie: {} at theatre: {} by user: {}, error: {}", 
                     ticket.getMovieName(), ticket.getTheatreName(), ticket.getUserLoginId(), e.getMessage(), e);
            throw e;
        }
    }

    private void validateTicket(Ticket ticket) {
        log.debug("Validating ticket - numberOfTickets: {}, seatNumbers: {}", 
                ticket.getNumberOfTickets(), ticket.getSeatNumbers());
        
        if (ticket.getNumberOfTickets() <= 0) {
            log.warn("Invalid number of tickets: {}", ticket.getNumberOfTickets());
            throw new IllegalArgumentException("Number of tickets must be positive");
        }
        
        if (ticket.getSeatNumbers() == null || ticket.getSeatNumbers().isEmpty()) {
            log.warn("No seat numbers provided for ticket booking");
            throw new IllegalArgumentException("Seat numbers must be provided");
        }
        
        if (ticket.getSeatNumbers().size() != ticket.getNumberOfTickets()) {
            log.warn("Seat numbers mismatch - numberOfTickets: {}, seatNumbersCount: {}", 
                    ticket.getNumberOfTickets(), ticket.getSeatNumbers().size());
            throw new IllegalArgumentException("Number of seat numbers must match number of tickets");
        }
        
        if (new HashSet<>(ticket.getSeatNumbers()).size() != ticket.getSeatNumbers().size()) {
            log.warn("Duplicate seat numbers detected: {}", ticket.getSeatNumbers());
            throw new IllegalArgumentException("Duplicate seat numbers are not allowed");
        }
        
        log.debug("Ticket validation passed successfully");
    }
}
