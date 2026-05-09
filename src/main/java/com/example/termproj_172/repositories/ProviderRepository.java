package com.example.termproj_172.repositories;

import com.example.termproj_172.domainModels.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    List<Provider> findAllByOrderByDepartment_NameAscFullNameAsc();

    List<Provider> findAllByDepartment_IdOrderByFullNameAsc(Long departmentId);
}
