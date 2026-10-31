package com.medibook.controller;
import com.medibook.dto.PatientDtos.*;import com.medibook.entity.AppUser;import com.medibook.repository.*;import jakarta.validation.Valid;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/patients/me") @PreAuthorize("hasRole('PATIENT')") public class PatientController {
 private final UserRepository users;private final PatientProfileRepository profiles;
 public PatientController(UserRepository users,PatientProfileRepository profiles){this.users=users;this.profiles=profiles;}
 @GetMapping public Profile get(@AuthenticationPrincipal Long id){AppUser u=users.findById(id).orElseThrow();var p=profiles.findById(id).orElseThrow();return new Profile(u.getFullName(),u.getEmail(),p.getPhone(),p.getLocation());}
 @PutMapping @Transactional public Profile update(@AuthenticationPrincipal Long id,@Valid @RequestBody Update in){AppUser u=users.findById(id).orElseThrow();var p=profiles.findById(id).orElseThrow();u.setFullName(in.fullName().trim());p.setPhone(in.phone());p.setLocation(in.location());return new Profile(u.getFullName(),u.getEmail(),p.getPhone(),p.getLocation());}
}
