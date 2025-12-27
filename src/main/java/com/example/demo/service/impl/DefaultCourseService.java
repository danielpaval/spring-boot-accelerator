package com.example.demo.service.impl;

import com.example.common.service.AbstractCommonService;
import com.example.demo.dto.CourseCriteria;
import com.example.demo.dto.CourseDto;
import com.example.demo.dto.CoursePatchDto;
import com.example.demo.entity.Course;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.repository.CourseRepository;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.CourseService;
import com.example.demo.service.EnversRevisionService;
import io.github.perplexhub.rsql.RSQLJPASupport;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DefaultCourseService extends AbstractCommonService<Long, Course, CourseDto, CoursePatchDto> implements CourseService {

    private final CourseRepository courseRepository;

    private final CourseMapper courseMapper;

    private final EnversRevisionService enversRevisionService;

    public DefaultCourseService(CourseRepository courseRepository, CourseMapper courseMapper,
                                Validator validator, EnversRevisionService enversRevisionService) {
        super(courseRepository, courseMapper, validator);
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.enversRevisionService = enversRevisionService;
    }

    @Override
    public CourseDto save(CourseDto dto) {
        if (dto.getTeacherId() == null) {
            dto.setTeacherId(SecurityUtils.getUserId());
        }
        return super.save(dto);
    }

    @Override
    public Page<CourseDto> findByCriteria(CourseCriteria criteria, Pageable pageable) {
        return courseRepository
                .findAll(
                        RSQLJPASupport.toSpecification(
                                criteria.getQuery(), true
                        ),
                        pageable
                )
                .map(courseMapper::map);
    }

    @Override
    public CourseDto update(Long id, CourseDto dto) {
        if (dto.getTeacherId() == null) {
            dto.setTeacherId(SecurityUtils.getUserId());
        }
        return super.update(id, dto);
    }

    @Override
    public CourseDto patch(Long id, CoursePatchDto dto) {
        dto.getName().ifPresent(name -> {
            if (!SecurityUtils.isAdmin()) {
                throw new AccessDeniedException("Only administrators can update the course name.");
            }
        });
        return super.patch(id, dto);
    }

    // Audit method implementations
    @Override
    public Optional<Page<Revision<Integer, CourseDto>>> findCourseRevisions(Long id, Pageable pageable) {
        if (!courseRepository.existsById(id)) {
            return Optional.empty();
        }
        Page<Revision<Integer, Course>> revisions = enversRevisionService.findRevisions(Course.class, id, pageable);
        Page<Revision<Integer, CourseDto>> dtoRevisions = revisions.map(revision ->
                Revision.of(revision.getMetadata(), courseMapper.map(revision.getEntity()))
        );
        return Optional.of(dtoRevisions);
    }

    @Override
    public Optional<Revision<Integer, CourseDto>> findCourseRevision(Long id, Integer revisionNumber) {
        return enversRevisionService.findRevision(Course.class, id, revisionNumber)
                .map(revision -> Revision.of(revision.getMetadata(), courseMapper.map(revision.getEntity())));
    }

    @Override
    public Optional<Revision<Integer, CourseDto>> findLatestCourseRevision(Long id) {
        return enversRevisionService.findLastChangeRevision(Course.class, id)
                .map(revision -> Revision.of(revision.getMetadata(), courseMapper.map(revision.getEntity())));
    }

}
