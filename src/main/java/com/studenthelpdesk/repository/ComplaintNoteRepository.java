package com.studenthelpdesk.repository;

import com.studenthelpdesk.model.Complaint;
import com.studenthelpdesk.model.ComplaintNote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintNoteRepository extends JpaRepository<ComplaintNote, Long> {
    List<ComplaintNote> findAllByComplaintOrderByCreatedAtAsc(Complaint complaint);
}
