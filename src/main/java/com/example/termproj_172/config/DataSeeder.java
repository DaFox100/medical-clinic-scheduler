package com.example.termproj_172.config;

import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.AppUserRole;
import com.example.termproj_172.domainModels.Department;
import com.example.termproj_172.domainModels.Patient;
import com.example.termproj_172.domainModels.Provider;
import com.example.termproj_172.domainModels.TimeSlot;
import com.example.termproj_172.domainModels.TimeSlotStatus;
import com.example.termproj_172.repositories.AppUserRepository;
import com.example.termproj_172.repositories.DepartmentRepository;
import com.example.termproj_172.repositories.PatientRepository;
import com.example.termproj_172.repositories.ProviderRepository;
import com.example.termproj_172.repositories.TimeSlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedSampleData(DepartmentRepository departmentRepository,
                                     ProviderRepository providerRepository,
                                     PatientRepository patientRepository,
                                     TimeSlotRepository timeSlotRepository,
                                     AppUserRepository appUserRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (departmentRepository.count() == 0 && providerRepository.count() == 0 && patientRepository.count() == 0) {
                Department cardiology = departmentRepository.save(new Department("Cardiology"));
                Department pediatrics = departmentRepository.save(new Department("Pediatrics"));
                Department familyMedicine = departmentRepository.save(new Department("Family Medicine"));

                Provider drPatel = providerRepository.save(new Provider("Dr. Anika Patel", cardiology));
                Provider drNguyen = providerRepository.save(new Provider("Dr. Minh Nguyen", pediatrics));
                Provider drRivera = providerRepository.save(new Provider("Dr. Elena Rivera", familyMedicine));

                patientRepository.save(new Patient("Jane Doe", "jane.doe@example.com"));
                patientRepository.save(new Patient("Bob Smith", "bob.smith@example.com"));
                patientRepository.save(new Patient("Sam Carter", "sam.carter@example.com"));

                LocalDate baseDate = LocalDate.now().plusDays(1);

                createSlot(timeSlotRepository, drPatel, baseDate, 9, 0);
                createSlot(timeSlotRepository, drPatel, baseDate, 10, 30);
                createSlot(timeSlotRepository, drNguyen, baseDate.plusDays(1), 11, 0);
                createSlot(timeSlotRepository, drNguyen, baseDate.plusDays(1), 13, 30);
                createSlot(timeSlotRepository, drRivera, baseDate.plusDays(2), 8, 30);
                createSlot(timeSlotRepository, drRivera, baseDate.plusDays(2), 15, 0);
            }

            var providers = providerRepository.findAll();
            var patients = patientRepository.findAll();

            ensureUser(appUserRepository, passwordEncoder, "admin", "password", AppUserRole.ADMIN, null, null);

            if (providers.size() > 0) {
                ensureUser(appUserRepository, passwordEncoder, "provider.patel", "password", AppUserRole.PROVIDER, null, providers.get(0));
            }
            if (providers.size() > 1) {
                ensureUser(appUserRepository, passwordEncoder, "provider.nguyen", "password", AppUserRole.PROVIDER, null, providers.get(1));
            }
            if (providers.size() > 2) {
                ensureUser(appUserRepository, passwordEncoder, "provider.rivera", "password", AppUserRole.PROVIDER, null, providers.get(2));
            }
            if (patients.size() > 0) {
                ensureUser(appUserRepository, passwordEncoder, "patient.jane", "password", AppUserRole.PATIENT, patients.get(0), null);
            }
            if (patients.size() > 1) {
                ensureUser(appUserRepository, passwordEncoder, "patient.bob", "password", AppUserRole.PATIENT, patients.get(1), null);
            }
            if (patients.size() > 2) {
                ensureUser(appUserRepository, passwordEncoder, "patient.sam", "password", AppUserRole.PATIENT, patients.get(2), null);
            }
        };
    }

    private void createSlot(TimeSlotRepository repository,
                            Provider provider,
                            LocalDate date,
                            int startHour,
                            int startMinute) {
        LocalTime startTime = LocalTime.of(startHour, startMinute);
        LocalTime endTime = startTime.plusMinutes(30);
        repository.save(new TimeSlot(provider, date, startTime, endTime, TimeSlotStatus.AVAILABLE));
    }

    private void ensureUser(AppUserRepository repository,
                            PasswordEncoder passwordEncoder,
                            String username,
                            String rawPassword,
                            AppUserRole role,
                            Patient patient,
                            Provider provider) {
        if (repository.findByUsername(username).isPresent()) {
            return;
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        appUser.setRole(role);
        appUser.setPatient(patient);
        appUser.setProvider(provider);
        repository.save(appUser);
    }
}
