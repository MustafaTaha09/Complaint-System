package com.example.complaintsystem.service;

import com.example.complaintsystem.dto.Departments.DepartmentDTO;
import com.example.complaintsystem.dto.Departments.DepartmentRequestDTO;
import com.example.complaintsystem.entity.Department;
import com.example.complaintsystem.exception.BadRequestException;
import com.example.complaintsystem.exception.ResourceNotFoundException;
import com.example.complaintsystem.repository.DepartmentRepository;
import com.example.complaintsystem.repository.TicketRepository;
import com.example.complaintsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             TicketRepository ticketRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        log.info("Fetching all departments");
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDTO> dtos = departments.stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());
        log.info("Found {} departments", dtos.size());
        return dtos;
    }

    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(Integer id) {
        log.info("Fetching department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with ID: {}", id);
                    return new ResourceNotFoundException("Department not found with id: " + id);
                });
        log.info("Found department: {}", department.getDepartmentName());
        return convertToDepartmentDTO(department);
    }

    @Transactional
    public DepartmentDTO createDepartment(DepartmentRequestDTO departmentRequestDTO) {
        String deptName = departmentRequestDTO.getDepartmentName().trim();
        log.info("Attempting to create department with name: '{}'", deptName);

        if (departmentRepository.existsByDepartmentName(deptName)) {
            log.warn("Department creation failed: Name '{}' already exists", deptName);
            throw new BadRequestException("Department with name '" + deptName + "' already exists.");
        }

        Department newDepartment = new Department();
        newDepartment.setDepartmentName(deptName);

        Department savedDepartment = departmentRepository.save(newDepartment);
        log.info("Successfully created department '{}' with ID: {}", savedDepartment.getDepartmentName(), savedDepartment.getDepartmentId());
        return convertToDepartmentDTO(savedDepartment);
    }

    @Transactional
    public DepartmentDTO updateDepartment(Integer id, DepartmentRequestDTO departmentRequestDTO) {
        String newDeptName = departmentRequestDTO.getDepartmentName().trim();
        log.info("Attempting to update department ID: {} to name: '{}'", id, newDeptName);

        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found for update with ID: {}", id);
                    return new ResourceNotFoundException("Department not found with id: " + id);
                });

        // Check if the new name is already taken by a *different* department
        Optional<Department> deptWithNewName = departmentRepository.findByDepartmentName(newDeptName);
        if (deptWithNewName.isPresent() && !deptWithNewName.get().getDepartmentId().equals(id)) {
            log.warn("Department update failed: Name '{}' is already used by department ID: {}", newDeptName, deptWithNewName.get().getDepartmentId());
            throw new BadRequestException("Department name '" + newDeptName + "' is already in use.");
        }

        existingDepartment.setDepartmentName(newDeptName);
        Department updatedDepartment = departmentRepository.save(existingDepartment);
        log.info("Successfully updated department ID: {} to name: '{}'", updatedDepartment.getDepartmentId(), updatedDepartment.getDepartmentName());
        return convertToDepartmentDTO(updatedDepartment);
    }

    @Transactional
    public void deleteDepartment(Integer id) {
        log.info("Attempting to delete department with ID: {}", id);

        // Check if department exists
        if (!departmentRepository.existsById(id)) {
            log.error("Department not found for deletion with ID: {}", id);
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }

        // Check if any users are assigned to this department
        long userCount = userRepository.countByDepartmentDepartmentId(id);
        if (userCount > 0) {
            log.warn("Deletion failed: Department ID {} is assigned to {} user(s).", id, userCount);
            throw new BadRequestException("Department cannot be deleted because it is assigned to " + userCount + " user(s).");
        }

        // Check if any tickets are assigned to this department
        long ticketCount = ticketRepository.countByDepartmentDepartmentId(id);
        if (ticketCount > 0) {
            log.warn("Deletion failed: Department ID {} is assigned to {} ticket(s).", id, ticketCount);
            throw new BadRequestException("Department cannot be deleted because it is assigned to " + ticketCount + " ticket(s).");
        }

        departmentRepository.deleteById(id);
        log.info("Successfully deleted department with ID: {}", id);
    }

    // Keep your existing convertToDepartmentDTO method
    public DepartmentDTO convertToDepartmentDTO(Department department) {
        if (department == null) {
            return null;
        }
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        return dto;
    }
}