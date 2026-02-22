package com.studenthelpdesk.controller;

import com.studenthelpdesk.dto.ComplaintForm;
import com.studenthelpdesk.dto.ManagementUserForm;
import com.studenthelpdesk.model.ComplaintStatus;
import com.studenthelpdesk.model.UserRole;
import com.studenthelpdesk.service.FileStorageService;
import com.studenthelpdesk.service.HelpdeskService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class DashboardController {

    private final HelpdeskService helpdeskService;
    private final FileStorageService fileStorageService;

    public DashboardController(HelpdeskService helpdeskService, FileStorageService fileStorageService) {
        this.helpdeskService = helpdeskService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String username = principal.getName();
        var user = helpdeskService.getUserByUsername(username);
        var complaints = helpdeskService.getVisibleComplaints(username);

        if (!model.containsAttribute("complaintForm")) {
            model.addAttribute("complaintForm", new ComplaintForm());
        }
        if (!model.containsAttribute("managementUserForm")) {
            model.addAttribute("managementUserForm", new ManagementUserForm());
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("isStudent", user.getRole() == UserRole.STUDENT);
        model.addAttribute("isFaculty", user.getRole() == UserRole.FACULTY);
        model.addAttribute("isStaff", user.getRole() == UserRole.STAFF || user.getRole() == UserRole.FACULTY);
        model.addAttribute("isManagement", user.getRole() == UserRole.MANAGEMENT);
        model.addAttribute("provisionRoles", new UserRole[]{UserRole.FACULTY, UserRole.STAFF, UserRole.MANAGEMENT});
        model.addAttribute("complaints", complaints);
        model.addAttribute("priorities", com.studenthelpdesk.model.TicketPriority.values());
        model.addAttribute("categories", com.studenthelpdesk.model.ComplaintCategory.values());
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("staffUsers", helpdeskService.getStaffUsers());
        model.addAttribute("notesMap", helpdeskService.getNotesMap(complaints));
        model.addAttribute("attachmentsMap", helpdeskService.getAttachmentsMap(complaints));
        model.addAttribute("statusSummary", helpdeskService.getStatusSummary());
        model.addAttribute("departmentStats", helpdeskService.getDepartmentStats());
        model.addAttribute("monthlyTrend", helpdeskService.getMonthlyTrend());

        return "dashboard";
    }

    @PostMapping("/student/complaints")
    public String createComplaint(@Valid ComplaintForm complaintForm,
                                  BindingResult bindingResult,
                                  @RequestParam(name = "files", required = false) MultipartFile[] files,
                                  Model model,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return dashboard(model, principal);
        }

        try {
            helpdeskService.createComplaint(principal.getName(), complaintForm, files);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint submitted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/management/users")
    public String createUserByManagement(@Valid ManagementUserForm managementUserForm,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes,
                                         Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete all required user fields.");
            return "redirect:/dashboard";
        }

        try {
            helpdeskService.registerUserByManagement(principal.getName(), managementUserForm);
            redirectAttributes.addFlashAttribute("successMessage", "Team member account created.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/management/complaints/{id}/assign")
    public String assignComplaint(@PathVariable Long id,
                                  @RequestParam Long staffUserId,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            helpdeskService.assignComplaint(principal.getName(), id, staffUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint assigned.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/staff/complaints/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam ComplaintStatus status,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            helpdeskService.updateComplaintStatus(principal.getName(), id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint status updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/complaints/{id}/notes")
    public String addNote(@PathVariable Long id,
                          @RequestParam String message,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        if (message == null || message.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Note cannot be empty.");
            return "redirect:/dashboard";
        }

        try {
            helpdeskService.addNote(principal.getName(), id, message);
            redirectAttributes.addFlashAttribute("successMessage", "Note added.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/complaints/{id}/attachments")
    public String uploadAttachment(@PathVariable Long id,
                                   @RequestParam("file") MultipartFile file,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            helpdeskService.addAttachment(principal.getName(), id, file);
            redirectAttributes.addFlashAttribute("successMessage", "Attachment uploaded.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id, Principal principal) throws IOException {
        var attachment = helpdeskService.getAttachment(id);
        if (!helpdeskService.canAccessComplaint(principal.getName(), attachment.getComplaint())) {
            return ResponseEntity.status(403).build();
        }

        Path filePath = fileStorageService.resolve(attachment.getStoredFileName());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(attachment.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
            .body(resource);
    }
}
