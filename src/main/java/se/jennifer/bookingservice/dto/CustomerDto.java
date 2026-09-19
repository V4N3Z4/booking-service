package se.jennifer.bookingservice.dto;

public record CustomerDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {}

