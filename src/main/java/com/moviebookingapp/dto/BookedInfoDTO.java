package com.moviebookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedInfoDTO {
    private int booked;
    private int remaining;
    private String status;
}
