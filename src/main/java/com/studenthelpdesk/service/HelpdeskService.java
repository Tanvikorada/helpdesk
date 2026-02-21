package com.studenthelpdesk.service;

import com.studenthelpdesk.dto.ComplaintForm;
import com.studenthelpdesk.dto.DepartmentCount;
import com.studenthelpdesk.dto.MonthlyPoint;
import com.studenthelpdesk.dto.RegisterForm;
import com.studenthelpdesk.model.AppUser;
import com.studenthelpdesk.model.Complaint;
import com.studenthelpdesk.model.ComplaintAttachment;
import com.studenthelpdesk.model.ComplaintNote;
import com.studenthelpdesk.model.ComplaintStatus;
import com.studenthelpdesk.model.UserRole;
import com.studenthelpdesk.repository.AppUserRepository;
import com.studenthelpdesk.repository.ComplaintAttachmentRepository;
import com.studenthelpdesk.repository.ComplaintNoteRepository;
import com.studenthelpdesk.repository.ComplaintRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HelpdeskService {

    private final AppUserRepository appUserRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintNoteRepository complaintNoteRepository;
    private final ComplaintAttachmentRepository complaintAttachmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public HelpdeskService(AppUserRepository appUserRepository,
                           ComplaintRepository complaintRepository,
                           ComplaintNoteRepository complaintNoteRepository,
                           ComplaintAttachmentRepository complaintAttachmentRepository,
                           PasswordEncoder passwordEncoder,
                           FileStorageService fileStorageService,
                           NotificationService notificationService) {
        this.appUserRepository = appUserRepository;
        this.complaintRepository = complaintRepository;
        this.complaintNoteRepository = complaintNoteRepository;
        this.complaintAttachmentRepository = complaintAttachmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void registerStudent(RegisterForm form) {
        if (appUserRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (appUserRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        AppUser student = new AppUser();
        student.setUsername(form.getUsername().trim());
        student.setPassword(passwordEncoder.encode(form.getPassword()));
        student.setFullName(form.getFullName().trim());
        student.setEmail(form.getEmail().trim());
        student.setDepartment(form.getDepartment().trim());
        student.setRole(UserRole.STUDENT);
        appUserRepository.save(student);
    }

    @Transactional
    public void createComplaint(String username, ComplaintForm form, MultipartFile[] files) {
        AppUser student = getUserByUsername(username);
        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only students can submit complaints.");
        }

        Complaint complaint = new Complaint();
        complaint.setTitle(form.getTitle().trim());
        complaint.setDescription(form.getDescription().trim());
        complaint.setCategory(form.getCategory());
        complaint.setPriority(form.getPriority());
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setStudent(student);
        complaint.setEscalationDueAt(computeEscalationDue(form.getPriority()));
        complaintRepository.save(complaint);

        addNoteInternal(complaint, student, "Complaint submitted by student.");
        storeAttachments(complaint, student, files);

        List<String> managerEmails = getUsersByRoles(UserRole.MANAGEMENT).stream().map(AppUser::getEmail).toList();
        notificationService.sendToMany(managerEmails,
            "New Complaint #" + complaint.getId(),
            "A new complaint was submitted by " + student.getFullName() + ": " + complaint.getTitle());

        notificationService.sendToMany(List.of(student.getEmail()),
            "Complaint Received #" + complaint.getId(),
            "Your complaint has been recorded. Current status: " + complaint.getStatus());
    }

    public List<Complaint> getVisibleComplaints(String username) {
        AppUser user = getUserByUsername(username);
        if (user.getRole() == UserRole.MANAGEMENT) {
            return complaintRepository.findAllByOrderByCreatedAtDesc();
        }
        if (user.getRole() == UserRole.STAFF) {
            return complaintRepository.findAllByAssignedToOrderByCreatedAtDesc(user);
        }
        return complaintRepository.findAllByStudentOrderByCreatedAtDesc(user);
    }

    public Map<Long, List<ComplaintNote>> getNotesMap(List<Complaint> complaints) {
        Map<Long, List<ComplaintNote>> map = new HashMap<>();
        for (Complaint complaint : complaints) {
            map.put(complaint.getId(), complaintNoteRepository.findAllByComplaintOrderByCreatedAtAsc(complaint));
        }
        return map;
    }

    public Map<Long, List<ComplaintAttachment>> getAttachmentsMap(List<Complaint> complaints) {
        Map<Long, List<ComplaintAttachment>> map = new HashMap<>();
        for (Complaint complaint : complaints) {
            map.put(complaint.getId(), complaintAttachmentRepository.findAllByComplaintOrderByUploadedAtDesc(complaint));
        }
        return map;
    }

    public Map<String, Long> getStatusSummary() {
        Map<String, Long> counts = new HashMap<>();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            counts.put(status.name(), complaintRepository.countByStatus(status));
        }
        return counts;
    }

    public List<DepartmentCount> getDepartmentStats() {
        return complaintRepository.countByDepartment().stream()
            .map(row -> new DepartmentCount(String.valueOf(row[0]), ((Number) row[1]).longValue()))
            .toList();
    }

    public List<MonthlyPoint> getMonthlyTrend() {
        return complaintRepository.monthlyComplaintTrend().stream()
            .map(row -> new MonthlyPoint(String.valueOf(row[0]), ((Number) row[1]).longValue()))
            .toList();
    }

    public List<AppUser> getStaffUsers() {
        return appUserRepository.findAllByRoleOrderByFullNameAsc(UserRole.STAFF);
    }

    public AppUser getUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Complaint getComplaint(Long complaintId) {
        return complaintRepository.findById(complaintId)
            .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
    }

    @Transactional
    public void assignComplaint(String managerUsername, Long complaintId, Long staffUserId) {
        AppUser manager = getUserByUsername(managerUsername);
        if (manager.getRole() != UserRole.MANAGEMENT) {
            throw new IllegalArgumentException("Only management can assign complaints.");
        }

        Complaint complaint = getComplaint(complaintId);
        AppUser staff = appUserRepository.findById(staffUserId)
            .orElseThrow(() -> new IllegalArgumentException("Staff member not found"));

        if (staff.getRole() != UserRole.STAFF) {
            throw new IllegalArgumentException("Selected user is not staff.");
        }

        complaint.setAssignedTo(staff);
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }
        complaint.setEscalationDueAt(computeEscalationDue(complaint.getPriority()));
        complaintRepository.save(complaint);

        addNoteInternal(complaint, manager, "Assigned to " + staff.getFullName() + ".");
        notificationService.sendToMany(List.of(staff.getEmail(), complaint.getStudent().getEmail()),
            "Complaint Assigned #" + complaint.getId(),
            "Complaint " + complaint.getTitle() + " has been assigned to " + staff.getFullName() + ".");
    }

    @Transactional
    public void updateComplaintStatus(String actorUsername, Long complaintId, ComplaintStatus status) {
        AppUser actor = getUserByUsername(actorUsername);
        Complaint complaint = getComplaint(complaintId);

        boolean canUpdate = actor.getRole() == UserRole.MANAGEMENT
            || (actor.getRole() == UserRole.STAFF && complaint.getAssignedTo() != null
            && complaint.getAssignedTo().getId().equals(actor.getId()));

        if (!canUpdate) {
            throw new IllegalArgumentException("You are not allowed to update this complaint.");
        }

        complaint.setStatus(status);
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) {
            complaint.setEscalated(false);
        } else {
            complaint.setEscalationDueAt(computeEscalationDue(complaint.getPriority()));
        }

        complaintRepository.save(complaint);
        addNoteInternal(complaint, actor, "Status changed to " + status + ".");

        List<String> recipients = new ArrayList<>();
        recipients.add(complaint.getStudent().getEmail());
        if (complaint.getAssignedTo() != null) {
            recipients.add(complaint.getAssignedTo().getEmail());
        }
        notificationService.sendToMany(uniqueEmails(recipients),
            "Complaint Status Updated #" + complaint.getId(),
            "Complaint " + complaint.getTitle() + " status is now " + status + ".");
    }

    @Transactional
    public void addNote(String actorUsername, Long complaintId, String message) {
        AppUser actor = getUserByUsername(actorUsername);
        Complaint complaint = getComplaint(complaintId);

        boolean isOwner = complaint.getStudent().getId().equals(actor.getId());
        boolean isAssignedStaff = complaint.getAssignedTo() != null
            && complaint.getAssignedTo().getId().equals(actor.getId());
        boolean isManager = actor.getRole() == UserRole.MANAGEMENT;

        if (!(isOwner || isAssignedStaff || isManager)) {
            throw new IllegalArgumentException("You are not allowed to add notes on this complaint.");
        }

        addNoteInternal(complaint, actor, message.trim());
    }

    @Transactional
    public void addAttachment(String actorUsername, Long complaintId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a file.");
        }

        AppUser actor = getUserByUsername(actorUsername);
        Complaint complaint = getComplaint(complaintId);

        boolean isOwner = complaint.getStudent().getId().equals(actor.getId());
        boolean isAssignedStaff = complaint.getAssignedTo() != null
            && complaint.getAssignedTo().getId().equals(actor.getId());
        boolean isManager = actor.getRole() == UserRole.MANAGEMENT;

        if (!(isOwner || isAssignedStaff || isManager)) {
            throw new IllegalArgumentException("You are not allowed to upload attachments here.");
        }

        storeAttachments(complaint, actor, new MultipartFile[]{file});
        addNoteInternal(complaint, actor, "Attachment uploaded: " + file.getOriginalFilename());
    }

    public List<ComplaintAttachment> getAttachmentsForComplaint(Long complaintId) {
        return complaintAttachmentRepository.findAllByComplaintOrderByUploadedAtDesc(getComplaint(complaintId));
    }

    public ComplaintAttachment getAttachment(Long attachmentId) {
        return complaintAttachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
    }

    public boolean canAccessComplaint(String username, Complaint complaint) {
        AppUser user = getUserByUsername(username);
        if (user.getRole() == UserRole.MANAGEMENT) {
            return true;
        }
        if (user.getRole() == UserRole.STUDENT) {
            return complaint.getStudent().getId().equals(user.getId());
        }
        return complaint.getAssignedTo() != null && complaint.getAssignedTo().getId().equals(user.getId());
    }

    @Transactional
    public void autoEscalateOverdueComplaints() {
        List<Complaint> overdue = complaintRepository.findAllByEscalatedFalseAndEscalationDueAtBeforeAndStatusNotIn(
            LocalDateTime.now(), List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED)
        );

        if (overdue.isEmpty()) {
            return;
        }

        AppUser systemActor = getSystemActor();
        List<String> managers = getUsersByRoles(UserRole.MANAGEMENT).stream().map(AppUser::getEmail).toList();

        for (Complaint complaint : overdue) {
            complaint.setEscalated(true);
            complaintRepository.save(complaint);
            addNoteInternal(complaint, systemActor,
                "SLA breach detected. Complaint escalated to management.");

            List<String> recipients = new ArrayList<>(managers);
            recipients.add(complaint.getStudent().getEmail());
            if (complaint.getAssignedTo() != null) {
                recipients.add(complaint.getAssignedTo().getEmail());
            }

            notificationService.sendToMany(uniqueEmails(recipients),
                "Escalated Complaint #" + complaint.getId(),
                "Complaint " + complaint.getTitle() + " exceeded SLA and was escalated.");
        }
    }

    private void storeAttachments(Complaint complaint, AppUser actor, MultipartFile[] files) {
        if (files == null) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String storedName = fileStorageService.store(file);
            ComplaintAttachment attachment = new ComplaintAttachment();
            attachment.setComplaint(complaint);
            attachment.setUploadedBy(actor);
            attachment.setOriginalFileName(file.getOriginalFilename());
            attachment.setStoredFileName(storedName);
            attachment.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            attachment.setSize(file.getSize());
            complaintAttachmentRepository.save(attachment);
        }
    }

    private LocalDateTime computeEscalationDue(com.studenthelpdesk.model.TicketPriority priority) {
        LocalDateTime now = LocalDateTime.now();
        return switch (priority) {
            case URGENT -> now.plusHours(12);
            case HIGH -> now.plusDays(1);
            case MEDIUM -> now.plusDays(3);
            case LOW -> now.plusDays(7);
        };
    }

    private void addNoteInternal(Complaint complaint, AppUser author, String message) {
        ComplaintNote note = new ComplaintNote();
        note.setComplaint(complaint);
        note.setAuthor(author);
        note.setMessage(message);
        complaintNoteRepository.save(note);
    }

    private AppUser getSystemActor() {
        return appUserRepository.findByUsername("admin")
            .orElseGet(() -> appUserRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No users found for escalation logging")));
    }

    private List<AppUser> getUsersByRoles(UserRole... roles) {
        return appUserRepository.findAllByRoleIn(Arrays.asList(roles));
    }

    private List<String> uniqueEmails(List<String> emails) {
        return emails.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(e -> !e.isEmpty())
            .distinct()
            .collect(Collectors.toList());
    }
}
