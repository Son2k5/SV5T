package com.example.SinhVien5T.user.repository;

import com.example.SinhVien5T.user.entity.UserDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {

    boolean existsByIdentityCardNumberAndUserIdNot(String identityCardNumber, Long userId);

    boolean existsByStudentCodeAndUserIdNot(String studentCode, Long userId);

    Page<UserDetail> findByFaculty(String faculty, Pageable pageable);

    Page<UserDetail> findByAcademicYear(Integer academicYear, Pageable pageable);

    Page<UserDetail> findByFacultyAndAcademicYear(
            String faculty,
            Integer academicYear,
            Pageable pageable
    );
}
