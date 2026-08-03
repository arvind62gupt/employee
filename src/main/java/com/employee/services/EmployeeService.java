package com.employee.services;

import com.employee.model.Employee;
import com.employee.repositories.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository repository;
    private final EmailService emailService;
    private final EmployeeProducer producer;

    public EmployeeService(EmployeeRepository repository,
                           EmailService emailService,
                           EmployeeProducer producer) {
        this.repository = repository;
        this.emailService = emailService;
        this.producer = producer;
    }

    public Employee saveEmployee(Employee employee) {

        log.info("Saving employee: {} {}",
                employee.getFirstName(),
                employee.getLastName());
        if (repository.findByEmail(employee.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "Email already exists: " + employee.getEmail());
        }
        Employee savedEmployee = repository.save(employee);

        log.info("Employee saved successfully. ID: {}, Email: {}",
                savedEmployee.getId(),
                savedEmployee.getEmail());

        producer.sendMessage(
                "ADD|" + savedEmployee.getEmail() +
                        "|" + savedEmployee.getFirstName()
        );
        log.info("Email notification sent to {}", savedEmployee.getEmail());

          return savedEmployee;
    }

    public Employee updateEmployee(Long id, Employee employee) {

        log.info("Updating employee with ID: {}", id);

        Employee existingEmployee = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        repository.findByEmail(employee.getEmail())
                .ifPresent(emp -> {
                    if (!emp.getId().equals(id)) {
                        throw new RuntimeException(
                                "Email already exists: "
                                        + employee.getEmail());
                    }
                });

        existingEmployee.setFirstName(employee.getFirstName());
        existingEmployee.setLastName(employee.getLastName());
        existingEmployee.setEmail(employee.getEmail());

        Employee updatedEmployee = repository.save(existingEmployee);

        log.info("Employee updated successfully. ID: {}, Email: {}",
                updatedEmployee.getId(),
                updatedEmployee.getEmail());

        producer.sendMessage(
                "UPDATE|" + updatedEmployee.getEmail() +
                        "|" + updatedEmployee.getFirstName()
        );
        log.info("Update email notification sent to {}",
                updatedEmployee.getEmail());

        return updatedEmployee;
    }

    public void deleteEmployee(Long id) {

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        repository.deleteById(id);

        log.info("Employee deleted successfully. ID: {}, Email: {}",
                employee.getId(),
                employee.getEmail());

        producer.sendMessage(
                "DELETE|" + employee.getEmail()
                        + "|" + employee.getFirstName());

        log.info("Kafka delete notification published for {}",
                employee.getEmail());
    }
}