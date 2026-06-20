package com.example.CustomerSupportHelpdeskSystem_1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.CustomerSupportHelpdeskSystem_1.entity.Ticket;
import com.example.CustomerSupportHelpdeskSystem_1.entity.User;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedBy(User createdBy);

    List<Ticket> findByAssignedTo(User assignedTo);
}