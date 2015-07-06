package com.company;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by Stephan Gelever on 7/5/15.
 * Handles the logging in and the commands sent to the server.
 * @author Stephan Gelever
 * @version 1.0 alpha
 */
public class CommandSFTP {
    private String hostIP = null;
    private String knownHostsFile = null;
    private int portNumber;
    private final String [] hostChecking = {"StrictHostKeyChecking", "yes"};
    private boolean isConnected = false;

    private JSch jsch = null;
    private Session session = null;
    private ChannelSftp channel = null;

    private boolean fileDisplay = false;

    private static Scanner sc = new Scanner(System.in);

    /**
     * Default constructor.
     */
    public CommandSFTP() {
        this.jsch = new JSch();
        this.jsch.setConfig(hostChecking[0], hostChecking[1]);
        this.portNumber = 22;
    }

    /**
     * Returns whether or not the user is currently connected.
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * Prompts user to enter server information.
     */

    public void promptInfo() {
        showMessage("Host IP:");
        this.hostIP = sc.nextLine();

        showMessage("Port Number:");
        boolean validPort = false;

        while (!validPort) {
            if (sc.hasNextInt()) {
                this.portNumber = sc.nextInt();
                validPort = true;
            }
        }

        sc.nextLine();
        showMessage("KnownHosts File Location\n" +
                "Will be created if it doesn't exist\n" +
                "Leave blank to create ~/.ssh/sftp_hosts:");
        this.knownHostsFile = sc.nextLine();


    }

    /**
     * Connects the user to the specifed server. Assumes server information has already been provided.
     * @param user User information
     * @return true if successful connection, false otherwise.
     */
    public boolean connect(User user) {
        //TODO(): Check is server information has already been provided.
        setKnownHostsFile(this.knownHostsFile);

        try {
            this.session = this.jsch.getSession(user.getUserName(), this.hostIP, this.portNumber);

        }
        catch (JSchException je) {
            //TODO: Handle exception properly
            System.err.println(je);
            return false;
        }
        this.session.setPassword(user.getPassword());

        if (!(connectSession(user) && channelConnect())) {
            return false;
        }
        this.isConnected = true;

        return true;

    }

    /**
     * Connects the channel to the current session.
     * @return true on success, false otherwise.
     */
    private boolean channelConnect() {

        try {
            this.channel = (ChannelSftp)this.session.openChannel("sftp");
            this.channel.connect();
        }
        catch (JSchException e) {
            System.err.println(e);
            return false;
        }
        return true;
    }

    /**
     * Attempts to correct the current session.
     * @param user User information
     * @return true on success, false otherwise.
     */
    private boolean connectSession(User user) {
        try {
            this.session.connect();
        }
        catch (JSchException je) {
            //Connection error
            if (je.getMessage().contains("socket")) {
                showMessage("\nInvalid info\n");
                return false;
            }
            //HostFileError
            else if(je.getMessage().contains("UnknownHost")) {

                //Prompt user to add key to host file.
                boolean addAHost = addHost(session.getHostKey().getKey(), this.knownHostsFile);
                if (!addAHost) {
                    showMessage("\nCannot connect without known hosts file!");
                    return false;
                }
                else {
                    boolean success = setKnownHostsFile(this.knownHostsFile);
                    if (!success) {
                        showMessage("Unable to set host file\n");
                        return false;
                    }
                    else {
                        //Retry connection after setting up new host file key.
                        try {
                            this.session = this.jsch.getSession(user.getUserName(), this.hostIP, this.portNumber);
                            this.session.setPassword(user.getPassword());
                            this.session.connect();
                        }
                        catch (JSchException tryAgain) {
                            System.err.println(tryAgain.getCause().toString());
                           showMessage("Unable to connect!");
                        }
                    }
                }
            }
        }
       return true;
    }

