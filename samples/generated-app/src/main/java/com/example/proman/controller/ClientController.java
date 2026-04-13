package com.example.proman.controller;

import com.example.proman.entity.Client;
import com.example.proman.form.ClientSearchForm;
import com.example.proman.service.CodeNameService;
import com.example.proman.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final ProjectService projectService;
    private final CodeNameService codeNameService;

    public ClientController(ProjectService projectService, CodeNameService codeNameService) {
        this.projectService = projectService;
        this.codeNameService = codeNameService;
    }

    @GetMapping("/search")
    public String searchInit(@ModelAttribute("searchForm") ClientSearchForm form, Model model) {
        model.addAttribute("industryClasses", codeNameService.getCodeMap(CodeNameService.INDUSTRY_CLASS));
        return "client/search";
    }

    @GetMapping("/search/search")
    public String searchExecute(@ModelAttribute("searchForm") ClientSearchForm form, Model model) {
        model.addAttribute("industryClasses", codeNameService.getCodeMap(CodeNameService.INDUSTRY_CLASS));
        List<Client> clients = projectService.searchClients(form.getClientName(), form.getIndustryCode());
        model.addAttribute("clients", clients);
        model.addAttribute("searchExecuted", true);
        return "client/search";
    }
}
