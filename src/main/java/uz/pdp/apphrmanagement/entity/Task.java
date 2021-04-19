package uz.pdp.apphrmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.pdp.apphrmanagement.entity.enums.TaskStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String title;

    @NotNull
    private String body;

    @NotNull
    private Date deadLine;              // vazifa tugatilishi kerak bo'lgan vaqt

    @ManyToOne
    private User responsible;           // vazifaga mas'ul

    @CreatedBy
    private UUID createdBy;             // vazifa qo'shuchi

    @CreationTimestamp
    private Timestamp createdAt;        // vazifa qo'shilgan vaqt

    private TaskStatus status;          // vazifa xolati

}
