package com.example.complaintsystem.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "TicketStatuses")
@Getter
@Setter
public class TicketStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "status_name", nullable = false)
    private String statusName;

    @OneToMany(mappedBy = "ticketStatus")
    @JsonIgnoreProperties("ticketStatus") // this is to break the circular reference between ticket and ticketstatuses
    private List<Ticket> tickets;

}



