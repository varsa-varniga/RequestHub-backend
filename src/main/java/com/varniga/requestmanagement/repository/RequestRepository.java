package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    // Optional: find all requests by user id
    //List<Request> findByUserId(Long userId);
}