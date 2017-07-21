package main;

import email.Email;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.Main;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    Parent root;

    @FXML
    private HBox hboxET;

    @FXML
    private TextField searchTextFieldDT;

    @FXML
    private PasswordField passwordBoxDT;

    @FXML
    private PasswordField passwordBoxET;

    @FXML
    private TextFlow outputTextFlowDT;

    @FXML
    private TableColumn<?, ?> tagsColumn;

    @FXML
    private TextArea secretsTextAreaET;

    @FXML
    private Tab helpTab;

    @FXML
    private VBox vboxHT;

    @FXML
    private Button encryptButtonET;

    @FXML
    private Button attachFileButtonET;

    @FXML
    private TextField attachFileTextFieldET;

    @FXML
    private Label label3ET;

    @FXML
    private Label label2ET;

    @FXML
    private Label label1ET;

    @FXML
    private TextField tagsTextFieldET;

    @FXML
    private Label selectEncryptLabelST;

    @FXML
    private ToggleGroup encryptModeGroupST;

    @FXML
    private HBox hboxDT;

    @FXML
    private VBox vBoxST;

    @FXML
    private Tab encryptTab;

    @FXML
    private Button decryptButtonDT;

    @FXML
    private Tab decryptTab;

    @FXML
    private Button nextMailButtonDT;

    @FXML
    private RadioButton simpleEncryptRadioST;

    @FXML
    private RadioButton robustEncryptRadioST;

    @FXML
    private Button saveButtonST;

    @FXML
    private TextFlow infoTextFlowET;

    @FXML
    private TextField kFieldST;

    @FXML
    private VBox vboxET;

    @FXML
    private Label infoLabelST;

    @FXML
    private Tab settingsTab;

    @FXML
    private VBox vBoxDT;

    @FXML
    private Button searchButtonDT;

    @FXML
    private TextFlow helpTextHT;

    @FXML
    private Label label2DT;

    @FXML
    private TextField fromEmailFieldST;

    @FXML
    private Label label1DT;

    @FXML
    private TableColumn<?, ?> dateColumn;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField emailListFieldST;

    @FXML
    private ListView<String> resultsListDT;


    int gotPasswords = 0;
    List<List<Email>> searchResults = new ArrayList<>();


    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, final ResourceBundle resources) {


    //Controls and initialisation for the encryption tab.
        encryptButtonET.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                String message = secretsTextAreaET.getText();
                String filePath = attachFileTextFieldET.getText();

                String tags = tagsTextFieldET.getText();
                System.out.println("Doing stuff!");
                System.out.println(message);
                System.out.println(tags);


                Main.userData.fromPassword = passwordBoxET.getText();
                try {
                    long startTime = System.currentTimeMillis();

                    Encrypt.encrypt(message, filePath, tags, Main.userData);

                    long endTime = System.currentTimeMillis();

                    System.out.println("That took " + (endTime - startTime)/1000.0 + " seconds");

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //TODO
                //Call some function to do the encryption and mailing.
                //Use catch and return values to correct for errors.
                //Will have to figure out preferences and stuff later.
            }
        });

        attachFileButtonET.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {

                //Stage is the main canvas on which everything is drawn, and filChooser needs it as argument.
                Stage myStage = (Stage) label1ET.getScene().getWindow();

                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(myStage);
                if(file != null) {
                    attachFileTextFieldET.setText(file.getAbsolutePath());
                }
            }
        });

        label3ET.setText("Enter password for ".concat(Main.userData.fromID).concat(" "));

        passwordBoxET.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                Main.userData.fromPassword = passwordBoxET.getText();
            }
        });


        //Controls and Initialisation for decrypt tab.


        label2DT.setText("Enter password for ".concat(Main.userData.emails.get(gotPasswords)).concat(" ").concat(Integer.toString(gotPasswords + 1)).concat(" of ").concat(Integer.toString(Main.userData.emails.size())));

        nextMailButtonDT.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                Main.userData.passwords.add(passwordBoxDT.getText());
                gotPasswords += 1;
                if(gotPasswords<Main.userData.emails.size()){
                    label2DT.setText("Enter password for ".concat(Main.userData.emails.get(gotPasswords)).concat(" ").concat(Integer.toString(gotPasswords + 1)).concat(" of ").concat(Integer.toString(Main.userData.emails.size())));
                    passwordBoxDT.clear();
                }
            }
        });




        searchButtonDT.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                String searchString = searchTextFieldDT.getText();
                searchString = (searchString);
                searchResults = Decrypt.search(searchString);
                ObservableList<String> toDisplay = FXCollections.observableArrayList();

                for(int i=0; i<searchResults.get(0).size(); i++){

                    Email e = searchResults.get(0).get(i);
                    //TODO Find a reversible transform to improve this representation.
                    toDisplay.add(Integer.toString(i).concat(". ").concat(e.subject));
                }
//                for (int i = 0; i < 3; i++) {
//                    toDisplay.add(searchString.concat(" result ").concat(Integer.toString(i)));
//                }
                resultsListDT.setItems(toDisplay);
            }
        });

        decryptButtonDT.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                //TODO
                String selected = resultsListDT.getSelectionModel().getSelectedItem();
                int i = Integer.parseInt(selected.substring(0, selected.indexOf('.')));
                String selectedSubject = searchResults.get(0).get(i).subject;
                System.out.println("Selected subject ".concat(selectedSubject));
                List<Email> toDecrypt = new ArrayList<Email>();
                for(List<Email> l : searchResults){
                    for (Email e : l){
                        if(e.subject.equals(selectedSubject)){
                            toDecrypt.add(e);
                            break;
                        }
                    }
                }

                String cleartext = Decrypt.decrypt(toDecrypt);
//                String cleartext = "output message looks like this.";
                Text output = new Text(cleartext);
                outputTextFlowDT.getChildren().clear();
                outputTextFlowDT.getChildren().addAll(output);
                resultsListDT.getItems().clear();
            }
        });


        //TODO : Implement settings and help. We'll see about that.
        saveButtonST.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                //Get List of email addresses.
                String emailList = emailListFieldST.getText();
                ArrayList<String> toEmails = new ArrayList(Arrays.asList(emailList.split(",")));

                String fromEmail = fromEmailFieldST.getText();

                int mode;
                if(simpleEncryptRadioST.isSelected()){
                    mode = 0;
                }
                else{
                    mode = 1;
                }

                int n = toEmails.size();
                int k = Integer.parseInt(kFieldST.getText());

                Main.userData.emails = toEmails;
                Main.userData.fromID = fromEmail;
                Main.userData.mode = mode;
                Main.userData.n = n;
                Main.userData.k = k;

            }
        });
    }
}