    /**
     * Handles SFTP Menu user input.
     */
    public void commands() {
        if (!isConnected()) {
            showMessage("\nNot Connected!");
        }

        boolean quit = false;
        while(!quit) {
            showCommandMenu();
            String userInput = sc.nextLine();
            switch(userInput) {
                case "1": {
                    listCurrentDirectory();
                } break;
                case "2": {
                    listCurrentFiles();
                } break;
                case "3": {
                    showMessage("\nAdd commands here\n");
                } break;
                case "4": {
                    setFileDisplay();
                } break;
                case "5": {
                    quit = true;
                } break;
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }

    }

    /**
     * Sets whether or not to display full file information.
     */
    private void setFileDisplay() {
        showMessage("Show Full File Information? (Y/N): ");
        String userInput = sc.nextLine();
        if (userInput.equalsIgnoreCase("y")) {
            this.fileDisplay = true;
        }
        else if (userInput.equalsIgnoreCase("n")) {
            this.fileDisplay = false;
        }
    }

    /**
     * Lists the current remote working directory.
     */
    private void listCurrentDirectory() {
        if(!isConnected()){
            return;

        }
        try {
            System.out.println(this.channel.pwd());
        }
        catch (SftpException je) {
            //TODO(): Handle this more gracefully?
            showMessage(je.toString());
        }
    }

    /**
     * Displays the SFTP Menu to the user.
     */
    private void showCommandMenu() {
        System.out.println("\nSFTP Menu:\n" +
                "\t1.List Current Directory\n" +
                "\t2.List Files in Current Remote Directory\n" +
                "\t3.MORE OPTIONS ADDED HERE\n" +
                "\t4.Change Full File Info Display\n" +
                "\t5.Quit");
    }

    /**
     * Attempts to quit the server the connection.
     * @return true on success, false otherwise.
     */
    public boolean quit() {
        if (this.session != null) {
            if (this.channel != null){
               this.channel.quit();
            }
            this.session.disconnect();
            this.isConnected = false;
            return true;
        }
        return false;
    }

    /**
     * Lists files on the remote working directory.
     */
    private void listCurrentFiles() {
        if (!isConnected()) {
            return;
        }
        try {
            //TODO(): Handle Unchecked Assignment.
            Vector<ChannelSftp.LsEntry> ls = channel.ls(channel.pwd());
            if (ls == null) {
                return;
            }
            for (ChannelSftp.LsEntry s : ls) {
                if(this.fileDisplay) {
                    System.out.println(s.getLongname());
                }
                else {
                    System.out.println(s.getFilename());
                }
            }
        }
        catch (SftpException je) {
            //TODO(): Handle this more gracefully?
            showMessage(je.toString());
        }
    }

    /**
     * Adds a host key to the specified file.
     * Modified from https://stackoverflow.com/questions/19063115/jschexception-unknownhostkey
     * @param key key that is to be added to the file
     * @param fileName file that will hold the key.
     * @return true on success, false otherwise.
     */
    private boolean addHost(String key, String fileName){
        //NOTE(gelever): this is a bit messy to display, but necessary?
        showMessage("\nAdd to host file?(Y/N): \n" + this.hostIP + " ssh-rsa " + key);
        String userInput = sc.nextLine();
        if(userInput.equalsIgnoreCase("y")) {
            try {
                FileWriter tmpwriter;
                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                tmpwriter = new FileWriter(file);
                tmpwriter.append(this.hostIP + " ssh-rsa " + key + "\n");

                tmpwriter.flush();
                tmpwriter.close();

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * Sets the known hosts file.  Creates it if it doesn't exist.
     * Creates default file at user.home/.ssh/sftp_hosts if no file is specified.
     * @param fileName Where to create the file.
     * @return true on success, false otherwise.
     */
    private boolean setKnownHostsFile(String fileName) {

        try {
            this.jsch.setKnownHosts(fileName);
        }
        catch (JSchException je) {
            //showMessage(je.toString());
            if (fileName.equals("")) {
                String userHome = System.getProperty( "user.home" );
                this.knownHostsFile = userHome + "/.ssh/sftp_hosts";

                fileName = this.knownHostsFile;
            }
            try {
                File file = new File(fileName);

                boolean success = file.createNewFile();

                this.jsch.setKnownHosts(fileName);
            }
            catch (IOException e) {
                System.err.println("Error Creating File " + e);
                return false;
            }
            catch (JSchException tryAgain) {
                showMessage("Unable to set knownHosts file");
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if all the required information has been set.
     * @return true if evertything is valid, false otherwise.
     */
    public boolean isValid() {
        return this.jsch != null &&
                this.hostIP != null &&
                this.knownHostsFile != null &&
                this.portNumber >= 0;
    }

    /**
     * Displays a message to the user.
     * @param s Message to display.
     */
    public void showMessage(String s) {
        System.out.println(s + " ");
    }
}
