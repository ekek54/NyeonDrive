package com.example.nyeondrive.user.entity;

import com.example.nyeondrive.file.entity.File;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_email")
    private String email;

    @JoinColumn(name = "drive")
    @OneToOne
    private File drive;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private User(String name, String email, File drive) {
        this.name = name;
        this.email = email;
        this.drive = drive;
    }
}
