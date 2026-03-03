package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.Request;
import com.varniga.requestmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByCreatedBy(User user); // Fetch all requests by user
}