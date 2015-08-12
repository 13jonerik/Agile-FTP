package com.company;

import java.util.Scanner;

/**
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since <pre>7/9/15</pre>
 */

/**
 * Shows menu options to user and handles response.
 */
public class CommandMenu {

    /**
     * Displays the SFTP Menu to the user.
     * @return The user's menu selection.
     */
    public static int showMainSFTPMenu() {
        return processMenu("SFTP Menu", new String[] {
                "Remote Management",
                "Local Files",
                "Options",
                "Disconnect from Server"
        });
    }


    /**
     * Shows a management menu to the user.
     * @param location  The type of menu to manage (remote or local).
     * @return The user's menu selection.
     */
    public static int showManageMenu(String location) {
        return processMenu(location + " Menu", new String[] {
                "File Management",
                "Directory Management",
                "SFTP Menu",
                "Disconnect from Server",

        });
    }


    /**
     * Shows a directory menu to the user.
     * @param location The type of menu to manage (remote or local).
     * @return The user's menu selection.
     */
    public static int showDirectoryMenu(String location) {
        return processMenu(location + " Directory Menu", new String[] {
                "List Current Directory",
                "List Files in Current Directory",
                "Change Directory",
                "Create Directory",
                "Delete Directory",
                "Rename Directory",
                location + " Menu",
                "Disconnect from Server",
        });
    }

    /**
     * Shows a local file menu to the user.
     * @return The user's menu selection.
     */
    public static int showLocalFileMenu() {
        return processMenu("Local File Menu", new String[] {
                "List Current Directory",
                "Change Current Directory",
                "List Local Files",
                "Rename Local File",
                "SFTP Menu",
                "Disconnect from Server"
        });
    }

    /**
     * Shows a remote file menu to the user.
     * @return The user's menu selection.
     */
    public static int showRemoteFileMenu() {
        return processMenu("Remote File Menu", new String[] {
                "Upload File to Remote Directory",
                "Download Files from Remote Directory",
                "Delete File from Remote Directory",
                "List Files in Current Directory",
                "Rename File",
                "Remote Menu",
                "Disconnect from Server" });
    }

    /**
     * Shows an options menu to the user.
     * @return The user's menu selection.
     */
    public static int showOptionsMenu() {
        return processMenu("Options Menu", new String[] {
                "Set Timeout Length",
                "Show Full File Details",
                "SFTP Menu",
                "Disconnect from Server",
        });
    }

    /**
     * Shows the remote file permissions menu to the user.
     * @return The user's menu selection.
     */
    public static int showRemotePermissionsMenu() {

        return processMenu("Remote Permissions Menu", new String[] {
                "Option",
                "Option",
                "More Option",
                "More Option",
                "Remote Menu",
                "Disconnect from Server"
        });
    }

    /**
     * Displays an initial user menu.
     * @return The user's menu selection.
     */
    public static int showMainMenu() {
        //TODO(): Possible add more selections (options, load user from file, etc).
        return processMenu("Main Menu", new String[] {
                "Connect to Server",
                "Quit"
        });
    }


    /**
     * Displays a menu to the user and processes her response.
     * @param title The title of the menu.
     * @param options The options available to the user.
     * @return The user's selection.
     */
    public static int processMenu(String title, String [] options) {
        System.out.println("\n" +title +":");
        for (int i = 0; i < options.length; ++i) {
                if (i + 1 == options.length) {
                    System.out.println("\n\t" + "0." + " " + options[i]);
                }
                else {
                    System.out.println("\t" + (i + 1) + "." + " " + options[i]); }
                }
        Scanner sc = new Scanner(System.in);
        String userString = sc.nextLine();
        int userInput;
        try {
            userInput = Integer.parseInt(userString);
        }
        catch (NumberFormatException e) {
            return -1;
        }
        return userInput;

    }
}
