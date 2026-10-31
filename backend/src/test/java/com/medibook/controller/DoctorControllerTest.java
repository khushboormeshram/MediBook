package com.medibook.controller;

import com.medibook.dto.DoctorDtos.DoctorView;
import com.medibook.service.DoctorService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorControllerTest {
    @Test
    void defaultDirectoryResponseIsAnUnpagedList() {
        DoctorService service = mock(DoctorService.class);
        DoctorView doctor = new DoctorView(3, "Dr. Meera", "Cardiology", "MBBS", 12,
                BigDecimal.valueOf(900), "Bengaluru", "Profile", true, false);
        when(service.search(null, null, null)).thenReturn(List.of(doctor));

        Object response = new DoctorController(service).search(null, null, null, null,
                -1, 12, "name", "asc");

        assertEquals(List.of(doctor), response);
    }

    @Test
    void explicitPaginationReturnsContentThatFrontendCanNormalize() {
        DoctorService service = mock(DoctorService.class);
        DoctorView doctor = new DoctorView(3, "Dr. Meera", "Cardiology", "MBBS", 12,
                BigDecimal.valueOf(900), "Bengaluru", "Profile", true, false);
        when(service.search(null, null, null)).thenReturn(List.of(doctor));

        Object response = new DoctorController(service).search(null, null, null, null,
                0, 12, "name", "asc");

        assertInstanceOf(Map.class, response);
        assertEquals(List.of(doctor), ((Map<?, ?>) response).get("content"));
    }
}
