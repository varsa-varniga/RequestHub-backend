package com.varniga.requestmanagement.repository;

import com.varniga.requestmanagement.entity.Comment;
import com.varniga.requestmanagement.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRequestOrderByCreatedAtAsc(Request request);
}