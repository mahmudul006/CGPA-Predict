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
    ObservableList<Student> newstudentid;
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
    @FXML
    private TextField searchField;
    
    
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
        searchField.setText("");
        allStudentList=FXCollections.observableArrayList();
        allStudentListField.setItems(allStudentList);
        selectedSemesterId=(int) semesterIdField.getSelectionModel().getSelectedItem();
        String query="SELECT DISTINCT R.studentId,S.studentName FROM registration as R INNER JOIN student AS S ON R.studentId=S.studentId WHERE semesterId='"+selectedSemesterId+"'";
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
        studentId=s.getStudentId();
        studentIdField.setText(studentId);
        studentNameField.setText(s.getStudentName());
        
        String query="SELECT G.courseCode,C.courseTitle,C.credits,G.facultyInitials,G.grade FROM grades AS G INNER JOIN course as C ON G.courseCode=C.courseCode WHERE G.studentId='"+s.getStudentId()+"' AND G.semesterId='"+selectedSemesterId+"'";
        try {
            resultset=statement.executeQuery(query);
            while(resultset.next())
            {
                int a1=0,a2=0,a3=0,b1=0,b2=0,b3=0,c=0,d=0,f=0;
                courseCode=resultset.getString("courseCode");
                String courseTitle=resultset.getString("courseTitle");
                String credits=resultset.getString("credits");
                facultyInitials=resultset.getString("facultyInitials");
                predictGrade();
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

    @FXML
    private void handleSearchId(ActionEvent event) {
        newstudentid=FXCollections.observableArrayList();
        String studentId=searchField.getText();
        for(Student s:allStudentList)
        {
            if(s.getStudentId().equals(studentId))
            {
                newstudentid.add(s);
            }
        }
        allStudentListField.setItems(newstudentid);
    }
    private String getAlphabaticGrades(double grade)
    {
                    if(grade>=4.00)
                    {
                        return "A+";
                    }
                    else if(grade>=3.75)
                    {
                        return "A";
                    }
                    else if(grade>=3.50)
                    {
                       return "A-";
                    }
                    else if(grade>=3.25)
                    {
                        return "B+";
                    }
                    else if(grade>=3.00)
                    {
                       return "B";
                    }
                    else if(grade>=2.75)
                    {
                        return "B-";
                    }
                    else if(grade>=2.5)
                    {
                        return "C+";
                    }
                    else if(grade>=2.25)
                    {
                        return "C";
                    }
                    else
                    {
                       return "D";
                    }
    }
    private String courseCode;
    private String predictGrade="0";
    private String facultyInitials="";
    String studentId="";
        private void predictGrade() throws SQLException
    {
         Statement stmnts=connection.createStatement();
         double gradeValue=0.00; 
         double totalCredits=0.00;
         String queryForTeacherInitial="SELECT C.credits ,G.grade FROM course as C INNER JOIN grades AS G ON C.courseCode=G.courseCode WHERE G.facultyInitials='"+facultyInitials+"' AND G.semesterId='"+selectedSemesterId+"' AND G.courseCode='"+courseCode+"'";
         ResultSet rst=stmnts.executeQuery(queryForTeacherInitial);
         
         while(rst.next())
         {
             double credits=rst.getDouble("credits");
             String grades=rst.getString("grade");
             totalCredits+=credits;
             switch (grades) {
                 case "A+":
                     gradeValue+=4.00*credits;
                     break;
                 case "A":
                     gradeValue+=3.75*credits;
                     break;
                 case "A-":
                     gradeValue+=3.50*credits;
                     break;
                 case "B+":
                     gradeValue+=3.25*credits;
                     break;
                 case "B":
                     gradeValue+=3.00*credits;
                     break;
                 case "B-":
                     gradeValue+=2.75*credits;
                     break;
                 case "C+":
                     gradeValue+=2.50*credits;
                     break;
                 case "C":
                     gradeValue+=2.25*credits;
                     break;
                 case "D":
                     gradeValue+=2.00*credits;
                     break;
                 default:
                     gradeValue+=0.0;
                     break;
             }
             
         }
         double averageResult=gradeValue/totalCredits;
         double gradesValue=0.00;
         double totalCredit=0.00;
         String queryForBacklog="SELECT C.credits ,G.grade FROM course as C INNER JOIN grades AS G ON C.courseCode=G.courseCode WHERE G.studentId='"+studentId+"'";
         Statement stment=connection.createStatement();
         ResultSet rest=stment.executeQuery(queryForBacklog);
         while(rest.next())
         {
             double credits=rest.getDouble("credits");
             String grades=rest.getString("grade");
             totalCredit+=credits;
             switch (grades) {
                 case "A+":
                     gradesValue+=4.00*credits;
                     break;
                 case "A":
                     gradesValue+=3.75*credits;
                     break;
                 case "A-":
                     gradesValue+=3.50*credits;
                     break;
                 case "B+":
                     gradesValue+=3.25*credits;
                     break;
                 case "B":
                     gradesValue+=3.00*credits;
                     break;
                 case "B-":
                     gradesValue+=2.75*credits;
                     break;
                 case "C+":
                     gradesValue+=2.50*credits;
                     break;
                 case "C":
                     gradesValue+=2.25*credits;
                     break;
                 case "D":
                     gradesValue+=2.00*credits;
                     break;
                 default:
                     gradesValue+=0.0;
                     break;
             }
             
         }
         double averageResultStudent=gradesValue/totalCredit;        
         predictGrade=getAlphabaticGrades((averageResult+averageResultStudent)/2);
    }

  
    
 

   

   
    
}
