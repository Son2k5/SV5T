package com.example.SinhVien5T.user.service;

import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Map<String, Object> getCurrentUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("User not authenticated");
        }

        User user = findAuthenticatedUser(authentication);

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("userName", user.getUserName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("avatar", user.getAvatar());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("ethnicity", user.getEthnicity());
        profile.put("gender", user.getGender());
        profile.put("birthDay", user.getBirthDay());
        profile.put("address", user.getAddress());
        profile.put("faculty", user.getFaculty());
        profile.put("classId", user.getClassId());
        profile.put("studentCode", user.getStudentCode());
        profile.put("verified", user.isVerified());
        profile.put("active", user.isActive());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("updatedAt", user.getUpdatedAt());
        return profile;
    }

    private User findAuthenticatedUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails && customUserDetails.getId() != null) {
            return userRepository.findById(customUserDetails.getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}



