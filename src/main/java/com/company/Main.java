package com.company;


/**
 * This Class handles the main menu that users see when then first start the program.
 * @author  Stephan Gelever
 * @version 1.0
 * @since July 5, 2015
 */

public class Main {
    /**
     * Processes the initial user menu and user input.
     * Offers the user to connect to the server or quit the program.
     * @param args  list of command line arguments
     */
    public static void main(String[] args) {
        clearScreen();
        User user = new User();
        CommandSFTP command = new CommandSFTP();

        boolean quit = false;
        while (!quit) {
            int userInput = CommandMenu.showMainMenu();

            //Process user input at main menu
            switch (userInput) {
                case 1: {

                    //Gather information from user
                    command.promptInfo();
                    user.promptUserName();
                    user.promptPassword();
                    command.setUser(user);

                    //Validate user input
                    if (!(user.validUser())) {
                        System.out.println("\nRequires Valid User Info!");
                        break;
                    }


                    //If the connection is valid, proceed to SFTP application
                    if(!command.connect()){
                        continue;
                    }
                    else {
                        clearScreen();
                        mainSFTPMenu(command);
                    }

                } break;
                case 0: {
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
     * Handles SFTP Menu user input.
     * @param command Where to send the user input.
     */
    public static void mainSFTPMenu(CommandSFTP command) {
        while(command.checkConnect()) {
            int userInput = CommandMenu.showMainSFTPMenu();
            clearScreen();
            switch(userInput) {
                case 1: {
                    remoteSFTPMenu(command);
                } break;
                case 2: {
                    localSFTPMenu(command);
                } break;
                case 3: {
                    optionsSFTPMenu(command);
                } break;
                case 0: {
                    clearScreen();
                    command.quit();
                    return;
                }
                default:
                    clearScreen();
                    showMessage("Invalid Command!\n");
            }
        }

    }


    /**
     * Handles options menu system.
     * @param command Where to send the user input.
     */
    private static void optionsSFTPMenu(CommandSFTP command) {
        while(command.checkConnect()) {
            int userInput = CommandMenu.showOptionsMenu();
            switch(userInput) {
                case 1: {
                    command.setTimeout();
                    clearScreen();
                } break;
                case 2: {
                    command.setFileDisplay();
                    clearScreen();
                } break;
                case 3: {
                    clearScreen();
                    return;
                }
                case 0: {
                    clearScreen();
                    command.quit();
                    return;
                }
                default:
                    clearScreen();
                    showMessage("\nInvalid Command!\n");
            }
        }

    }


    /**
     * Handles remote directory menu system for remote management
     * @param command Where to send the user input.
     */
    private static void remoteSFTPDirMenu(CommandSFTP command) {
        while(command.checkConnect()) {
            int userInput = CommandMenu.showDirectoryMenu("Remote");
            switch(userInput) {
                case 1: {
                    clearScreen();
                    command.listCurrentRemoteDirectory();
                } break;
                case 2: {
                    clearScreen();
                    command.listCurrentRemoteFiles();
                } break;
                case 3: {
                    command.changeRemoteDirectory();
                    clearScreen();
                    command.listCurrentRemoteDirectory();
                } break;
                case 4: {
                    clearScreen();
                    command.createRemoteDir();
                } break;
                case 5: {
                    clearScreen();
                    command.listCurrentRemoteFiles();
                    System.out.println();
                    command.deleteRemoteDirectory();
                } break;
                case 6: {
                    clearScreen();
                    command.listCurrentRemoteFiles();
                    System.out.println();
                    command.renameRemoteDirectory();
                } break;
                case 7: {
                    clearScreen();
                    return;
                }
                case 0: {
                    clearScreen();
                    command.quit();
                    return;
                }
                default:
                    clearScreen();
                    showMessage("\nInvalid Command!\n");
            }
        }

    }


    /**
     * Handles remote file menu system for remote management
     * @param command Where to send the user input.
     */
    private static void remoteSFTPFileMenu(CommandSFTP command) {
        while(command.checkConnect()) {
            int userInput = CommandMenu.showRemoteFileMenu();
            switch(userInput) {
                case 1: {
                    clearScreen();
                    command.uploadRemoteFile();
                } break;
                case 2: {
                    clearScreen();
                    command.listCurrentRemoteFiles();
                    System.out.println();
                    command.getMultipleRemote();
                } break;
                case 3: {
                    command.deleteRemoteFile();
                    clearScreen();
                } break;
                case 4: {
                    clearScreen();
                    command.listCurrentRemoteFiles();
                } break;
                case 5: {
                    command.renameRemoteFile();
                    clearScreen();
                } break;
                case 6: {
                    clearScreen();
                    return;
                }
                case 0: {
                    clearScreen();
                    command.quit();
                    return;
                }
                default:
                    clearScreen();
                    showMessage("\nInvalid Command!\n");
            }
        }
    }


    /**
     * Handles remote file and directory menu system for remote management
     * @param command Where to send the user input.
     */
    private static void remoteSFTPMenu(CommandSFTP command) {
        //TODO(gelever): Finish up these calls.
        while(command.checkConnect()) {
            int userInput = CommandMenu.showManageMenu("Remote");
            clearScreen();
            switch(userInput) {
                case 1: {
                    remoteSFTPFileMenu(command);
                } break;
                case 2: {
                    remoteSFTPDirMenu(command);
                } break;
                case 3: {
                    clearScreen();
                    return;
                }
                case 0: {
                    clearScreen();
                    command.quit();
                    return;
                }
                default:
                    clearScreen();
                    showMessage("\nInvalid Command!\n");
            }
        }

    }


    /**
     * Handles remote file permissions menu system for remote management
     * @param command Where to send the user input.
     */
    private void remoteSFTPPermissionMenu(CommandSFTP command) {
        //TODO(gelever): Finish up these calls.
        while(command.checkConnect()) {
            int userInput = CommandMenu.showRemotePermissionsMenu();
            //NOTE: Fix this positioning too.
            clearScreen();

            switch(userInput) {
                case 1: {

                } break;
                case 2: {

                } break;
                case 3: {
                    return;
                }
                case 0: {
                    command.quit();
                    return;
                }
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }

    }


    /**
     * Handles local file menu system for local file management.
     * @param command Where to send the user input.
     */
    private static void localSFTPMenu(CommandSFTP command) {
        while(command.checkConnect()) {
            int userInput = CommandMenu.showLocalFileMenu();
            switch(userInput) {
                case 1: {
                    clearScreen();
                    command.listCurrentLocalDirectory();
                } break;
                case 2: {
                    command.changeCurrentLocalDirectory();
                    clearScreen();
                    command.listCurrentLocalDirectory();
                } break;
                case 3: {
                    clearScreen();
                    command.listCurrentLocalDirectoryFiles();
                } break;
                case 4: {
                    clearScreen();
                    command.renameLocalFile();
                    command.listCurrentLocalDirectoryFiles();
                    System.out.println();
                } break;
                case 5: {
                    clearScreen();
                    return;
                }
                case 0: {
                    clearScreen();
                    command.quit();
                    return;
                }
                default:
                    clearScreen();
                    showMessage("\nInvalid Command!\n");
            }
        }

    }


    /**
     * Clears the screen.
     */
    public static void clearScreen() {
        System.out.println("\033[2J");
        System.out.println("\033[1;1H");
    }


    /**
     * Displays a message to the user.
     * @param s Message to display.
     */
    public static void showMessage(String s) {
        System.out.print(s);
    }
}

