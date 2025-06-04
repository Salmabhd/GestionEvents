package com.example.projetsdr.service;


import com.example.projetsdr.model.Admin;
import com.example.projetsdr.repository.AdminRepository;

public class AdminService {
    private final AdminRepository adminRepo;

    public AdminService(AdminRepository adminRepo) {
        this.adminRepo = adminRepo;
    }

    public Admin login(String email, String password) {
        return adminRepo.findByEmailAndPassword(email, password);
    }
}

