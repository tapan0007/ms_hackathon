package email;

/**
 * Created by Ayush on 1/1/2015.
 */

import sun.misc.IOUtils;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.io.*;
import java.util.*;


public class ReadMail {

    public static List<Email> read(String host, String user, String pass, String tag) throws Exception {
        System.out.print("Searching for ");
        System.out.print(tag);
        System.out.print("in the inbox of ");
        System.out.println(user);

        List<Email> emails = new ArrayList<Email>();

        // Create empty properties
        Properties props = new Properties();

        // Get the session
        Session session = Session.getInstance(props, null);

        // Get the store
        Store store = session.getStore("imaps");
        store.connect(host, user, pass);
        System.out.println("Connected");
        // Get folder
        Folder folder = store.getFolder("Inbox");
        folder.open(Folder.READ_WRITE);
        System.out.println("Inbox Opened");
        try {
            // Get directory listing
            //Address from = new Add
            SearchTerm sTerm = new SubjectTerm(tag);
//            SearchTerm fTerm = new FromStringTerm(user);
//            SearchTerm aTerm = new AndTerm(sTerm, fTerm);
            Message messages[] = folder.search(sTerm);
            System.out.println(messages.length);
            for (int i = 0; i < messages.length; i++) {

                Email email = new Email();

                email.subject = messages[i].getSubject();
                System.out.println(email.subject);
                if (email.subject == null) {
                    email.subject = "no subject";
                }
                //Pattern pattern = Pattern.compile("Sample");
                //Matcher matcher = pattern.matcher(email.subject.trim());
                //if (matcher.find()){

                // received date
                if (messages[i].getReceivedDate() != null) {
                    email.received = messages[i].getReceivedDate();
                } else {
                    email.received = new Date();
                }

                // body and attachments
                email.body = "";
                Object content = messages[i].getContent();

                System.out.println(content.toString());


//                String theString = "";
//                java.util.Scanner s = new java.util.Scanner((InputStream) content).useDelimiter("\\A");
//                theString = s.hasNext() ? s.next() : "";
//                System.out.println(theString);
//                System.out.println(theString.indexOf("Share"));
//                if(true){
//                    String theString = "";//new IOUtils().toString((InputStream) content, "UTF-8");
//
//                    java.util.Scanner s = new java.util.Scanner((InputStream) content).useDelimiter("\\A");
//                    theString = s.hasNext() ? s.next() : "";
//                    ArrayList<String> pieces = new ArrayList(Arrays.asList(theString.split("\n")));
//                    for (int j = 0; i < pieces.size(); j++) {
//                            System.out.println(pieces.get(j).concat(" at pos").concat(Integer.toString(j)));
//                        }
//                    }



                if (content instanceof java.lang.String) {
                    System.out.println("message.content was string-".concat(content.toString()));

                    email.body = (String) content;

                } else if (content instanceof Multipart) {

                    System.out.println("message.content was multipart");

                    Multipart mp = (Multipart) content;

                    for (int j = 0; j < mp.getCount(); j++) {

                        Part part = mp.getBodyPart(j);
                        String disposition = part.getDisposition();

                        if (disposition == null) {

                            MimeBodyPart mbp = (MimeBodyPart) part;
                            if (mbp.isMimeType("text/plain")) {
                                // Plain
                                email.body += (String) mbp.getContent();
                            }

                        } else if ((disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE))) {

                            // Check if plain
                            MimeBodyPart mbp = (MimeBodyPart) part;
                            if (mbp.isMimeType("text/plain")) {
                                email.body += (String) mbp.getContent();
                            } else {
                                EmailAttachment attachment = new EmailAttachment();
                                attachment.name = decodeName(part.getFileName());
                                email.attachments.add(attachment);
                            }
                        }
                    } // end of multipart for loop
                } // end messages for loop
                else {
                    String theString = "";
                    java.util.Scanner s = new java.util.Scanner((InputStream) content).useDelimiter("\\A");
                    theString = s.hasNext() ? s.next() : "";
                    theString = theString.substring(theString.indexOf("Share"));
                    theString = theString.substring(0, theString.indexOf("---"));
                    email.body = theString;
                    }

                emails.add(email);

            }

            // Close connection
            folder.close(false); // true tells the mail server to expunge deleted messages
            store.close();

        } catch (Exception e) {
            folder.close(false); // true tells the mail server to expunge deleted
            store.close();
            throw e;
        }

        return emails;
    }

    private static String decodeName(String name) throws Exception {
        if (name == null || name.length() == 0) {
            return "unknown";
        }
        String ret = java.net.URLDecoder.decode(name, "UTF-8");

        // also check for a few other things in the string:
        ret = ret.replaceAll("=\\?utf-8\\?q\\?", "");
        ret = ret.replaceAll("\\?=", "");
        ret = ret.replaceAll("=20", " ");

        return ret;
    }

    private static int saveFile(File saveFile, Part part) throws Exception {

        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(saveFile));

        byte[] buff = new byte[2048];
        InputStream is = part.getInputStream();
        int ret = 0, count = 0;
        while ((ret = is.read(buff)) > 0) {
            bos.write(buff, 0, ret);
            count += ret;
        }
        bos.close();
        is.close();
        return count;
    }
}

