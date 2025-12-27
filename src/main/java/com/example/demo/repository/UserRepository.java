package com.example.demo.repository;

import com.example.common.repository.CommonRepository;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CommonRepository<Long, User> {

    Optional<User> findByExternalId(String externalId);

    @Query("SELECT u.id FROM User u WHERE u.externalId = :externalId")
    Optional<Long> findIdByExternalId(String externalId);

}