package com.company;


import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import java.io.IOException;

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
                    boolean connected;
                    try {
                       connected = command.connect();
                    }
                    catch (IOException e) {
                        showMessage("Unable to Set Known Hosts File!");
                        continue;
                    }
                    catch (JSchException j ) {
                        if (j.getMessage().contains("UnknownHostException")) {
                            showMessage("\nCan't Reach Server!\n");
                        }
                        else if (j.getMessage().contains("socket")) {
                            showMessage("\nUnable to Establish Connection!\n");
                        }
                        else if (j.getMessage().contains("refused")) {
                            showMessage("\nServer Connection Refused!\n");
                        }
                        else if (j.getMessage().contains("Packet corrupt") ) {
                            showMessage("\nInvalid Server Credentials!\n");
                        }
                        else {
                            showMessage("Unable to Create Connection!");
                        }
                        continue;
                    }

                    if(!connected){
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
                    try {
                        command.setTimeout();
                    } catch (JSchException e) {
                        showMessage("Unable to set timeout");
                    }
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
                    try {
                        command.listCurrentRemoteDirectory();
                    } catch (SftpException e) {
                        showMessage("Unable to List Directory");
                    }
                } break;
                case 2: {
                    clearScreen();
                    try {
                        command.listCurrentRemoteFiles();
                    } catch (SftpException e) {
                        showMessage("Unable to List Remote Files");
                    }
                } break;
                case 3: {
                    try {
                        command.changeRemoteDirectory();
                    } catch (SftpException e) {
                        showMessage("Unable to Change Remote Directory");
                    }
                    clearScreen();
                    try {
                        command.listCurrentRemoteDirectory();
                    } catch (SftpException e) {
                        showMessage("Unable to List Remote Files");
                    }
                } break;
                case 4: {
                    clearScreen();
                    try {
                        command.createRemoteDir();
                    } catch (SftpException e) {
                        showMessage("Unable to Create Remote Directory");
                    }
                } break;
                case 5: {
                    clearScreen();
                    try {
                        command.listCurrentRemoteFiles();
                    } catch (SftpException e) {
                        showMessage("Unable to List Remote Files");
                    }
                    System.out.println();
                    try {
                        command.deleteRemoteDirectory();
                    } catch (SftpException e) {
                        showMessage("Unable to Delete Remote Directory");
                    }
                } break;
                case 6: {
                    clearScreen();
                    try {
                        command.listCurrentRemoteFiles();
                    } catch (SftpException e) {
                        showMessage("Unable to List Remote Files");
                    }
                    System.out.println();
                    try {
                        command.renameRemoteDirectory();
                    } catch (SftpException e) {
                        showMessage("Unable to Rename Remote Directory");
                    }
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
                    try {
                        command.uploadRemoteFile();
                    } catch (SftpException e) {
                        showMessage("Unable to Upload File");
                    }
                } break;
                case 2: {
                    clearScreen();
                    try {
                        command.listCurrentRemoteFiles();
                    } catch (SftpException e) {
                        showMessage("Unable to List Remote Files");
                    }
                    System.out.println();
                    try {
                        command.getMultipleRemote();
                    } catch (SftpException e) {
                        showMessage("Unable to Download Files");
                    }
                } break;
                case 3: {
                    try {
                        command.deleteRemoteFile();
                    } catch (SftpException e) {
                        showMessage("Unable to Delete Remote Files");
                    }
                    clearScreen();
                } break;
                case 4: {
                    clearScreen();
                    try {
                        command.listCurrentRemoteFiles();
                    } catch (SftpException e) {
                        showMessage("Unable to List Remote Files");
                    }
                } break;
                case 5: {
                    try {
                        command.renameRemoteFile();
                    } catch (SftpException e) {
                        showMessage("Unable to Rename Remote File");
                    }
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
                    try {
                        command.changeCurrentLocalDirectory();
                    } catch (SftpException e) {
                        showMessage("Unable to Change Directory");
                    }
                    clearScreen();
                    command.listCurrentLocalDirectory();
                } break;
                case 3: {
                    clearScreen();
                    try {
                        command.listCurrentLocalDirectoryFiles();
                    } catch (IOException e) {
                        showMessage("Unable to List Directory");
                    }
                } break;
                case 4: {
                    clearScreen();
                    command.renameLocalFile();
                    try {
                        command.listCurrentLocalDirectoryFiles();
                    } catch (IOException e) {
                        showMessage("Unable to List Local Files");
                    }
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

