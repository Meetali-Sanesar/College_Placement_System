package com.yourname.campusplacement.config;

import com.yourname.campusplacement.entity.Company;
import com.yourname.campusplacement.entity.DriveStatus;
import com.yourname.campusplacement.entity.PlacementDrive;
import com.yourname.campusplacement.entity.Role;
import com.yourname.campusplacement.entity.User;
import com.yourname.campusplacement.repository.CompanyRepository;
import com.yourname.campusplacement.repository.PlacementDriveRepository;
import com.yourname.campusplacement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository driveRepository;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      CompanyRepository companyRepository, PlacementDriveRepository driveRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.driveRepository = driveRepository;
    }

    @Override
    public void run(String... args) {
        String adminPassword = System.getenv("ADMIN_DEFAULT_PASSWORD");
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn(">>> ADMIN_DEFAULT_PASSWORD env var not set. Admin account seeding skipped.");
        } else {
            User admin = userRepository.findByEmail("admin@campusplacement.com").orElse(new User());
            admin.setFullName("System Admin");
            admin.setEmail("admin@campusplacement.com");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info(">>> Default admin account seeded/updated: admin@campusplacement.com");
        }

        // Seed Companies and Drives if none exist
        if (companyRepository.count() == 0) {
            Company google = new Company();
            google.setName("Google");
            google.setWebsite("https://google.com");
            google.setIndustry("Technology");
            google.setDescription("Leading global technology company.");
            companyRepository.save(google);

            Company microsoft = new Company();
            microsoft.setName("Microsoft");
            microsoft.setWebsite("https://microsoft.com");
            microsoft.setIndustry("Technology");
            microsoft.setDescription("Multinational technology corporation.");
            companyRepository.save(microsoft);

            PlacementDrive sweDrive = new PlacementDrive();
            sweDrive.setCompany(google);
            sweDrive.setRole("Software Engineer");
            sweDrive.setDescription("Build scalable backend systems and exciting frontend features.");
            sweDrive.setPackageLpa(25.0);
            sweDrive.setLocation("Bangalore");
            sweDrive.setEligibilityCgpa(8.0);
            sweDrive.setEligibleBranches("CSE, ECE, IT");
            sweDrive.setDeadline(LocalDate.now().plusDays(15));
            sweDrive.setStatus(DriveStatus.OPEN);
            driveRepository.save(sweDrive);

            PlacementDrive dsDrive = new PlacementDrive();
            dsDrive.setCompany(microsoft);
            dsDrive.setRole("Data Scientist");
            dsDrive.setDescription("Work on machine learning models and data analytics.");
            dsDrive.setPackageLpa(20.0);
            dsDrive.setLocation("Hyderabad");
            dsDrive.setEligibilityCgpa(7.5);
            dsDrive.setEligibleBranches("CSE, AI/ML");
            dsDrive.setDeadline(LocalDate.now().plusDays(30));
            dsDrive.setStatus(DriveStatus.OPEN);
            driveRepository.save(dsDrive);

            log.info(">>> Seeded default dummy companies and placement drives.");
        }
    }
}
