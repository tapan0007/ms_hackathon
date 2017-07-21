package main;

import email.SendMail;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    public static UserData userData;

    @Override
    public void start(Stage primaryStage) throws Exception {
//        System.out.println("Called!");

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("SecretSafe");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }


    public static void main(String[] args) throws Exception {


//        SendMail sendMail = new SendMail();
//        sendMail.send("ayush.inferno@gmail.com", "qwerty..", "hoodakaushal@gmail.com", "ayush.inferno@gmail.com", "Yes", "No");


        readUserData();
        launch(args);
    }

    //Format : line 1 : n. line 2 : k. line 3 : mode of encryption. line 4 : from email id. Then list of email ids, one per line.
    public static void readUserData() throws IOException {
        UserData tempUserData = new UserData();
        List<String> data = Files.readAllLines(Paths.get("userdata"));

        tempUserData.n = Integer.parseInt(data.get(0));
        tempUserData.k = Integer.parseInt(data.get(1));
        tempUserData.mode = Integer.parseInt(data.get(2));
        tempUserData.fromID = data.get(3);

        for(int i = 4; i<data.size(); i++){
            tempUserData.emails.add(data.get(i));
        }
        ArrayList<String> pwords = new ArrayList<String>(tempUserData.emails.size());
        pwords.add(0, "qwerty..");
        pwords.add(1, "qwertyuiop123456789");
        tempUserData.passwords = pwords;
        tempUserData.initialised = true;

        Main.userData = tempUserData;
    }

    public static void saveUserData() throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter("userdata"));
        bw.write(Integer.toString(Main.userData.n));
        bw.newLine();
        bw.write(Integer.toString(Main.userData.k));
        bw.newLine();
        bw.write(Integer.toString(Main.userData.mode));
        bw.newLine();
        bw.write(Main.userData.fromID);
        bw.newLine();

        for (int i = 0; i < Main.userData.emails.size(); i++) {
            bw.write(Main.userData.emails.get(i));
            bw.newLine();
        }

        bw.flush();
    }
}
