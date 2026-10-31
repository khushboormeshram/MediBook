package com.medibook.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.*; import java.util.List;
public final class DoctorDtos { private DoctorDtos(){}
 public record DoctorView(long id,String name,String specialty,String qualifications,Integer experienceYears,BigDecimal fee,String location,String bio,boolean verified,boolean manualConfirmation){}
 public record AvailabilityInput(@NotNull DayOfWeek dayOfWeek,@NotNull LocalTime startTime,@NotNull LocalTime endTime,@Min(10) @Max(120) int slotMinutes,LocalTime breakStart,LocalTime breakEnd){}
 public record ProfileUpdate(@NotBlank String fullName,@NotBlank String specialty,@NotBlank String qualifications,@Min(0) int experienceYears,@DecimalMin("0.00") BigDecimal fee,@NotBlank String location,@Size(max=1000) String bio,boolean manualConfirmation){}
 public record AvailabilityUpdate(@NotNull List<@NotNull @Valid AvailabilityInput> schedules){}
}
