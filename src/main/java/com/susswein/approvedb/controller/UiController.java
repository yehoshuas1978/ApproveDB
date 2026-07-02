package com.susswein.approvedb.controller;

import com.susswein.approvedb.model.ChangeRequest;
import com.susswein.approvedb.repository.ChangeRequestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class UiController {

    private final ChangeRequestRepository changeRequestRepository;

    public UiController(ChangeRequestRepository changeRequestRepository) {
        this.changeRequestRepository = changeRequestRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("requests", changeRequestRepository.findByStateOrderByReceivedAtDesc("PENDING"));
        return "dashboard";
    }

    @GetMapping("/requests/{id}")
    public String requestDetail(@PathVariable String id, Model model) {
        ChangeRequest request = changeRequestRepository.findById(id).orElseThrow();
        model.addAttribute("request", request);
        return "request-detail";
    }
}
