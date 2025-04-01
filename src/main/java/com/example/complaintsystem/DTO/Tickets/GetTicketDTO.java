// TicketDTO.java
package com.example.complaintsystem.DTO.Tickets;

import com.example.complaintsystem.DTO.Comments.CommentDTO;
import com.example.complaintsystem.Entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetTicketDTO {
    private Integer ticketId;
    private Integer userId;
    private Integer departmentId;
    private Integer statusId;
    private String statusName;
    private String title;
    private String description;
    private String departmentName;
    private List<CommentDTO> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
