package com.medibook.controller;
import com.medibook.repository.SpecialtyRepository;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/specialties") public class SpecialtyController {private final SpecialtyRepository specialties;public SpecialtyController(SpecialtyRepository s){specialties=s;}@GetMapping public List<Map<String,Object>> all(){return specialties.findAll().stream().map(s->Map.<String,Object>of("id",s.getId(),"name",s.getName())).toList();}}
