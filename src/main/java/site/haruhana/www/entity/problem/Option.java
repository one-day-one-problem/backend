package site.haruhana.www.entity.problem;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "options")
public class Option {

    /**
     * 옵션 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 옵션 내용
     */
    @Column(columnDefinition = "TEXT")
    private String content;

}
