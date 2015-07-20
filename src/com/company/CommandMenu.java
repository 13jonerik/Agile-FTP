package com.company;

import java.util.Scanner;

/**
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since <pre>7/9/15</pre>
 */
public class CommandMenu {

    /**
     * Displays the SFTP Menu to the user.
     */
    public static int showMainSFTPMenu() {
        return processMenu("SFTP Menu", new String[] {
                "Remote File Management",
                        "Local File Management",
                        "Options",
                        "Disconnect from Server"
        });
    }

    public static int showManageMenu(String location) {
        return processMenu(location + " Menu", new String[] {
                "File Management",
                        "Directory Management",
                        "Permission Management",
                        "SFTP Menu",
                        "Disconnect from Server",

        });
    }


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

    public static int showOptionsMenu() {
        return processMenu("Options Menu", new String[] {
                "Set Timeout Length",
                "Show Full File Details",
                "More Option",
                "More Option",
                "SFTP Menu",
                "Disconnect from Server",
        });
    }

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
