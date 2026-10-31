package com.medibook.dto;
import jakarta.validation.constraints.*; import java.time.Instant;
public final class AppointmentDtos { private AppointmentDtos(){}
 public record Booking(@NotNull Long doctorId,@NotNull Instant startAt,@Size(max=500) String reason){}
 public record Reschedule(@NotNull Instant startAt){}
 public record StatusChange(@NotBlank String status){}
 public record Slot(Instant startAt,Instant endAt){}
 public record AppointmentView(long id,long patientId,String patientName,long doctorId,String doctorName,Instant startAt,Instant endAt,String status,String reason){}
}
