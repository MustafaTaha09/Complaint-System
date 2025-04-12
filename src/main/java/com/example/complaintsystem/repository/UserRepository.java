package com.example.complaintsystem.repository;

import com.example.complaintsystem.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Integer> {
    //we used join fetch to solve the lazy initilization of spring security when returning a user (to add roles)
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String userName);

    Optional<Object> findByEmail(String email);

    // Count users associated with a specific role ID
    long countByRoleRoleId(Integer roleId);

    // Count users assigned to a specific department ID
    long countByDepartmentDepartmentId(Integer departmentId);

}
