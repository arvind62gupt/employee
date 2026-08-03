package com.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;


@SpringBootTest
@ActiveProfiles("test") // 👈 Activates the test profile
class EmployeeApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() throws SQLException {

	}
}
