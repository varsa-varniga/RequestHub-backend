package com.varniga.requestmanagement.repository;
import com.varniga.requestmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Optional: find a user by email
    User findByEmail(String email);
}