/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cgpapredict;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author mahmud
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private ComboBox<Number> semesterIdField;
    ObservableList<Number> semesterList;
    ObservableList<Student> allStudentList;
    ObservableList<StudentDetails>studentDetailsList;
    private Connection connection;
    private Statement statement;
    private ResultSet resultset;
    private int selectedSemesterId;
    
    @FXML
    private ListView<Student> allStudentListField;
    @FXML
    private TextField studentNameField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TableView<StudentDetails> allStudentTableView;
     @FXML
    private TableColumn<StudentDetails, String> courseCodeView;
    @FXML
    private TableColumn<StudentDetails, String> courseTitleView;
    @FXML
    private TableColumn<StudentDetails, String> creditView;
    @FXML
    private TableColumn<StudentDetails, String> facultyView;
    @FXML
    private TableColumn<StudentDetails, String> predictGradeView;
    @FXML
    private TableColumn<StudentDetails, String> actualGradeView;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        semesterList=FXCollections.observableArrayList();
        semesterIdField.setItems(semesterList);
        try {
            final String DB_URL="jdbc:mysql://localhost/predictdb";
            final String DB_USERNAME="root";
            final String DB_PASS="";
            connection=DriverManager.getConnection(DB_URL,DB_USERNAME,DB_PASS);
            statement=connection.createStatement();
            String query="SELECT DISTINCT semesterId FROM registration";
            resultset=statement.executeQuery(query);
            while(resultset.next())
            {
                int semesterId=resultset.getInt("semesterId");
                semesterList.add(semesterId);
            }
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    @FXML
    private void handleSemesterIdSelected(ActionEvent event) {
        allStudentList=FXCollections.observableArrayList();
        allStudentListField.setItems(allStudentList);
        selectedSemesterId=(int) semesterIdField.getSelectionModel().getSelectedItem();
        String query="SELECT R.studentId,S.studentName FROM registration as R INNER JOIN student AS S ON R.studentId=S.studentId WHERE semesterId='"+selectedSemesterId+"'";
        try {
            resultset=statement.executeQuery(query);
            while(resultset.next())
            {
                String studentId=resultset.getString("studentId");
                String studentName=resultset.getString("studentName");
                Student student =new Student(studentId, studentName);
                allStudentList.add(student);
            }
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleListViewSelected(MouseEvent event) {
        studentDetailsList=FXCollections.observableArrayList();
        Student s=allStudentListField.getSelectionModel().getSelectedItem();
        studentIdField.setText(s.getStudentId());
        studentNameField.setText(s.getStudentName());
        String query="SELECT G.courseCode,C.courseTitle,C.credits,G.facultyInitials,G.grade FROM grades AS G INNER JOIN course as C ON G.courseCode=C.courseCode WHERE G.studentId='"+s.getStudentId()+"' AND G.semesterId='"+selectedSemesterId+"'";
        try {
            resultset=statement.executeQuery(query);
            while(resultset.next())
            {
                String courseCode=resultset.getString("courseCode");
                String courseTitle=resultset.getString("courseTitle");
                String credits=resultset.getString("credits");
                String facultyInitials=resultset.getString("facultyInitials");
                String predictGrade="0";
                String grade=resultset.getString("grade");
                StudentDetails sd=new StudentDetails(courseCode, courseTitle, credits, facultyInitials, predictGrade, grade);
                studentDetailsList.add(sd);
                allStudentTableView.setItems(studentDetailsList);
            }
            courseCodeView.setCellValueFactory(x->new SimpleStringProperty(x.getValue().getCourseCode()));
            courseTitleView.setCellValueFactory(x->new SimpleStringProperty(x.getValue().getCourseTitle()));
            creditView.setCellValueFactory(x->new SimpleStringProperty(x.getValue().getCredits()));
            facultyView.setCellValueFactory(x->new SimpleStringProperty(x.getValue().getFacultyInitials()));
            predictGradeView.setCellValueFactory(x->new SimpleStringProperty(x.getValue().getPredictGrade()));
            actualGradeView.setCellValueFactory(x->new SimpleStringProperty(x.getValue().getGrade()));
            
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
