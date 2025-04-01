package com.example.complaintsystem.Entity;

import com.example.complaintsystem.Enums.Statuses;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Entity
@Table(name = "TicketAssignments")
@Getter
@Setter
public class TicketAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Generates a unique ID for each TicketAssignment
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ticketAssignments"})
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

