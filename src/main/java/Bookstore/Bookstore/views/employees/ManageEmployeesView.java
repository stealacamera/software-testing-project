package Bookstore.Bookstore.views.employees;

import Bookstore.Bookstore.bll.dto.EmployeeDTO;
import Bookstore.Bookstore.views.IView;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ManageEmployeesView extends IView {
	private VBox pane = new VBox();
	private Button addBt = new Button("Add new employee"), modifyBt = new Button("Modify"), 
			deleteBt = new Button("Delete");
	private ListView<EmployeeDTO> employeesLv = new ListView<>();
	
	public ManageEmployeesView(ObservableList<EmployeeDTO> employees) {
		addBt.setId("add-btn");
		modifyBt.setId("modify-btn");
		deleteBt.setId("delete-btn");
		employeesLv.setId("employees-list");
		setId("manage-employees-view");
		
		setList(employees);
		createLayout();
		super.getChildren().add(pane);
	}
	
	public void setInsertListener(EventHandler<ActionEvent> action) {
		addBt.setOnAction(action);
	}
	
	public void setModifyListener(EventHandler<ActionEvent> action) {
		modifyBt.setOnAction(action);
	}
	
	public void setDeleteListener(EventHandler<ActionEvent> action) {
		deleteBt.setOnAction(action);
	}
	
	public int getSelectedIndex() {
		return employeesLv.getSelectionModel().getSelectedIndex();
	}
	
	public EmployeeDTO getSelectedItem() {
		return employeesLv.getSelectionModel().getSelectedItem();
	}
	
	public void refreshList(ObservableList<EmployeeDTO> employees) {
		if(employees != null)
			employeesLv.setItems(employees);
		
		employeesLv.refresh();
	}
	
	private void createLayout() {
		addBt.setPrefHeight(40);
		modifyBt.setPrefHeight(40);
		deleteBt.setPrefHeight(40);
		employeesLv.setPrefWidth(300);
		
		HBox btPane = new HBox(addBt, modifyBt, deleteBt);
		btPane.setSpacing(10);
		btPane.setPrefWidth(200);
		
		pane.getChildren().addAll(btPane, employeesLv);
		pane.setSpacing(20);
		pane.setPadding(new Insets(30));		
	}
	
	private void setList(ObservableList<EmployeeDTO> employees) {
		employeesLv.setItems(employees);
		employeesLv.setFocusTraversable(false);
		
		employeesLv.setCellFactory(cell -> new EmployeesListViewCell());
	}
	
	private static class EmployeesListViewCell extends ListCell<EmployeeDTO> {
		@Override
		protected void updateItem(EmployeeDTO employee, boolean empty) {
			super.updateItem(employee, empty);
			
			if(empty || employee == null) {
				setText(null);
				setGraphic(null);
			} else {
				if(employee.getAccessLvl() == 3) {
					setDisable(true);
					setTextFill(Color.GRAY);
				}
				
				setText(employee.toString());
			}
		}
	}
}
