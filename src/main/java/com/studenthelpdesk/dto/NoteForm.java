package com.studenthelpdesk.dto;

import jakarta.validation.constraints.NotBlank;

public class NoteForm {

    @NotBlank(message = "Note message is required")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
