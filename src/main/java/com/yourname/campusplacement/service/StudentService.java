package com.yourname.campusplacement.service;

import com.yourname.campusplacement.dto.StudentProfileRequest;
import com.yourname.campusplacement.entity.StudentProfile;
import com.yourname.campusplacement.entity.User;
import com.yourname.campusplacement.exception.ResourceNotFoundException;
import com.yourname.campusplacement.repository.StudentProfileRepository;
import com.yourname.campusplacement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public StudentService(StudentProfileRepository studentProfileRepository, UserRepository userRepository,
                           FileStorageService fileStorageService) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public StudentProfile getProfileByEmail(String email) {
        User user = getUser(email);
        return studentProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    StudentProfile newProfile = new StudentProfile();
                    newProfile.setUser(user);
                    return studentProfileRepository.save(newProfile);
                });
    }

    @Transactional
    public StudentProfile updateProfile(String email, StudentProfileRequest request) {
        StudentProfile profile = getProfileByEmail(email);
        profile.setCollege(request.getCollege());
        profile.setBranch(request.getBranch());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setPhone(request.getPhone());
        profile.setCgpa(request.getCgpa());
        profile.setSkills(request.getSkills());
        return studentProfileRepository.save(profile);
    }

    @Transactional
    public StudentProfile uploadResume(String email, MultipartFile file) {
        StudentProfile profile = getProfileByEmail(email);
        String resumeUrl = fileStorageService.storeResume(file, profile.getId(), profile.getResumeUrl());
        profile.setResumeUrl(resumeUrl);
        return studentProfileRepository.save(profile);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
