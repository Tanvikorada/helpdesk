package com.studenthelpdesk.repository;

import com.studenthelpdesk.model.Complaint;
import com.studenthelpdesk.model.ComplaintAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, Long> {
    List<ComplaintAttachment> findAllByComplaintOrderByUploadedAtDesc(Complaint complaint);
}
