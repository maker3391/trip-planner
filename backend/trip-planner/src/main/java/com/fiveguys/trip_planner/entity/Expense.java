package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
@Getter @Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_user_id")
    private User paidByUser;

    // --- 계층 구조를 위한 자기 참조 필드 추가 ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 부모(상위 틀)의 ID를 저장
    private Expense parent;

    @Builder.Default // Builder 사용 시 리스트 초기화 보장
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> subExpenses = new ArrayList<>();

    // ---------------------------------------

    private String category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String expenseType;

    private String memo;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 편의 메서드: 하위 항목 추가 시 부모 연결을 자동으로 처리
    public void addSubExpense(Expense sub) {
        this.subExpenses.add(sub);
        sub.setParent(this);
        sub.setTripPlan(this.tripPlan); // 하위 항목도 동일한 여행 계획에 속함
    }

    public void update(BigDecimal amount, String category, String description) {
        this.amount = amount;
        this.category = category;
        this.description = description;
    }
}