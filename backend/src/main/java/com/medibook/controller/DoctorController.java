package com.medibook.controller;

import com.medibook.dto.DoctorDtos.*;
import com.medibook.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService service;
    public DoctorController(DoctorService service){this.service=service;}

    @GetMapping public Object search(@RequestParam(required=false) String specialty,
        @RequestParam(required=false) String location,@RequestParam(required=false) BigDecimal maxFee,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate availableDate,
        @RequestParam(defaultValue="-1") int page,@RequestParam(defaultValue="12") int size,
        @RequestParam(defaultValue="name") String sortBy,@RequestParam(defaultValue="asc") String direction){
        List<DoctorView> results=new ArrayList<>(service.search(specialty,location,maxFee));
        if(availableDate!=null)results.removeIf(d->service.slots(d.id(),availableDate).isEmpty());
        if(page<0)return results;
        if(page>10000||size<1||size>50)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Page must be 0–10000 and size 1–50");
        Comparator<DoctorView> comparator=switch(sortBy){
            case "fee"->Comparator.comparing(DoctorView::fee,Comparator.nullsLast(BigDecimal::compareTo));
            case "experience"->Comparator.comparing(DoctorView::experienceYears,Comparator.nullsLast(Integer::compareTo));
            default->Comparator.comparing(DoctorView::name,String.CASE_INSENSITIVE_ORDER);
        };
        if(direction.equalsIgnoreCase("desc"))comparator=comparator.reversed();
        results.sort(comparator);
        int from=Math.min(page*size,results.size()),to=Math.min(from+size,results.size());
        return Map.of("content",results.subList(from,to),"page",page,"size",size,"totalElements",results.size(),"totalPages",(results.size()+size-1)/size);
    }
    @GetMapping("/{id}") public DoctorView get(@PathVariable long id){return service.get(id);}
    @GetMapping("/{id}/availability") public Object availability(@PathVariable long id,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){return date==null?service.availability(id):service.slots(id,date);}
    @PutMapping("/me/profile") @PreAuthorize("hasRole('DOCTOR')") public DoctorView profile(@org.springframework.security.core.annotation.AuthenticationPrincipal Long id,@Valid @RequestBody ProfileUpdate body){return service.updateProfile(id,body);}
    @PutMapping("/me/availability") @PreAuthorize("hasRole('DOCTOR')") public Object availability(@org.springframework.security.core.annotation.AuthenticationPrincipal Long id,@Valid @RequestBody AvailabilityUpdate body){return service.setAvailability(id,body.schedules());}
}
