package com.petshop.petshop.controller;

import com.petshop.petshop.dto.AdminDashboardSummaryDTO;
import com.petshop.petshop.service.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public String hienThiDashboard(Model model) {
        AdminDashboardSummaryDTO summary = adminDashboardService.layTongQuan();
        model.addAttribute("summary", summary);
        return "staff/dashboard/index";
    }
}
