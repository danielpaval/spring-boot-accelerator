package com.example.demo.service;

import com.example.common.service.CommonService;
import com.example.demo.dto.UserPatchDto;
import com.example.demo.entity.User;
import com.example.demo.generated.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;

import java.util.List;
import java.util.Optional;

public interface UserService extends CommonService<Long, User, UserDto, UserPatchDto> {

    List<UserDto> findAll();

    Optional<UserDto> findByExternalId(String externalId);

    Optional<Long> findIdByExternalId(String externalId);

    // Audit methods
    Optional<Page<Revision<Integer, UserDto>>> findUserRevisions(Long id, Pageable pageable);
    
    Optional<Revision<Integer, UserDto>> findUserRevision(Long id, Integer revisionNumber);
    
    Optional<Revision<Integer, UserDto>> findLatestUserRevision(Long id);

}
