package com.moviebookingapp.controller;

import com.moviebookingapp.domain.Ticket;
import com.moviebookingapp.dto.TicketRequestDTO;
import com.moviebookingapp.service.MovieService;
import com.moviebookingapp.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/moviebooking")

public class TicketController {

    @Autowired
    private final TicketService ticketService;
    @Autowired
    private final MovieService movieService;

    @Autowired
    public TicketController(TicketService ticketService, MovieService movieService) {
        this.ticketService = ticketService;
        this.movieService = movieService;
    }
    @PostMapping("/{moviename}/add")
    public ResponseEntity<Ticket> add(@PathVariable("moviename") String moviename,
                                      @Valid @RequestBody TicketRequestDTO request) {
        Ticket ticket = Ticket.builder()
                .movieName(moviename)
                .theatreName(request.getTheatreName())
                .numberOfTickets(request.getNumberOfTickets())
                .seatNumbers(request.getSeatNumbers())
                .userLoginId(request.getUserLoginId())
                .build();
        return ResponseEntity.ok(ticketService.bookTicket(ticket));
    }

    @PutMapping("/{moviename}/update/{ticket}")
    public ResponseEntity<String> updateStatus(@PathVariable("moviename") String moviename,
                                               @PathVariable("ticket") String theatreName) {
        String status = movieService.computeAndUpdateStatus(moviename, theatreName);
        return ResponseEntity.ok(status);
    }
}
