package com.medibook.service;

import com.medibook.dto.AppointmentDtos.Booking;
import com.medibook.entity.*;
import com.medibook.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {
    AppointmentRepository appointments;
    DoctorProfileRepository doctors;
    AvailabilityRepository availability;
    NotificationRepository notifications;
    DoctorService doctorService;
    AppointmentService service;
    ZoneId zone=ZoneId.of("Asia/Kolkata");

    @BeforeEach void setUp(){
        appointments=mock(AppointmentRepository.class);doctors=mock(DoctorProfileRepository.class);
        availability=mock(AvailabilityRepository.class);notifications=mock(NotificationRepository.class);
        var users=mock(UserRepository.class);doctorService=mock(DoctorService.class);
        service=new AppointmentService(appointments,doctors,availability,notifications,users,doctorService,mock(EmailNotifier.class),24,"Asia/Kolkata");
    }

    @Test void bookingCreatesConfirmedAppointmentAndNotifications(){
        LocalDate day=LocalDate.now(zone).plusDays(3);Instant start=day.atTime(10,0).atZone(zone).toInstant();
        DoctorProfile doctor=mock(DoctorProfile.class);AppUser docUser=mock(AppUser.class);
        when(doctor.isVerified()).thenReturn(true);when(doctor.getUser()).thenReturn(docUser);when(docUser.isActive()).thenReturn(true);when(doctor.isManualConfirmation()).thenReturn(false);
        when(docUser.getFullName()).thenReturn("Dr. Demo");
        when(doctors.lockById(7L)).thenReturn(Optional.of(doctor));
        DoctorAvailability schedule=new DoctorAvailability(7L,day.getDayOfWeek(),LocalTime.of(9,0),LocalTime.of(12,0),30,null,null);
        when(availability.findByDoctorIdAndDayOfWeek(7L,day.getDayOfWeek())).thenReturn(List.of(schedule));
        when(appointments.findConflicts(eq(7L),any(),any())).thenReturn(List.of());
        when(appointments.save(any())).thenAnswer(i->i.getArgument(0));

        Appointment result=service.book(2L,new Booking(7L,start,"Annual check-up"));

        assertEquals(AppointmentStatus.CONFIRMED,result.getStatus());
        assertEquals(start.plusSeconds(1800),result.getEndAt());
        verify(appointments).save(any(Appointment.class));
        verify(notifications,times(2)).save(any(Notification.class));
    }

    @Test void bookingRejectsAlreadyOccupiedSlot(){
        LocalDate day=LocalDate.now(zone).plusDays(3);Instant start=day.atTime(10,0).atZone(zone).toInstant();
        DoctorProfile doctor=mock(DoctorProfile.class);AppUser docUser=mock(AppUser.class);when(doctor.isVerified()).thenReturn(true);when(doctor.getUser()).thenReturn(docUser);when(docUser.isActive()).thenReturn(true);
        when(doctors.lockById(7L)).thenReturn(Optional.of(doctor));
        when(availability.findByDoctorIdAndDayOfWeek(7L,day.getDayOfWeek())).thenReturn(List.of(new DoctorAvailability(7L,day.getDayOfWeek(),LocalTime.of(9,0),LocalTime.of(12,0),30,null,null)));
        when(appointments.findConflicts(eq(7L),any(),any())).thenReturn(List.of(mock(Appointment.class)));
        assertThrows(ResponseStatusException.class,()->service.book(2L,new Booking(7L,start,null)));
        verify(appointments,never()).save(any());
    }

    @Test void cannotCancelAnotherPatientsAppointment(){
        Appointment appointment=mock(Appointment.class);when(appointment.getPatientId()).thenReturn(88L);
        when(appointments.findById(9L)).thenReturn(Optional.of(appointment));
        ResponseStatusException error=assertThrows(ResponseStatusException.class,()->service.cancel(9L,2L));
        assertEquals(403,error.getStatusCode().value());
    }

    @Test void patientCanCancelConfirmedVisitOutsideNoticeWindow(){
        Appointment appointment=mock(Appointment.class);when(appointment.getPatientId()).thenReturn(2L);when(appointment.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);when(appointment.getStartAt()).thenReturn(Instant.now().plus(Duration.ofDays(3)));when(appointment.getDoctorId()).thenReturn(7L);
        when(appointments.findById(9L)).thenReturn(Optional.of(appointment));
        assertSame(appointment,service.cancel(9L,2L));verify(appointment).setStatus(AppointmentStatus.CANCELLED);verify(notifications,times(2)).save(any(Notification.class));
    }

    @Test void rejectsInvalidStatusTransition(){
        Appointment appointment=mock(Appointment.class);when(appointment.getDoctorId()).thenReturn(7L);when(appointment.getStatus()).thenReturn(AppointmentStatus.CANCELLED);
        when(appointments.findById(9L)).thenReturn(Optional.of(appointment));
        assertThrows(ResponseStatusException.class,()->service.changeStatus(9L,7L,"COMPLETED"));
        verify(notifications,never()).save(any());
    }

    @Test void rescheduleRejectsNewAppointmentInsideNoticeWindow(){
        Appointment appointment=mock(Appointment.class);
        when(appointment.getPatientId()).thenReturn(2L);
        when(appointment.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
        when(appointment.getStartAt()).thenReturn(Instant.now().plus(Duration.ofDays(3)));
        when(appointment.getDoctorId()).thenReturn(7L);
        when(appointments.findById(9L)).thenReturn(Optional.of(appointment));

        Instant tooSoon=Instant.now().plus(Duration.ofHours(2));
        ResponseStatusException error=assertThrows(ResponseStatusException.class,
            ()->service.reschedule(9L,2L,new com.medibook.dto.AppointmentDtos.Reschedule(tooSoon)));

        assertEquals(409,error.getStatusCode().value());
        verify(appointment,never()).setStartAt(any());
        verify(notifications,never()).save(any());
    }
}
