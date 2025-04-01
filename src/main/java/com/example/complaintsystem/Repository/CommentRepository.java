package com.example.complaintsystem.Repository;

import com.example.complaintsystem.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Find all comments for a specific ticket, ordered by creation time
    List<Comment> findByTicketTicketIdOrderByCreatedAtAsc(Integer ticketId);

}