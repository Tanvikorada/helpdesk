package com.studenthelpdesk.repository;

import com.studenthelpdesk.model.Complaint;
import com.studenthelpdesk.model.ComplaintStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findAllByOrderByCreatedAtDesc();

    List<Complaint> findAllByStudentOrderByCreatedAtDesc(com.studenthelpdesk.model.AppUser student);

    List<Complaint> findAllByAssignedToOrderByCreatedAtDesc(com.studenthelpdesk.model.AppUser assignedTo);

    List<Complaint> findAllByEscalatedFalseAndEscalationDueAtBeforeAndStatusNotIn(LocalDateTime time, List<ComplaintStatus> closedStatuses);

    long countByStatus(ComplaintStatus status);

    @Query("""
        select c.student.department, count(c)
        from Complaint c
        group by c.student.department
        order by count(c) desc
        """)
    List<Object[]> countByDepartment();

    @Query("""
        select function('date_format', c.createdAt, '%Y-%m'), count(c)
        from Complaint c
        group by function('date_format', c.createdAt, '%Y-%m')
        order by function('date_format', c.createdAt, '%Y-%m')
        """)
    List<Object[]> monthlyComplaintTrend();
}
