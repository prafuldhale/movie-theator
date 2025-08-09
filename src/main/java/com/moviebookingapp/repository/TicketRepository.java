package com.moviebookingapp.repository;

import com.moviebookingapp.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("select coalesce(sum(t.numberOfTickets), 0) from Ticket t where t.movieName = :movieName and t.theatreName = :theatreName")
    Long totalBookedForMovieAndTheatre(@Param("movieName") String movieName, @Param("theatreName") String theatreName);
} 