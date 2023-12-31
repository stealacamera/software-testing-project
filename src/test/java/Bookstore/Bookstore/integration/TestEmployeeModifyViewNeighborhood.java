package Bookstore.Bookstore.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import Bookstore.Bookstore.bll.dto.EmployeeDTO;
import Bookstore.Bookstore.bll.dto.UserDTO;
import Bookstore.Bookstore.bll.services.EmployeeService;
import Bookstore.Bookstore.bll.services.iservices.IEmployeeService;
import Bookstore.Bookstore.commons.utils.Utils;
import Bookstore.Bookstore.controllers.EmployeesController;
import Bookstore.Bookstore.dal.repositories.DbContext;
import Bookstore.Bookstore.dal.repositories.EmployeeRepository;
import javafx.scene.Scene;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TestEmployeeModifyViewNeighborhood extends TestNeighborHoodBase {
	private static FxRobot robot = new FxRobot();
	private static EmployeeDTO employee;
	private static IEmployeeService employeeService;
	private static EmployeesController controller;
	
	@BeforeAll
	static void setUp() {
		employeeService = new EmployeeService(new EmployeeRepository(Utils.testingUserDataDirPath, new DbContext()));
		controller = new EmployeesController(employeeService);
		
		try {
			employee = new EmployeeDTO(
				new UserDTO("foobar", "foo bar", "foo@gmail.com", "password123", "069 123 1234", LocalDate.now()), 
				100, 1);
			
			employeeService.add(employee);
			employee = employeeService.get(0);
		} catch (Exception e) {
			e.printStackTrace(); // Won't be thrown
		}
	}
	
	@Start
	private void start(Stage stage) {				
		stage.setScene(new Scene(controller.getModifyView(employee)));
		stage.show();
	}
	
	@ParameterizedTest
	@MethodSource("provideInvalidValues")
	void testModifyEmployee_InvalidValues(String fieldId, int nrCharsToRemove, String newValue, String errorMessage) {
		// Input change		
		robot.clickOn(fieldId).eraseText(nrCharsToRemove).write(newValue);
		robot.clickOn("#submit-btn");
				
		// Check for error pop-up
		WaitForAsyncUtils.waitForFxEvents();	
		FxAssert.verifyThat("#alert_error_message", LabeledMatchers.hasText(errorMessage));
	
		// Check database entity hasn't changed
		assertEquals(employee, employeeService.get(0));
	}
	
	@ParameterizedTest
	@MethodSource("provideValidValues")
	void testModifyEmployee_ValidValues(String fieldId, int nrCharsToRemove, String newValue, Consumer<EmployeeDTO> updateModel) {
		// Input change
		robot.clickOn(fieldId).eraseText(nrCharsToRemove).write(newValue);
		robot.clickOn("#submit-btn");
		
		// Check database entity has changed
		updateModel.accept(employee);
		assertEquals(employee, employeeService.get(0));
	}
	
	private static Stream<Arguments> provideInvalidValues() {
		return Stream.of(
			Arguments.of("#username", employee.getUsername().length(), "", "Input fields cannot be empty: Please enter a value for username"),
			Arguments.of("#email", employee.getEmail().length(), "foo", "Incorrect email format: Correct format is (2 characters)@gmail.com"),
			Arguments.of("#phone-nr", employee.getPhoneNum().length(), "123", "Incorrect phone number format: Correct format is 06X XXX XXXX")
		);
	}
	
	private static Stream<Arguments> provideValidValues() {
		return Stream.of(
			Arguments.of("#username", employee.getUsername().length(), "newUsername", (Consumer<EmployeeDTO>) employee -> employee.setUsername("newUsername")),
			Arguments.of("#email", employee.getEmail().length(), "newEmail@gmail.com", (Consumer<EmployeeDTO>) employee -> employee.setEmail("newEmail@gmail.com")),
			Arguments.of("#phone-nr", employee.getPhoneNum().length(), "068 789 7890", (Consumer<EmployeeDTO>) employee -> employee.setPhoneNum("068 789 7890"))
		);
	}
}
