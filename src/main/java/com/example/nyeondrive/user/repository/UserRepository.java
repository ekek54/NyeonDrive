package com.example.nyeondrive.user.repository;

import com.example.nyeondrive.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
     Optional<User> findByName(String name);
}
