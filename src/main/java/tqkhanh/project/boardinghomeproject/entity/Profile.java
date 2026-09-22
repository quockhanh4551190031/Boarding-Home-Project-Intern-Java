package tqkhanh.project.boardinghomeproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false, unique = true)
    private User user;

    @Column(name="full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true,length = 15)
    private String phone;

    @Column(name="avatar_url",length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_brith")
    private LocalDate dateOfBirth;

    @Column(length = 255)
    private String address;

    @Column(name = "identity_number", length = 20)
    private String identityNumber;

    @Column(columnDefinition = "TEXT")
    private String bio;
}
