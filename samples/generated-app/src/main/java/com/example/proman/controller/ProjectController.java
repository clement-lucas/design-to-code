package com.example.proman.controller;

import com.example.proman.entity.Client;
import com.example.proman.entity.Organization;
import com.example.proman.entity.Project;
import com.example.proman.form.ProjectCreateForm;
import com.example.proman.form.ProjectSearchForm;
import com.example.proman.form.ProjectUpdateForm;
import com.example.proman.security.LoginUserDetails;
import com.example.proman.service.CodeNameService;
import com.example.proman.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/project")
public class ProjectController {

    private static final int PAGE_SIZE = 20;

    private final ProjectService projectService;
    private final CodeNameService codeNameService;

    public ProjectController(ProjectService projectService, CodeNameService codeNameService) {
        this.projectService = projectService;
        this.codeNameService = codeNameService;
    }

    // ==================== プロジェクト登録 (WA10201) ====================

    @GetMapping("/create")
    public String createInit(@ModelAttribute("form") ProjectCreateForm form, Model model) {
        addCodeAttributes(model);
        addOrganizationAttributes(model);
        return "project/create";
    }

    @PostMapping("/create/confirm")
    public String createConfirm(@Valid @ModelAttribute("form") ProjectCreateForm form,
                                BindingResult result, Model model) {
        if (form.getProjectStartDate() != null && form.getProjectEndDate() != null
                && form.getProjectEndDate().isBefore(form.getProjectStartDate())) {
            result.rejectValue("projectEndDate", "validator.periodConsistencyCheck.message.ProjectCreateForm");
        }
        if (result.hasErrors()) {
            addCodeAttributes(model);
            addOrganizationAttributes(model);
            return "project/create";
        }
        addCodeAttributes(model);
        addOrganizationAttributes(model);
        resolveClientName(form.getClientId(), model);
        return "project/confirmCreate";
    }

    @PostMapping("/create/execute")
    public String createExecute(@Valid @ModelAttribute("form") ProjectCreateForm form,
                                BindingResult result, Model model,
                                @RequestParam(value = "back", required = false) String back) {
        if (back != null) {
            addCodeAttributes(model);
            addOrganizationAttributes(model);
            return "project/create";
        }
        if (result.hasErrors()) {
            addCodeAttributes(model);
            addOrganizationAttributes(model);
            return "project/create";
        }
        Project project = mapCreateFormToEntity(form);
        projectService.createProject(project);
        return "redirect:/project/create/complete";
    }

    @GetMapping("/create/complete")
    public String createComplete() {
        return "project/completeCreate";
    }

    // ==================== プロジェクト検索 (WA10202) ====================

    @GetMapping("/search")
    public String searchInit(@ModelAttribute("searchForm") ProjectSearchForm form, Model model) {
        addCodeAttributes(model);
        addOrganizationAttributes(model);
        return "project/search";
    }

    @GetMapping("/search/search")
    public String searchExecute(@ModelAttribute("searchForm") ProjectSearchForm form, Model model) {
        addCodeAttributes(model);
        addOrganizationAttributes(model);

        int page = (form.getPageNumber() != null) ? form.getPageNumber() : 0;
        Page<Project> results = projectService.searchProjects(
                form.getProjectName(), form.getProjectType(), form.getProjectClass(),
                form.getOrganizationId(), form.getClientId(),
                form.getProjectStartDateFrom(), form.getProjectStartDateTo(),
                form.getProjectEndDateFrom(), form.getProjectEndDateTo(),
                PageRequest.of(page, PAGE_SIZE, Sort.by("projectId").ascending()));

        if (results.isEmpty()) {
            model.addAttribute("errorMessage", "条件に一致するプロジェクトがありません。");
        }
        model.addAttribute("projects", results);
        model.addAttribute("searchExecuted", true);
        return "project/search";
    }

    // ==================== プロジェクト詳細 (WA10204) ====================

    @GetMapping("/detail")
    public String detail(@RequestParam("projectId") Integer projectId, Model model) {
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        model.addAttribute("project", project);
        addCodeAttributes(model);
        resolveNames(project, model);
        return "project/detail";
    }

    // ==================== プロジェクト更新 (WA10203) ====================

    @GetMapping("/update")
    public String updateInit(@RequestParam("projectId") Integer projectId, Model model) {
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        ProjectUpdateForm form = mapEntityToUpdateForm(project);
        model.addAttribute("form", form);
        addCodeAttributes(model);
        addOrganizationAttributes(model);
        resolveClientName(project.getClientId(), model);
        return "project/update";
    }

