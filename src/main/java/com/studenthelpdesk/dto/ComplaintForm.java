package com.studenthelpdesk.dto;

import com.studenthelpdesk.model.ComplaintCategory;
import com.studenthelpdesk.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;

public class ComplaintForm {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private TicketPriority priority = TicketPriority.MEDIUM;

    private ComplaintCategory category = ComplaintCategory.OTHER;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }
}
