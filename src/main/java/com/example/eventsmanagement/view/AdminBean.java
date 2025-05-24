package com.example.eventsmanagement.view;

import com.example.eventsmanagement.model.Admin;
import com.example.eventsmanagement.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@Component
public class AdminBean implements Serializable {

    @Autowired
    private AdminRepository adminRepository;

    private List<Admin> admins;

    @PostConstruct
    public void init() {
        admins = adminRepository.findAll();
    }

    public List<Admin> getAdmins() {
        return admins;
    }
}
