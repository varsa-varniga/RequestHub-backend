
package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatus, Long> {
    // Optional: find by name if needed
    RequestStatus findByName(String name);
}