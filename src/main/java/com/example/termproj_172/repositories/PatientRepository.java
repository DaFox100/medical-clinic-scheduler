package com.example.termproj_172.repositories;

import com.example.termproj_172.domainModels.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
