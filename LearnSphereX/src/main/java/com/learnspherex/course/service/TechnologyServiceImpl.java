package com.learnspherex.course.service;

import com.learnspherex.course.dto.TechnologyDTO;
import com.learnspherex.course.dto.TechnologyRequestDTO;
import com.learnspherex.course.entity.Technology;
import com.learnspherex.course.mapper.CourseMapper;
import com.learnspherex.course.repository.TechnologyRepository;
import com.learnspherex.course.service.TechnologyService;
import com.learnspherex.exception.DuplicateResourceException;
import com.learnspherex.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnologyServiceImpl implements TechnologyService {

    private final TechnologyRepository technologyRepository;
    private final CourseMapper courseMapper;

    @Override
    public TechnologyDTO createTechnology(TechnologyRequestDTO request) {
        if (technologyRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Technology with name '" + request.getName() + "' already exists");
        }

        Technology technology = Technology.builder()
                .name(request.getName())
                .version(request.getVersion())
                .description(request.getDescription())
                .build();

        Technology saved = technologyRepository.save(technology);
        return courseMapper.toTechnologyDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TechnologyDTO getTechnologyById(Long id) {
        Technology technology = technologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technology not found with id: " + id));
        return courseMapper.toTechnologyDTO(technology);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnologyDTO> getAllTechnologies() {
        return technologyRepository.findAll().stream()
                .map(courseMapper::toTechnologyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TechnologyDTO updateTechnology(Long id, TechnologyRequestDTO request) {
        Technology technology = technologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technology not found with id: " + id));

        if (!technology.getName().equalsIgnoreCase(request.getName()) &&
                technologyRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Technology with name '" + request.getName() + "' already exists");
        }

        technology.setName(request.getName());
        technology.setVersion(request.getVersion());
        technology.setDescription(request.getDescription());

        Technology updated = technologyRepository.save(technology);
        return courseMapper.toTechnologyDTO(updated);
    }

    @Override
    public void deleteTechnology(Long id) {
        if (!technologyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Technology not found with id: " + id);
        }
        technologyRepository.deleteById(id);
    }
}