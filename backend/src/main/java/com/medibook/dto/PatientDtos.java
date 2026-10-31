package com.medibook.dto;
import jakarta.validation.constraints.*;
public final class PatientDtos {private PatientDtos(){} public record Profile(String fullName,String email,String phone,String location){} public record Update(@NotBlank @Size(max=80) String fullName,@Size(max=30) String phone,@Size(max=120) String location){}}
