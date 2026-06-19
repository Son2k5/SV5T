package com.example.SinhVien5T.user.repository;

import com.example.SinhVien5T.user.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {

    boolean existsByIdentityCardNumberAndUserIdNot(String identityCardNumber, Long userId);

    boolean existsByStudentCodeAndUserIdNot(String studentCode, Long userId);
}
