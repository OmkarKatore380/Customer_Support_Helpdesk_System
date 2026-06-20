package com.example.CustomerSupportHelpdeskSystem_1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.CustomerSupportHelpdeskSystem_1.entity.Attachment;
import com.example.CustomerSupportHelpdeskSystem_1.entity.Ticket;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTicket(Ticket ticket);
}