package uz.pdp.apphrmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class SalaryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User employee;

    private double salaryAmount;              // berilgan oylik miqdori

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createdAt;             // oylik berilgan sana va vaqt

    // berilayotgan oylik maosh qaysi sana oraliqlari uchun berilayotgani
    // ==>
    @Column(nullable = false)
    private Date workStartDate;

    @Column(nullable = false)
    private Date workEndDate;
    // <===

}
