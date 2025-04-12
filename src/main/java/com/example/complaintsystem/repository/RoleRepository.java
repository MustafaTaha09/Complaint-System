package com.example.complaintsystem.repository;

import com.example.complaintsystem.entity.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    // Count users associated with a specific role ID

}
