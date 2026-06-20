package com.example.CustomerSupportHelpdeskSystem_1.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CustomerSupportHelpdeskSystem_1.dto.AssignTicketRequest;
import com.example.CustomerSupportHelpdeskSystem_1.dto.TicketResponse;
import com.example.CustomerSupportHelpdeskSystem_1.services.TicketService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TicketService ticketService;

    public AdminController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Step 3 of the flow: ADMIN assigns a ticket to a STAFF member
    @PutMapping("/tickets/{id}/assign")
    public TicketResponse assignTicket(
            @PathVariable("id") long id,
            @RequestBody AssignTicketRequest request) {

        return ticketService.assignTicket(id, request.getStaffId());
    }
}