package com.medibook.dto;
import jakarta.validation.constraints.*;import java.math.BigDecimal;
public final class AdminDtos {private AdminDtos(){} public record CreateDoctor(@NotBlank @Size(max=80) String fullName,@Email @NotBlank String email,@NotBlank @Size(min=10,max=72) String password,@NotBlank String specialty,@NotBlank String qualifications,@Min(0) int experienceYears,@NotNull @DecimalMin("0.00") BigDecimal fee,@NotBlank String location,@Size(max=1000) String bio,boolean verified){} public record SpecialtyInput(@NotBlank @Size(max=80) String name){} }
