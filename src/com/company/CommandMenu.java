package com.company;

/**
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since <pre>7/9/15</pre>
 */
public class CommandMenu {

    /**
     * Displays the SFTP Menu to the user.
     */
    public static void showMainSFTPMenu() {
        System.out.println("\nSFTP Menu:\n" +
                "\t1.Remote File Management\n" +
                "\t2.Local File Management\n" +
                "\t3.Options\n" +
                "\t4.Quit");
    }

    public static void showManageMenu(String location) {
        System.out.println("\n" + location + " Menu:\n" +
                "\t1.File Management\n" +
                "\t2.Directory Management\n" +
                "\t3.Permission Management\n" +
                "\t4.SFTP Menu");
    }


    public static void showRemoteDirectoryMenu(String location) {
        System.out.println("\n" + location + " Directory Menu:\n" +
                "\t1.List Current Directory\n" +
                "\t2.List Files in Current Directory\n" +
                "\t3.Change Directory\n" +
                "\t4.Create Directory\n" +
                "\t5.Delete Directory\n" +
                "\t6.Rename Directory\n" +
                "\t7." + location + " Menu");
    }

    public static void showLocalFileMenu() {
        System.out.println("\nLocal File Menu:\n" +
                "\t1.Rename File\n" +
                "\t2.Option\n" +
                "\t3.Option\n" +
                "\t4.Local Menu");
    }
    public static void showRemoteFileMenu() {
        System.out.println("\nRemote File Menu:\n" +
                "\t1.Upload File to Remote Directory\n" +
                "\t2.Download File from Remote Directory\n" +
                "\t3.Delete File from Remote Directory\n" +
                "\t4.Remote Menu");
    }

    public static void showOptionsMenu() {
        System.out.println("\nOptions Menu:\n" +
                "\t1.Set Timeout Length\n" +
                "\t2.Show Full File Details\n" +
                "\t3.Option\n" +
                "\t4.Option\n" +
                "\t5.Option\n" +
                "\t6.SFTP Menu");
    }

    public static void showRemotePermisionsMenu() {

    }
}
