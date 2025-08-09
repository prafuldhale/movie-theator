package com.moviebookingapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = {"email"}),
        @UniqueConstraint(name = "uk_user_login", columnNames = {"login_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank
    @Column(name = "login_id", nullable = false)
    private String loginId;

    @NotBlank
    @Size(min = 8)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
} 