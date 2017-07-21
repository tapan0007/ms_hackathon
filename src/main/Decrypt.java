package main;

import crypto.Shamir;
import crypto.XOR;
import email.Email;
import email.ReadMail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hooda on 2/4/2015.
 */
public class Decrypt {

    public static List<List<Email>> search(String searchTerm) {
        List<List<Email>> searchResults = new ArrayList<>();
        for (int i = 0; i < Main.userData.emails.size(); i++) {
            try {
                List<Email> email = ReadMail.read(Email.getHost(2, Main.userData.emails.get(i)), Main.userData.emails.get(i), Main.userData.passwords.get(i), searchTerm);
//                List<Email> email = ReadMail.read(Email.getHost(2, Main.userData.emails.get(i)), Main.userData.emails.get(i), Main.userData.passwords.get(i), "Please Find This");
                searchResults.add(email);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return searchResults;
    }


    public static String decrypt(List<Email> toDecrypt) {
        String cleartext = "";

        System.out.println(toDecrypt.get(0).body);

        if (toDecrypt.get(0).body.substring(0, 5).equals("Share")) {
            //ShamirDecrypt
            ArrayList<String> parts = new ArrayList<>();
            for (Email e : toDecrypt) {
                parts.add(e.body);
            }
            String decrypted = Shamir.shamirCombine(parts, null, Main.userData.k);
            cleartext = decrypted;
        } else {
            String[] parts = new String[toDecrypt.size()];
            for (int i = 0; i < parts.length; i++) {
                parts[i] = toDecrypt.get(i).body;
            }
            XOR xor = new XOR();
            String decrypted = xor.decrypt(parts);
            cleartext = decrypted;
        }
        return cleartext;
    }
}
