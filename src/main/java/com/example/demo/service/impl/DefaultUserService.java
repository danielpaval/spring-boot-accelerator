package com.example.demo.service.impl;

import com.example.common.service.AbstractCommonService;
import com.example.demo.dto.UserPatchDto;
import com.example.demo.entity.User;
import com.example.demo.generated.dto.UserDto;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EnversRevisionService;
import com.example.demo.service.UserService;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DefaultUserService extends AbstractCommonService<Long, User, UserDto, UserPatchDto> implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final EnversRevisionService enversRevisionService;

    public DefaultUserService(UserRepository userRepository, UserMapper userMapper,
                              Validator validator, EnversRevisionService enversRevisionService) {
        super(userRepository, userMapper, validator);
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.enversRevisionService = enversRevisionService;
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::map).collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId)
                .map(userMapper::map);
    }

    @Override
    public Optional<Long> findIdByExternalId(String externalId) {
        return userRepository.findIdByExternalId(externalId);
    }

    // Audit method implementations
    @Override
    public Optional<Page<Revision<Integer, UserDto>>> findUserRevisions(Long id, Pageable pageable) {
        if (!userRepository.existsById(id)) {
            return Optional.empty();
        }
        Page<Revision<Integer, User>> revisions = enversRevisionService.findRevisions(User.class, id, pageable);
        Page<Revision<Integer, UserDto>> dtoRevisions = revisions.map(revision ->
            Revision.of(revision.getMetadata(), userMapper.map(revision.getEntity()))
        );
        return Optional.of(dtoRevisions);
    }

    @Override
    public Optional<Revision<Integer, UserDto>> findUserRevision(Long id, Integer revisionNumber) {
        return enversRevisionService.findRevision(User.class, id, revisionNumber)
                .map(revision -> Revision.of(revision.getMetadata(), userMapper.map(revision.getEntity())));
    }

    @Override
    public Optional<Revision<Integer, UserDto>> findLatestUserRevision(Long id) {
        return enversRevisionService.findLastChangeRevision(User.class, id)
                .map(revision -> Revision.of(revision.getMetadata(), userMapper.map(revision.getEntity())));
    }

}
