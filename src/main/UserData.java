package main;

import email.SendMail;

import java.util.ArrayList;

/**
 * Created by hooda on 2/4/2015.
 */
public class UserData {
    Boolean initialised = false;
    public int n;
    public int k;
    public int mode;
    public ArrayList<String> emails = new ArrayList<String>();
    public ArrayList<String> passwords;
    public String fromID;
    public String fromPassword;
}
