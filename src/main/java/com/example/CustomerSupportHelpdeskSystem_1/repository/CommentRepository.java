package com.example.CustomerSupportHelpdeskSystem_1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.CustomerSupportHelpdeskSystem_1.entity.Comment;
import com.example.CustomerSupportHelpdeskSystem_1.entity.Ticket;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}