package com.learnspherex.course.controller;

import com.learnspherex.course.dto.TechnologyDTO;
import com.learnspherex.course.dto.TechnologyRequestDTO;
import com.learnspherex.course.service.TechnologyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/technologies")
@RequiredArgsConstructor
public class TechnologyController {

    private final TechnologyService service;

    @GetMapping
    public List<TechnologyDTO> all() {
        return service.getAllTechnologies();
    }

    @GetMapping("/{id}")
    public TechnologyDTO get(@PathVariable Long id) {
        return service.getTechnologyById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TechnologyDTO create(@Valid @RequestBody TechnologyRequestDTO request) {
        return service.createTechnology(request);
    }

    @PutMapping("/{id}")
    public TechnologyDTO update(@PathVariable Long id, @Valid @RequestBody TechnologyRequestDTO request) {
        return service.updateTechnology(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTechnology(id);
        return ResponseEntity.noContent().build();
    }
}
