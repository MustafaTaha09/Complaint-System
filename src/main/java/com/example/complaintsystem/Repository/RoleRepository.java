package com.example.complaintsystem.Repository;

import com.example.complaintsystem.Entity.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    // Count users associated with a specific role ID

}
