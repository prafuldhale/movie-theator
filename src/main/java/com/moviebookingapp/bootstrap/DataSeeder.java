package com.moviebookingapp.bootstrap;

import com.moviebookingapp.config.AppConstants;
import com.moviebookingapp.domain.Movie;
import com.moviebookingapp.domain.User;
import com.moviebookingapp.repository.MovieRepository;
import com.moviebookingapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        seedMovies();
        seedAdmin();
    }

    private void seedMovies() {
        if (movieRepository.findAll().isEmpty()) {
            movieRepository.save(Movie.builder().movieName("Inception").theatreName("Grand Cinema").totalTickets(100).status("BOOK ASAP").build());
            movieRepository.save(Movie.builder().movieName("Inception").theatreName("Cityplex").totalTickets(120).status("BOOK ASAP").build());
            movieRepository.save(Movie.builder().movieName("Interstellar").theatreName("Grand Cinema").totalTickets(90).status("BOOK ASAP").build());
            movieRepository.save(Movie.builder().movieName("Interstellar").theatreName("Cityplex").totalTickets(110).status("BOOK ASAP").build());
        }
    }

    private void seedAdmin() {
        userRepository.findByLoginId(AppConstants.DEFAULT_ADMIN_LOGIN).orElseGet(() ->
                userRepository.save(User.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .email(AppConstants.DEFAULT_ADMIN_EMAIL)
                        .loginId(AppConstants.DEFAULT_ADMIN_LOGIN)
                        .passwordHash(encoder.encode(System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "ChangeMe123!")))
                        .contactNumber("0000000000")
                        .build())
        );
    }
}
