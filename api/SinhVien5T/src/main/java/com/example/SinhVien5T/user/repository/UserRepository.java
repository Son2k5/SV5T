package com.example.SinhVien5T.user.repository;

import com.example.SinhVien5T.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByPublicId(String publicId);

    @Query("""
            select distinct u
            from User u
            left join fetch u.detail
            left join fetch u.addresses
            where u.id = :id
            """)
    Optional<User> findByIdWithDetailAndAddresses(@Param("id") Long id);

    @Query("""
            select distinct u
            from User u
            left join fetch u.detail
            left join fetch u.addresses
            where u.email = :email
            """)
    Optional<User> findByEmailWithDetailAndAddresses(@Param("email") String email);
}


