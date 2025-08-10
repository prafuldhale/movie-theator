package com.moviebookingapp.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApiEndpointsTest {

    @LocalServerPort
    private int port;

    private String authToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v1.0/moviebooking";
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put("firstName", "John");
        userRequest.put("lastName", "Doe");
        userRequest.put("email", "john.doe@example.com");
        userRequest.put("loginId", "johndoe");
        userRequest.put("password", "Password123!");
        userRequest.put("confirmPassword", "Password123!");
        userRequest.put("contactNumber", "1234567890");

        given()
            .contentType(ContentType.JSON)
            .body(userRequest)
        .when()
            .post("/register")
        .then()
            .statusCode(201)
            .body("loginId", equalTo("johndoe"));
    }

    @Test
    @Order(2)
    void testUserLogin() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("loginId", "johndoe");
        loginRequest.put("password", "Password123!");

        authToken = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    }

    @Test
    @Order(3)
    void testAddMovie() {
        Map<String, Object> movieRequest = new HashMap<>();
        movieRequest.put("movieName", "Inception");
        movieRequest.put("theatreName", "PVR");
        movieRequest.put("totalTickets", 100);
        movieRequest.put("status", "BOOK ASAP");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(movieRequest)
        .when()
            .post("/movies/add")
        .then()
            .statusCode(201)
            .body("movieName", equalTo("Inception"))
            .body("totalTickets", equalTo(100));
    }

    @Test
    @Order(4)
    void testGetAllMovies() {
        given()
        .when()
            .get("/all")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].movieName", notNullValue());
    }

    @Test
    @Order(5)
    void testSearchMovies() {
        given()
        .when()
            .get("/movies/search/{moviename}", "Inception")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].movieName", equalTo("Inception"));
    }

    @Test
    @Order(6)
    void testBookTickets() {
        Map<String, Object> bookingRequest = new HashMap<>();
        bookingRequest.put("theatreName", "PVR");
        bookingRequest.put("numberOfTickets", 2);
        bookingRequest.put("seatNumbers", Arrays.asList("A1", "A2"));
        bookingRequest.put("userLoginId", "johndoe");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body(bookingRequest)
        .when()
            .post("/{moviename}/add", "Inception")
        .then()
            .statusCode(200)
            .body("movieName", equalTo("Inception"))
            .body("numberOfTickets", equalTo(2));
    }

    @Test
    @Order(7)
    void testGetBookedTicketsInfo() {
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/{moviename}/booked/{theatre}", "Inception", "PVR")
        .then()
            .statusCode(200)
            .body("booked", greaterThan(0));
    }

    @Test
    @Order(8)
    void testUpdateTickets() {
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .patch("/{moviename}/theatres/{theatre}/tickets?total=80", "Inception", "PVR")
        .then()
            .statusCode(200)
            .body("totalTickets", equalTo(80));
    }

    @Test
    @Order(9)
    void testPasswordReset() {
        Map<String, String> resetRequest = new HashMap<>();
        resetRequest.put("loginId", "johndoe");
        resetRequest.put("password", "NewPassword123!");
        resetRequest.put("confirmPassword", "NewPassword123!");

        given()
            .contentType(ContentType.JSON)
            .body(resetRequest)
        .when()
            .put("/forgot")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(10)
    void testDeleteMovie() {
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete("/{moviename}/delete/{theatre}", "Inception", "PVR")
        .then()
            .statusCode(200);
    }

}

