package com.moviebookingapp.messaging;

import com.moviebookingapp.config.AppConstants;
import com.moviebookingapp.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagingService {
    private final MovieService movieService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_TICKETS, groupId = "moviebooking-admin")
    public void onTicketBooked(String payload) {
        // payload format: movie|theatre|count
        String[] parts = payload.split("\\|");
        if (parts.length >= 2) {
            String movieName = parts[0];
            String theatreName = parts[1];
            String status = movieService.computeAndUpdateStatus(movieName, theatreName);
            kafkaTemplate.send(AppConstants.KAFKA_TOPIC_STATUS, movieName + "|" + theatreName + "|" + status);
        }
    }
} 