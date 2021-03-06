package sample;

import com.opencsv.CSVWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller  {
    private File fileToSave;

    @FXML
    private ListView listview;

    @FXML
    private TextField textfield;

    @FXML
    public Button uploadButton;

    @FXML
    public ComboBox comboBox;

    @FXML
    public Label uploadLoc;

    public String showSaveFileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Spreadsheet (*.csv)", "csv");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(fileChooser);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
            return fileToSave.toString() + ".csv";
        }
        else
            return null;
    }

    @FXML
    public void uploadItems(ActionEvent e){
        Uploader uploader = new Uploader();
    }

    @FXML
    public void importNewPrices(ActionEvent event) throws FileNotFoundException  {
        String connectionUrl = "jdbc:sqlserver://DESIGNER1:1433;databaseName=INTRANET;user=intranet;password=bathcountry110";

        String SQL = "";

        BufferedReader bufferedReader = new BufferedReader(
                new FileReader("C:\\Users\\hubert\\IdeaProjects\\eBaySystem\\src\\sample\\Price Pull.sql")
        );

        String thisLine, sqlQuery;
        try {
            sqlQuery = "";
            while ((thisLine = bufferedReader.readLine()) != null)
            {
                //Skip comments and empty lines
                if(thisLine.length() > 0 && thisLine.charAt(0) == '-' || thisLine.length() == 0 )
                    continue;
                sqlQuery = sqlQuery + " " + thisLine;
                //If one command complete
                if(sqlQuery.charAt(sqlQuery.length() - 1) == ';') {
                    sqlQuery = sqlQuery.replace(';' , ' '); //Remove the ; since jdbc complains
                }
            }
            SQL = sqlQuery;
        }
        catch(IOException ex) {

        }

        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(SQL);

            Date todayWithZeroTime = new java.sql.Date(System.currentTimeMillis());
            // Iterate through the data in the result set and display it.
            while (rs.next()) {

                CSVWriter csvWriter = new CSVWriter(new FileWriter(showSaveFileDialog()));
                csvWriter.writeAll(rs, true);
                System.out.println("Finished");
            }
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @FXML
    public void pullItemsFromDB(ActionEvent e){
        ArrayList<String> productList = new ArrayList<>(Arrays.asList(textfield.getText().split("\\s*,\\s*")));
        String appendedProducts = "";
        for(String product: productList){
            if(productList.indexOf(product) == productList.size() - 1){
                appendedProducts = appendedProducts + "\'" + product + "\'";
            } else {
                appendedProducts = appendedProducts + "\'" + product + "\',";
            }
        }
        String connectionUrl = "jdbc:sqlserver://DESIGNER1:1433;databaseName=INTRANET;user=intranet;password=bathcountry110";

        String SQL = "";

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(
                    new FileReader("C:\\Users\\hubert\\IdeaProjects\\eBaySystem\\src\\sample\\Pull Products In Template.sql")
            );
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        String thisLine, sqlQuery;
        try {
            sqlQuery = "";
            while ((thisLine = bufferedReader.readLine()) != null)
            {
                //Skip comments and empty lines
                if(thisLine.length() > 0 && thisLine.charAt(0) == '-' || thisLine.length() == 0 )
                    continue;
                sqlQuery = sqlQuery + " " + thisLine;
                //If one command complete
                if(sqlQuery.charAt(sqlQuery.length() - 1) == ';') {
                    //Remove the ; since jdbc complains
                    sqlQuery = sqlQuery.replace(';' , ' ');
                }
            }
            SQL = sqlQuery;
            SQL = SQL.replace("MY NAME JEFF", appendedProducts);
        }
        catch(IOException ex) {

        }

        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(SQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                System.out.println(rs.getNString("prodcode"));
                listview.getItems().add(rs.getNString("prodcode"));
            }
            CSVWriter csvWriter = new CSVWriter(new FileWriter(showSaveFileDialog()));
            csvWriter.writeAll(rs, true);

        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
