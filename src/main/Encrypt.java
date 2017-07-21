package main;

import crypto.Shamir;
import crypto.XOR;
import email.Email;
import email.SendMail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by hooda on 2/4/2015.
 */

/**
 * Error Codes to be used.
 * 0 - Okie Dokie
 * -1 -> Network errors.
 * -2 -> Wrong passwords.
 * -3 -> Need more emails.
 */

public class Encrypt {

    public static final String SECRETSAFE = "SecretSafe";

    public static int encrypt(String message, String filePath, String tags, UserData userData) throws IOException {

        UUID uuid = UUID.randomUUID();
        String subject = SECRETSAFE.concat(" ").concat(String.valueOf(uuid)).concat(" ").concat(tags);

        ArrayList<String> splitNames = null;
	/*
        if(filePath != null && filePath!=""){
        splitNames = Shamir.fileSplit(filePath, userData.n, userData.k);
        }
	*/
        //Simple XOR encryption.
        if (userData.mode == 0) {
            XOR xor = new XOR();
            String[] pieces = new String[0];
            try {
                pieces = xor.encrypt(message, userData.n);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<String> mails = new ArrayList<>(Arrays.asList(pieces));
            return sendMails(mails, subject, splitNames, userData);
        }

        //Shamir encryption.
        else if (userData.mode == 1) {
            ArrayList<String> mails = Shamir.shamirSplit(message, userData.n, userData.k);
            return sendMails(mails, subject, splitNames, userData);
        }

        //TODO Implement proper error codes
        else return -1;


    }

    private static int shamirCrypt(String message, UserData userData) {
        //TODO

        return 0;
    }

    private static int sendMails(ArrayList<String> mails, String subject, ArrayList<String> attachments, UserData userData) {


        for (int i = 0; i < mails.size(); i++) {
            try {
                SendMail sendMail = new SendMail(userData.fromID, userData.fromPassword, Email.getHost(1, userData.fromID));
                System.out.println("----------------------------------------------------------------------------------");
//                System.out.println(userData.fromID);
//                System.out.println(userData.fromPassword);
                System.out.println(subject);
                System.out.println(mails.get(i));
                if(attachments!=null) {
                    sendMail.addAttachment(attachments.get(i));
                }
                sendMail.send(userData.fromID, userData.fromPassword, userData.emails.get(i), userData.fromID, subject, mails.get(i));
                System.out.println("sentmail to ".concat(userData.emails.get(i)));
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return 0;
    }
}
