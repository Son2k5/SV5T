package com.example.SinhVien5T.user.repository;

import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByPublicId(String publicId);

    @EntityGraph(attributePaths = "detail")
    Page<User> findByRole(Role role, Pageable pageable);

    @EntityGraph(attributePaths = "detail")
    Page<User> findByIsActive(boolean active, Pageable pageable);

    @EntityGraph(attributePaths = "detail")
    Page<User> findByRoleAndIsActive(Role role, boolean active, Pageable pageable);

    @EntityGraph(attributePaths = "detail")
    Page<User> findAll(Pageable pageable);

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

    @EntityGraph(attributePaths = "detail")
    @Query(
            value = """
                    select u
                    from User u
                    left join u.detail d
                    where (:role is null or u.role = :role)
                      and (:active is null or u.isActive = :active)
                      and (
                           u.email like :keywordPrefix
                        or d.studentCode like :keywordPrefix
                        or d.fullName like :keywordPrefix
                      )
                    order by u.id desc
                    """,
            countQuery = """
                    select count(u)
                    from User u
                    left join u.detail d
                    where (:role is null or u.role = :role)
                      and (:active is null or u.isActive = :active)
                      and (
                           u.email like :keywordPrefix
                        or d.studentCode like :keywordPrefix
                        or d.fullName like :keywordPrefix
                      )
                    """
    )
    Page<User> searchAdminByKeywordPrefix(
            @Param("keywordPrefix") String keywordPrefix,
            @Param("role") Role role,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
