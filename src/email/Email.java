package email;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ayush on 1/1/2015.
 */
public class Email {
    public Date received;
    public String subject;
    public String body;
    public List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();

    public static String getHost(int f, String user) {
        String host = user.split("@")[1];
        if (host.startsWith("gmail")) {
            if (f == 1) {
                return "smtp.gmail.com";
            } else {
                return "imap.gmail.com";
            }
        } else if (host.startsWith("yahoo")) {
            if (f == 1) {
                return "smtp.mail.yahoo.com";
            } else {
                return "imap.mail.yahoo.com";
            }
        } else {
            return "";
        }
    }
}
