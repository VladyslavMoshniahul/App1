package com.example.demo.javaSrc.users;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public void addAdmin(Admin admin) {
        adminRepository.save(admin);
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public Optional<Admin> getAdminByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public Admin updateAdmin(Long id, Admin updatedData) {
        return adminRepository.findById(id).map(existing -> {
            existing.setFirstName(updatedData.getFirstName());
            existing.setLastName(updatedData.getLastName());
            existing.setAboutMe(updatedData.getAboutMe());
            existing.setDateOfBirth(updatedData.getDateOfBirth());
            existing.setEmail(updatedData.getEmail());
            existing.setPassword(updatedData.getPassword());
            return adminRepository.save(existing);
        }).orElse(null);
    }
}
