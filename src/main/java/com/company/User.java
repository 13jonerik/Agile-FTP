package com.company;

import com.jcraft.jsch.UserInfo;


import java.io.Console;
import java.util.Scanner;

/** Stores user information that is needed to connect to a server
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since July 5, 2015
 */

public class User implements UserInfo {
    protected String userName;
    protected String password;
    protected String passPhrase = "";

    protected static Scanner sc = new Scanner(System.in);

    /**
     * Default constructor.
     */
    public User () {
        this.userName = "";
        this.password = "";
    }

    /**
     * Constructor sets user name
     * @param name the users login name
     * @param password the user's password
     */
    public User(String name, String password) {
        this.password = password;
        this.userName = name;

    }

    /**
     * Gets the user name
     * @return <code>userName</code>
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Prompts user to enter her user name.
     */
    public void promptUserName() {
        showMessage("User Name:");
        this.userName = sc.nextLine();

    }

    /**
     * Determines if the user information is complete
     * @return if both username and password are valid.  Does not check passphrase
     */

    public boolean validUser() {
        return ((!this.userName.equals("")) && (!this.password.equals("")));
    }

    /**
     * Returns passphrase.  Not used at this point
     * @return <code>passPhrase</code>
     */
    @Override
    public String getPassphrase() {
        return passPhrase;
    }

    /**
     * Returns user password.
     * @return <code>password</code>
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Prompts the user to enter her login password.
     * @param s message string required by UserInfo.  Not used.
     * @return True is valid password, false otherwise.
     */

    public boolean promptPassword(String s) {
        return promptPassword();
    }

    /**
     * Prompts the user to enter her login password.
     * @return True if non-null password, false otherwise.
     */
    public boolean promptPassword() {
        showMessage("Password:");

        //Modified from https://docs.oracle.com/javase/7/docs/api/java/io/Console.html#readPassword%28%29
        Console cons;
        char[] passwd;
        if ((cons = System.console()) != null &&
                (passwd = cons.readPassword()) != null) {
            this.password = new String(passwd);
            java.util.Arrays.fill(passwd, ' ');
        }
        else {
            this.password = sc.nextLine();
        }

        return this.password != null;
    }


    /**
     * Prompts user for passphrase. Not used at this time.
     */
    @Override
    public boolean promptPassphrase(String s) {
        return false;
    }

    /**
     * Prompts user for Yes/No question. Not used at this time.
     * @param s Message to prompt user.
     * @return User's response.
     */
    @Override
    public boolean promptYesNo(String s) {
        showMessage(s + " (Yes/No)? : ");
        return sc.nextLine().equals("yes");
    }

    /**
     * Displays a message to the user.
     * @param s message to display.
     */
    @Override
    public void showMessage(String s) {
        System.out.print(s + " ");
    }

    public void clearPass() {
        this.password = "";
    }
}
