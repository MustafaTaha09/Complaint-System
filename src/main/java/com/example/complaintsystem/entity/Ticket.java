package com.example.complaintsystem.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Tickets")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tickets"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "department_id")
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tickets"})
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY) // to avoid N+1 Problem
    @JoinColumn(name = "status_id", referencedColumnName = "status_id")
    // This is to avoid ciruclar reference relationships and to avoid the lazy initilaizer error associated with it.
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tickets"})
    private TicketStatus ticketStatus;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ticket"})
    private List<Comment> comments;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ticket"})
    private List<TicketAssignment> ticketAssignments;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}