    @PostMapping("/update/confirm")
    public String updateConfirm(@Valid @ModelAttribute("form") ProjectUpdateForm form,
                                BindingResult result, Model model) {
        if (form.getProjectStartDate() != null && form.getProjectEndDate() != null
                && form.getProjectEndDate().isBefore(form.getProjectStartDate())) {
            result.rejectValue("projectEndDate", "validator.periodConsistencyCheck.message.ProjectUpdateForm");
        }
        if (result.hasErrors()) {
            addCodeAttributes(model);
            addOrganizationAttributes(model);
            return "project/update";
        }
        addCodeAttributes(model);
        addOrganizationAttributes(model);
        resolveClientName(form.getClientId(), model);
        return "project/confirmUpdate";
    }

    @PostMapping("/update/execute")
    public String updateExecute(@Valid @ModelAttribute("form") ProjectUpdateForm form,
                                BindingResult result, Model model,
                                @RequestParam(value = "back", required = false) String back) {
        if (back != null) {
            addCodeAttributes(model);
            addOrganizationAttributes(model);
            return "project/update";
        }
        if (result.hasErrors()) {
            addCodeAttributes(model);
            addOrganizationAttributes(model);
            return "project/update";
        }
        Project project = projectService.findById(form.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        mapUpdateFormToEntity(form, project);
        projectService.updateProject(project);
        return "redirect:/project/update/complete";
    }

    @GetMapping("/update/complete")
    public String updateComplete() {
        return "project/completeUpdate";
    }

    // ==================== Helper Methods ====================

    private void addCodeAttributes(Model model) {
        model.addAttribute("projectTypes", codeNameService.getCodeMap(CodeNameService.PROJECT_TYPE));
        model.addAttribute("projectClasses", codeNameService.getCodeMap(CodeNameService.PROJECT_CLASS));
        model.addAttribute("industryClasses", codeNameService.getCodeMap(CodeNameService.INDUSTRY_CLASS));
    }

    private void addOrganizationAttributes(Model model) {
        List<Organization> organizations = projectService.findAllOrganizations();
        model.addAttribute("organizations", organizations);
    }

    private void resolveClientName(Integer clientId, Model model) {
        if (clientId != null) {
            projectService.findClientById(clientId).ifPresent(c ->
                    model.addAttribute("clientName", c.getClientName()));
        }
    }

    private void resolveNames(Project project, Model model) {
        resolveClientName(project.getClientId(), model);
        projectService.findOrganizationById(project.getOrganizationId()).ifPresent(o ->
                model.addAttribute("organizationName", o.getOrganizationName()));
        model.addAttribute("projectTypeName",
                codeNameService.getCodeName(CodeNameService.PROJECT_TYPE, project.getProjectType()));
        model.addAttribute("projectClassName",
                codeNameService.getCodeName(CodeNameService.PROJECT_CLASS, project.getProjectClass()));
    }

    private Project mapCreateFormToEntity(ProjectCreateForm form) {
        Project p = new Project();
        p.setProjectName(form.getProjectName());
        p.setProjectType(form.getProjectType());
        p.setProjectClass(form.getProjectClass());
        p.setProjectStartDate(form.getProjectStartDate());
        p.setProjectEndDate(form.getProjectEndDate());
        p.setOrganizationId(form.getOrganizationId());
        p.setClientId(form.getClientId());
        p.setProjectManager(form.getProjectManager());
        p.setProjectLeader(form.getProjectLeader());
        p.setNote(form.getNote());
        p.setSales(form.getSales());
        return p;
    }

    private ProjectUpdateForm mapEntityToUpdateForm(Project p) {
        ProjectUpdateForm f = new ProjectUpdateForm();
        f.setProjectId(p.getProjectId());
        f.setProjectName(p.getProjectName());
        f.setProjectType(p.getProjectType());
        f.setProjectClass(p.getProjectClass());
        f.setProjectStartDate(p.getProjectStartDate());
        f.setProjectEndDate(p.getProjectEndDate());
        f.setOrganizationId(p.getOrganizationId());
        f.setClientId(p.getClientId());
        f.setProjectManager(p.getProjectManager());
        f.setProjectLeader(p.getProjectLeader());
        f.setNote(p.getNote());
        f.setSales(p.getSales());
        f.setVersionNo(p.getVersionNo());
        return f;
    }

    private void mapUpdateFormToEntity(ProjectUpdateForm form, Project p) {
        p.setProjectName(form.getProjectName());
        p.setProjectType(form.getProjectType());
        p.setProjectClass(form.getProjectClass());
        p.setProjectStartDate(form.getProjectStartDate());
        p.setProjectEndDate(form.getProjectEndDate());
        p.setOrganizationId(form.getOrganizationId());
        p.setClientId(form.getClientId());
        p.setProjectManager(form.getProjectManager());
        p.setProjectLeader(form.getProjectLeader());
        p.setNote(form.getNote());
        p.setSales(form.getSales());
        p.setVersionNo(form.getVersionNo());
    }
}
