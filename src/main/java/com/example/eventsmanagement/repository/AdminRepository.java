package com.example.eventsmanagement.repository;

import com.example.eventsmanagement.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByEmailAndPasswordAndUsername(String email, String password, String username);

}
