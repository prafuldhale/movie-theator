package com.moviebookingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequestDTO {
    @NotBlank(message = "Theatre name is required")
    private String theatreName;

    @Positive(message = "Number of tickets must be positive")
    private int numberOfTickets;

    @NotEmpty(message = "Seat numbers must be provided")
    private List<String> seatNumbers;

    @NotBlank(message = "User login ID is required")
    private String userLoginId;
}
