package com.example.complaintsystem.Repository;

import com.example.complaintsystem.Entity.Ticket;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends BaseRepository<Ticket, Integer> {



//@Query("select t from Ticket t join fetch t.ticketStatus ts where t.ticketId = :ticketId")
    @Query(value = "SELECT t.* FROM dbo.tickets t JOIN dbo.ticket_statuses ts ON t.status_id = ts.status_id WHERE t.ticket_id = :ticketId", nativeQuery = true)
    Optional<Ticket> getTicketAndTicketStatus(@Param("ticketId") Integer Id);


    // We are using join fetch to solve the N+1 problem
    @Query("SELECT t FROM Ticket t JOIN FETCH t.ticketStatus ts " +
            "JOIN FETCH t.user u " +
            "JOIN FETCH t.department d " +
            "LEFT JOIN FETCH t.comments c " +
            "LEFT JOIN FETCH t.ticketAssignments ta where t.ticketId = :ticketId")
    Optional<Ticket> getAllFieldsOfTicket(@Param("ticketId") Integer Id);

    Optional<Ticket> getAllByTicketId(Integer Id);

    Optional<Ticket> getTicketByTicketId(Integer Id);

    // Query to fetch Ticket and its Comments eagerly
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.comments WHERE t.ticketId = :id")
    Optional<Ticket> findTicketWithCommentsById(@Param("id") Integer id);

    // Count tickets assigned to a specific department ID
    long countByDepartmentDepartmentId(Integer departmentId);
}
