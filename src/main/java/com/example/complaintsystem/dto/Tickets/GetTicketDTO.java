// TicketDTO.java
package com.example.complaintsystem.dto.Tickets;

import com.example.complaintsystem.dto.Comments.CommentDTO;
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
