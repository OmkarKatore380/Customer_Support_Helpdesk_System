package com.example.CustomerSupportHelpdeskSystem_1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CustomerSupportHelpdeskSystem_1.dto.CommentRequest;
import com.example.CustomerSupportHelpdeskSystem_1.dto.StatusUpdateRequest;
import com.example.CustomerSupportHelpdeskSystem_1.dto.TicketRequest;
import com.example.CustomerSupportHelpdeskSystem_1.dto.TicketResponse;
import com.example.CustomerSupportHelpdeskSystem_1.entity.Attachment;
import com.example.CustomerSupportHelpdeskSystem_1.services.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Step 2 of the flow: USER creates a ticket
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @RequestBody TicketRequest request,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();
        TicketResponse response = ticketService.createTicket(request, currentUserEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // USER views their own tickets
    @GetMapping("/my")
    public List<TicketResponse> getMyTickets(Authentication authentication) {
        return ticketService.getMyTickets(authentication.getName());
    }

    // STAFF views tickets assigned to them
    @GetMapping("/assigned")
    public List<TicketResponse> getAssignedTickets(Authentication authentication) {
        return ticketService.getAssignedTickets(authentication.getName());
    }

    // ADMIN / STAFF views every ticket in the system
    @GetMapping("/all")
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // Step 4 / Step 6 of the flow: STAFF updates ticket status
    @PutMapping("/{id}")
    public TicketResponse updateStatus(
            @PathVariable("id") long id,
            @RequestBody StatusUpdateRequest request) {

        return ticketService.updateStatus(id, request.getStatus());
    }

    // Step 5 of the flow: adding a comment to a ticket
    @PostMapping("/{id}/comment")
    public ResponseEntity<String> addComment(
            @PathVariable("id") long id,
            @RequestBody CommentRequest request,
            Authentication authentication) {

        ticketService.addComment(id, request, authentication.getName());
        return new ResponseEntity<>("Comment added successfully", HttpStatus.CREATED);
    }

    // Step 2 of the flow: uploading a file to a ticket
    @PostMapping("/{id}/upload")
    public ResponseEntity<Attachment> uploadFile(
            @PathVariable("id") long id,
            @RequestParam("file") MultipartFile file) {

        Attachment attachment = ticketService.uploadAttachment(id, file);
        return new ResponseEntity<>(attachment, HttpStatus.CREATED);
    }
}