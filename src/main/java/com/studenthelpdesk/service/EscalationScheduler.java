package com.studenthelpdesk.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EscalationScheduler {

    private final HelpdeskService helpdeskService;

    public EscalationScheduler(HelpdeskService helpdeskService) {
        this.helpdeskService = helpdeskService;
    }

    @Scheduled(fixedDelayString = "${app.escalation.fixed-delay-ms:300000}")
    public void escalateOverdueComplaints() {
        helpdeskService.autoEscalateOverdueComplaints();
    }
}
