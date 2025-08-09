package com.moviebookingapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tickets")
@EntityListeners(AuditingEntityListener.class)
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "movie_name", nullable = false)
    private String movieName;

    @NotBlank
    @Column(name = "theatre_name", nullable = false)
    private String theatreName;

    @Min(1)
    @Column(name = "num_tickets", nullable = false)
    private int numberOfTickets;

    @NotEmpty
    @ElementCollection
    @CollectionTable(name = "ticket_seats", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "seat_number")
    private List<String> seatNumbers;

    @NotBlank
    @Column(name = "user_login_id", nullable = false)
    private String userLoginId;

    @CreatedDate
    @Column(name = "booked_at", updatable = false)
    private Instant bookedAt;
} 