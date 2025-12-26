package com.example.demo.repository;

import com.example.common.repository.CommonRepository;
import com.example.demo.entity.Course;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends CommonRepository<Long, Course>, RevisionRepository<Course, Long, Integer> {
}
