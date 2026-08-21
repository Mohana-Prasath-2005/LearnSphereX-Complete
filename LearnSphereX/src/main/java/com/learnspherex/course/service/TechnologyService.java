package com.learnspherex.course.service;

import com.learnspherex.course.dto.TechnologyDTO;
import com.learnspherex.course.dto.TechnologyRequestDTO;

import java.util.List;

public interface TechnologyService {

    TechnologyDTO createTechnology(TechnologyRequestDTO request);

    TechnologyDTO getTechnologyById(Long id);

    List<TechnologyDTO> getAllTechnologies();

    TechnologyDTO updateTechnology(Long id, TechnologyRequestDTO request);

    void deleteTechnology(Long id);
}