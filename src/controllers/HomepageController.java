package controllers;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import bll.IServices.IBillService;
import bll.IServices.IBookInventoryService;
import bll.IServices.IBookPurchaseService;
import bll.IServices.ICategoryService;
import bll.IServices.IEmployeeService;
import bll.dto.BookInventoryDTO;
import controllers.books.BillController;
import controllers.books.BookController;
import exceptions.EmptyInputException;
import exceptions.ExistingObjectException;
import exceptions.IncorrectPermissionsException;
import exceptions.NonPositiveInputException;
import exceptions.WrongFormatException;
import exceptions.WrongLengthException;
import models.utilities.Role;
import startup.Session;
import utils.Utils;
import views.ChangePasswordView;
import views.HomepageView;
import views.IView;
import views.books.AddCategoryView;

public class HomepageController {
	private IBookInventoryService bookInventoryService;
	private ICategoryService categoryService;
	private IBillService billService;
	private IBookPurchaseService bookPurchaseService;
	private IEmployeeService employeeService;
	
	public HomepageController(
			IBookInventoryService bookInventoryService, ICategoryService categoryService,
			IBillService billService, IBookPurchaseService bookPurchaseService,
			IEmployeeService employeeService) {
		this.bookInventoryService = bookInventoryService;
		this.categoryService = categoryService;
		this.billService = billService;
		this.bookPurchaseService = bookPurchaseService;
		this.employeeService = employeeService;
	}
	
	public IView getIndexView() {
		HomepageView view = new HomepageView();
		
		List<String> lowStockBookTitles = getLowStockBooks();
		if(Session.getCurrentUser().getAccessLvl() == 2 && lowStockBookTitles.size() != 0)
			view.showLowStockWarning(getLowStockBooks());
		
		view.setChangePasswordListener(e -> Utils.createPopupStage("Change password", getChangePasswordView()).showAndWait());
		view.setCategoryFormListener(e -> Utils.createPopupStage("Add new category", getAddCategoryView()).showAndWait());
		
		view.createButtons(
			Session.getCurrentUser().getPermissions(), 
			(permission, pane, goBackBtn) -> {
				Map.Entry<String, IView> result = getHomeActionView(permission);
				
				return (e -> {
					if(view != null) {
						Utils.getCurrentStage(e).setTitle(result.getKey());
						pane.setCenter(result.getValue());
						goBackBtn.setVisible(true);
					}
				});			
			}
		);
		
		return view;
	}
	
	private List<String> getLowStockBooks() {
		ArrayList<String> bookTitles = new ArrayList<>();
		
		for(BookInventoryDTO book: bookInventoryService.getAll())
			if(book.getStock() <= 5)
				bookTitles.add(book.getBook().getTitle());
		
		return Collections.unmodifiableList(bookTitles);
	}
	
	private IView getChangePasswordView() {
		ChangePasswordView view = new ChangePasswordView();

		view.setSubmitAction(e -> {
			try {				
				if(employeeService.changePassword(Session.getCurrentUser(), view.getCurrentPassword(), view.getNewPassword()))
					Utils.getCurrentStage(e).close();
				else
					view.displayError("Incorrect current password");
			} catch(Exception ex) {
				view.displayError(ex.getLocalizedMessage());
			}
		});
		
		return view;
	}
	
	private IView getAddCategoryView() {
		AddCategoryView view = new AddCategoryView();
		
		view.setAddAction(e -> {
			try {
				categoryService.add(view.submitForm());
				view.clearForm();
			} catch(ExistingObjectException | EmptyInputException | NonPositiveInputException | WrongFormatException | WrongLengthException
					| IncorrectPermissionsException ex) {
				view.displayError(ex.getMessage());
			}
		});

		return view;
	}
	
	private Map.Entry<String, IView> getHomeActionView(Role permission) {
		String viewTitle = null;
		IView view = null;
		
		switch(permission) {
			case CREATE_BILL:
				view = new BillController(billService, bookInventoryService).getIndexView();
				viewTitle = "Create a bill";
				break;
			case MANAGE_BOOKS:
				view = new BookController(bookInventoryService, bookPurchaseService, categoryService).getIndexView();
				viewTitle = "Manage books";
				break;
			case GET_BOOK_STATS:
				view = new StatisticsController(billService, employeeService, bookPurchaseService).getBookExpensesView();
				viewTitle = "Book cash flow";
				break;
			case GET_REVENUE_STATS:
				view = new StatisticsController(billService, employeeService, bookPurchaseService).getCashFlowStatsView();
				viewTitle = "Bookstore cash flow";
				break;
			case GET_LIBR_PERFORMANCE:
				view = new StatisticsController(billService, employeeService, bookPurchaseService).getLibrarianPerformanceView();
				viewTitle = "Librarians' performance";
				break;
			case MANAGE_EMPLOYEES:
				view = new EmployeesController(employeeService).getIndexView();
				viewTitle = "Manage employees";
				break;
			default: break;
		}
		
		return new AbstractMap.SimpleEntry<String, IView>(viewTitle, view);
	}
}
