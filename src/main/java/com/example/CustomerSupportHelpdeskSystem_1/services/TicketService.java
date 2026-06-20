package com.example.CustomerSupportHelpdeskSystem_1.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.CustomerSupportHelpdeskSystem_1.dto.CommentRequest;
import com.example.CustomerSupportHelpdeskSystem_1.dto.TicketRequest;
import com.example.CustomerSupportHelpdeskSystem_1.dto.TicketResponse;
import com.example.CustomerSupportHelpdeskSystem_1.entity.Attachment;
import com.example.CustomerSupportHelpdeskSystem_1.entity.Comment;
import com.example.CustomerSupportHelpdeskSystem_1.entity.Ticket;
import com.example.CustomerSupportHelpdeskSystem_1.entity.User;
import com.example.CustomerSupportHelpdeskSystem_1.exception.ResourceNotFoundException;
import com.example.CustomerSupportHelpdeskSystem_1.repository.AttachmentRepository;
import com.example.CustomerSupportHelpdeskSystem_1.repository.CommentRepository;
import com.example.CustomerSupportHelpdeskSystem_1.repository.TicketRepository;
import com.example.CustomerSupportHelpdeskSystem_1.repository.UserRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final EmailService emailService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.admin.email}")
    private String adminEmail;

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            AttachmentRepository attachmentRepository,
            EmailService emailService) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.emailService = emailService;
    }

    // Step 2 of the flow: USER creates a ticket
    public TicketResponse createTicket(TicketRequest request, String currentUserEmail) {

        User creator = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus("OPEN");
        ticket.setCreatedBy(creator);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        // Notify admin by email that a new ticket has arrived
        emailService.sendEmail(
                adminEmail,
                "New Support Ticket Created",
                "A new ticket has been created by " + creator.getName()
                        + ".\nTitle: " + saved.getTitle()
                        + "\nPriority: " + saved.getPriority());

        return toResponse(saved);
    }

    // USER sees their own tickets
    public List<TicketResponse> getMyTickets(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        return ticketRepository.findByCreatedBy(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // STAFF sees tickets assigned to them
    public List<TicketResponse> getAssignedTickets(String currentUserEmail) {
        User staff = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        return ticketRepository.findByAssignedTo(staff)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ADMIN/STAFF sees every ticket
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Step 3 of the flow: ADMIN assigns ticket to a STAFF member
    public TicketResponse assignTicket(long ticketId, long staffId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found: " + staffId));

        ticket.setAssignedTo(staff);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        emailService.sendEmail(
                staff.getEmail(),
                "A Ticket Has Been Assigned To You",
                "Ticket \"" + saved.getTitle() + "\" has been assigned to you. Please review it.");

        return toResponse(saved);
    }

    // Step 4 / Step 6 of the flow: STAFF updates ticket status
    public TicketResponse updateStatus(long ticketId, String newStatus) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        // If the ticket is resolved, notify the original creator (the USER)
        if ("RESOLVED".equalsIgnoreCase(newStatus)) {
            emailService.sendEmail(
                    saved.getCreatedBy().getEmail(),
                    "Your Ticket Has Been Resolved",
                    "Good news! Your ticket \"" + saved.getTitle() + "\" has been marked as RESOLVED.");
        }

        return toResponse(saved);
    }

    // Step 5 of the flow: adding a comment to a ticket thread
    public void addComment(long ticketId, CommentRequest request, String currentUserEmail) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setMessage(request.getMessage());
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    // Step 2 of the flow: uploading a file (like a screenshot) to a ticket
    public Attachment uploadAttachment(long ticketId, MultipartFile file) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID() + "_" + originalName;
            Path targetPath = uploadPath.resolve(storedName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = new Attachment();
            attachment.setTicket(ticket);
            attachment.setFileName(originalName);
            attachment.setFileUrl(targetPath.toString());
            attachment.setUploadedAt(LocalDateTime.now());

            return attachmentRepository.save(attachment);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    // Helper: converts a Ticket entity into a clean TicketResponse for the API
    private TicketResponse toResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setPriority(ticket.getPriority());
        response.setStatus(ticket.getStatus());
        response.setCreatedByName(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getName() : null);
        response.setAssignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getName() : null);
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        return response;
    }
}