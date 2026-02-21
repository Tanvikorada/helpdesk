package com.studenthelpdesk.dto;

import com.studenthelpdesk.model.ComplaintStatus;

public class StatusUpdateForm {

    private ComplaintStatus status;

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }
}
