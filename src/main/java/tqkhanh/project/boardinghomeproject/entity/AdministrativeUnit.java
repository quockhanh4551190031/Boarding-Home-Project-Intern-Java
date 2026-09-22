package tqkhanh.project.boardinghomeproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrative_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String province;
}