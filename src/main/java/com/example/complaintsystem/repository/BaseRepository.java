package com.example.complaintsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
// indicate that this interface is not a repository that should be instantiated by Spring Data JPA
public interface BaseRepository <T, ID extends Serializable> extends JpaRepository<T, ID> {

}