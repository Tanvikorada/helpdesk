package com.studenthelpdesk.controller;

import com.studenthelpdesk.dto.RegisterForm;
import com.studenthelpdesk.service.HelpdeskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class AuthController {

    private final HelpdeskService helpdeskService;

    public AuthController(HelpdeskService helpdeskService) {
        this.helpdeskService = helpdeskService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterForm registerForm,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerForm", registerForm);
            return "register";
        }

        try {
            helpdeskService.registerStudent(registerForm);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registerForm", registerForm);
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please login.");
        return "redirect:/login";
    }
}
