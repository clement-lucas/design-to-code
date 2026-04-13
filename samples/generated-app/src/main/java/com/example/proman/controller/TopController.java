package com.example.proman.controller;

import com.example.proman.security.LoginUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {

    @GetMapping("/")
    public String top(@AuthenticationPrincipal LoginUserDetails user, Model model) {
        model.addAttribute("userName", user.getKanjiName());
        model.addAttribute("isPm", user.isPm());
        return "top";
    }
}
