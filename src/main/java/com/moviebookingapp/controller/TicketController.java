package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.dto.TicketRequestDTO;
import com.moviebookingapp.service.MovieService;
import com.moviebookingapp.service.TicketService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/moviebooking")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;
    private final MovieService movieService;

    @PostMapping("/{moviename}/add")
    public ResponseEntity<Ticket> add(@PathVariable("moviename") String moviename,
                                      @Valid @RequestBody TicketRequestDTO request) {
        log.info("Ticket booking request for movie: {} at theatre: {} by user: {}", 
                moviename, request.getTheatreName(), request.getUserLoginId());
        log.debug("Ticket details - numberOfTickets: {}, seatNumbers: {}", 
                 request.getNumberOfTickets(), request.getSeatNumbers());
        
        try {
            Ticket ticket = Ticket.builder()
                    .movieName(moviename)
                    .theatreName(request.getTheatreName())
                    .numberOfTickets(request.getNumberOfTickets())
                    .seatNumbers(request.getSeatNumbers())
                    .userLoginId(request.getUserLoginId())
                    .build();
            
            Ticket savedTicket = ticketService.bookTicket(ticket);
            log.info("Ticket booked successfully - id: {}, movie: {}, theatre: {}, user: {}, tickets: {}", 
                    savedTicket.getId(), moviename, request.getTheatreName(), 
                    request.getUserLoginId(), request.getNumberOfTickets());
            return ResponseEntity.ok(savedTicket);
        } catch (Exception e) {
            log.error("Ticket booking failed for movie: {} at theatre: {} by user: {}, error: {}", 
                     moviename, request.getTheatreName(), request.getUserLoginId(), e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{moviename}/update/{ticket}")
    public ResponseEntity<String> updateStatus(@PathVariable("moviename") String moviename,
                                               @PathVariable("ticket") String theatreName) {
        log.info("Request to update status for movie: {} at theatre: {}", moviename, theatreName);
        
        try {
            String status = movieService.computeAndUpdateStatus(moviename, theatreName);
            log.info("Status updated successfully for movie: {} at theatre: {}, new status: {}", 
                    moviename, theatreName, status);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Status update failed for movie: {} at theatre: {}, error: {}", 
                     moviename, theatreName, e.getMessage(), e);
            throw e;
        }
    }

}
