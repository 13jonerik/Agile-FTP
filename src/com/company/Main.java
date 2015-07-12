package com.company;

import java.util.Scanner;

/**
 * This Class handles the main menu that users see when then first start the program.
 * @author  Stephan Gelever
 * @version 1.0
 * @since July 5, 2015
 */

public class Main {
    private static Scanner sc = new Scanner(System.in);

    /**
     * Processes the initial user menu and user input.
     * Offers the user to connect to the server or quit the program.
     * @param args  list of command line arguments
     */
    public static void main(String[] args) {
        User user = new User();
        CommandSFTP command = new CommandSFTP();

        boolean quit = false;
        while (!quit) {
            showMainMenu();
            String input = sc.nextLine();

            //Process user input at main menu
            switch (input) {
                case "1": {

                    //Gather information from user
                    command.promptInfo();
                    user.promptUserName();
                    user.promptPassword();
                    command.setUser(user);

                    //Validate user input
                    if (!(user.validUser() && command.isValid())) {
                        System.out.println("\nRequires valid User and Server Info!");
                        break;
                    }


                    //If the connection is valid, proceed to SFTP application
                    if(!command.connect()){
                        //System.out.println("Error Connecting!");
                        continue;
                    }
                    else {
                        command.mainSFTPMenu();
                    }

                } break;
                case "2": {
                    quit = true;
                } break;
                default:
                    System.out.println("\nInvalid Selection!\n");
                    break;
            }
        }
        System.exit(0);
    }

    /**
     * Displays an initial user menu.
     */
    private static void showMainMenu() {
        //TODO(): Possible add more selections (options, load user from file, etc).
        System.out.println("\nMain Menu:\n" +
                "\t1.Connect to Server\n" +
                "\t2.Quit");
    }
}

