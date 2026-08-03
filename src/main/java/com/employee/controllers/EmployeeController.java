package com.employee.controllers;

import com.employee.model.Employee;
import com.employee.repositories.EmployeeRepository;
import com.employee.services.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService,
                              EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }
    @GetMapping
    public String viewHomePage(Model model) {
        log.info("Fetching all employees");

        model.addAttribute("listEmployees", employeeRepository.findAll());
        return "index";
    }

    @GetMapping("/showNewEmployeeForm")
    public String showNewEmployeeForm(Model model) {

        log.info("Opening New Employee Form");

        Employee employee = new Employee();
        model.addAttribute("employee", employee);
        return "new_employee";
    }

    @PostMapping("/saveEmployee")
    public String saveEmployee(
            @ModelAttribute("employee") Employee employee,
            RedirectAttributes redirectAttributes) {
        try {
            if (employee.getId() == null) {
                employeeService.saveEmployee(employee);
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Employee added successfully!");
            } else {
                employeeService.updateEmployee(
                        employee.getId(),
                        employee);
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Employee updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
            return "redirect:/employees";
        }
        return "redirect:/employees";
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String showFormForUpdate(@PathVariable Long id, Model model) {

        log.info("Updating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));

        model.addAttribute("employee", employee);
        return "update_employee";
    }

    @GetMapping("/deleteEmployee/{id}")
    public String deleteEmployee(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {

        employeeService.deleteEmployee(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Employee deleted successfully!");

        return "redirect:/employees";
    }
}