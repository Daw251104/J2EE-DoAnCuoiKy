/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})  // xử lý cả /, /home
    public String home() {
        return "redirect:/sanpham";  // redirect để đồng bộ hiển thị dữ liệu sản phẩm
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "redirect:/admin/accounts";
    }
}