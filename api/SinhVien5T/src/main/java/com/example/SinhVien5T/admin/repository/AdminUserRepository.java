package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("""
           select distinct u
           from User u
           left join fetch u.detail
           where u.publicId = :publicId
           """)
    Optional<User> findByPublicIdWithDetail(@Param("publicId") String publicId);

    @EntityGraph(attributePaths = "detail")
    @Query("""
           select u
           from User u
           left join u.detail d
           where (:role is null or u.role = :role)
             and (:active is null or u.isActive = :active)
             and (:keyword is null
                or lower(u.email) like :keyword
                or lower(coalesce(d.fullName, '')) like :keyword
                or lower(coalesce(d.studentCode, '')) like :keyword)
           order by u.id desc
           """)
    Page<User> search(@Param("keyword") String keyword,
                      @Param("role") Role role,
                      @Param("active") Boolean active,
                      Pageable pageable);
}