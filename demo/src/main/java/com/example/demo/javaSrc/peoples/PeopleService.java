package com.example.demo.javaSrc.peoples;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeopleService {
    @Autowired
    private final PeopleRepository peopleRepository;

    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public People getPeopleById(Long id) {
        return peopleRepository.findById(id).orElse(null);
    }

    public List<People> getAllPeoples() {
        return peopleRepository.findAll();
    }

    public People createPeople(People person) {
        if (person.getRole() != People.Role.ADMIN && person.getSchoolId() == null) {
            throw new IllegalArgumentException("School ID is required unless role is ADMIN.");
        }
        return peopleRepository.save(person);
    }

    public List<People> getBySchoolAndClass(Long schoolId, Long classId) {
        if (schoolId == null) return Collections.emptyList();
        if (classId == null) {
            return peopleRepository.findBySchoolId(schoolId);
        }
        return peopleRepository.findBySchoolIdAndClassId(schoolId, classId);
    }

    public List<People> getBySchoolClassAndRole(Long schoolId, Long classId, People.Role role) {
        return getBySchoolAndClass(schoolId, classId).stream()
                .filter(p -> role.equals(p.getRole()))
                .collect(Collectors.toList());
    }

    public List<People> getPeopleByRole(String role) {
        return peopleRepository.findByRole(People.Role.valueOf(role.toUpperCase()));
    }

    public People findByEmail(String email) {
        return peopleRepository.findByEmail(email).orElse(null);
    }

    public People updateProfile(Long id, People updatedData) {
        return peopleRepository.findById(id).map(existing -> {
            copyCommonFields(updatedData, existing, false);
            return peopleRepository.save(existing);
        }).orElse(null);
    }

    public People updatePeople(Long id, People updatedData) {
        return peopleRepository.findById(id).map(existing -> {
            copyCommonFields(updatedData, existing, true);
            return peopleRepository.save(existing);
        }).orElse(null);
    }

    public boolean deletePeople(Long id) {
        if (peopleRepository.existsById(id)) {
            peopleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<People> getPeoplesByRoles(People.Role... roles) {
        List<People.Role> roleList = Arrays.asList(roles);
        return peopleRepository.findAll().stream()
                .filter(u -> roleList.contains(u.getRole()))
                .collect(Collectors.toList());
    }

    private void copyCommonFields(People source, People target, boolean includeSensitive) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAboutMe(source.getAboutMe());
        target.setDateOfBirth(source.getDateOfBirth());
        if (includeSensitive) {
            target.setEmail(source.getEmail());
            target.setPassword(source.getPassword());
        }
    }

    public Long getCountBySchoolIdAndClassIdAndRole(Long schoolId, Long classId, People.Role role) {
        return peopleRepository.findCountBySchoolIdAndClassIdAndRole(schoolId, classId, role);
    }

    public Long getCountBySchoolIdAndRole(Long schoolId, People.Role role) {
        return peopleRepository.findCountBySchoolIdAndRole(schoolId, role);
    }
}
