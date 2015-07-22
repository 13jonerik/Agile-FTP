package com.company;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.Vector;

/**
 * Handles the logging in and the commands sent to the server.
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since July 5, 2015
 */
public class CommandSFTP {
    private String hostIP = "";
    private String knownHostsFile = "";
    private int portNumber;
    private final String [] hostChecking = {"StrictHostKeyChecking", "yes"};
    private User user = null;

    private JSch jsch = null;
    private Session session = null;
    private ChannelSftp channel = null;

    private static final int timeout = 10000;

    private boolean fileDisplay = false;
    private boolean checkConnect = false;


    private static Scanner sc = new Scanner(System.in);


    /**
     * Default constructor.
     */
    public CommandSFTP() {
        this.jsch = new JSch();
        JSch.setConfig(hostChecking[0], hostChecking[1]);
        this.portNumber = 22;
    }


    /**
     * Constructor that sets all parameters necessary to connect
     * @param hostIp server ip address
     * @param portNumber port number to connect to
     * @param knownHostsFile ssh key known hosts file to check
     */
    public CommandSFTP(String hostIp, int portNumber, String knownHostsFile){

        this.jsch = new JSch();
        JSch.setConfig(hostChecking[0], hostChecking[1]);
        this.portNumber = portNumber;
        this.hostIP = hostIp;
        this.knownHostsFile = knownHostsFile;
    }


    /**
     * Returns whether or not the user is currently connected.
     * @return true if connected, false otherwise.
     */
    public boolean checkConnect() {
        return this.checkConnect;
    }


    /**
     * Prompts user to enter server information.
     */
    public void promptInfo() {
        //NOTE(gelever): should this be set after a successful connection?
        //checkConnect = true;                     //reset to true so user can log back in from main menu
        showMessage("Host IP: ");
        this.hostIP = sc.nextLine();

        int userInput;

        while(true) {
            showMessage("Port Number (Default: 22): ");
            String userString = sc.nextLine();
            if (userString.equals("")) {
                this.portNumber = 22;
                break;
            }
            try {
                userInput = Integer.parseInt(userString);
            }
            catch (NumberFormatException e) {
                showMessage("Positive Integers Only!\n");
                continue;
            }
            if (userInput > 0 && userInput < 65536) {
                this.portNumber = userInput;
                break;
            }
            else {
                showMessage("Positive Integers Only!\n");
            }
        }
        showMessage("KnownHosts File (~/.ssh/sftp_hosts): ");
        this.knownHostsFile = sc.nextLine();
    }


    /**
     * Sets the user information
     * @param user holds user information
     */
    public void setUser(User user) {
        this.user = user;
    }


    /**
     * Connects the user to the specified server. Assumes server information has already been provided.
     * @return true if successful connection, false otherwise.
     */
    public boolean connect() {
        if (!(this.user != null &&
                this.user.validUser() && this.isValid())) {
            return false;
        }
        if(!setKnownHostsFile(this.knownHostsFile)) {
            return false;
        }
        if (!(setSession(this.user) && connectSession(this.user) && channelConnect())) {
            return false;
        }
        this.checkConnect = true;
        this.user.clearPass();
        return true;
    }


    /**
     * Requests a session from jsch with the server information and sets the user password.
     * @param user  User information
     * @return true on success, false otherwise
     */
    private boolean setSession(User user) {
        try {
            this.session = this.jsch.getSession(user.getUserName(), this.hostIP, this.portNumber);
            this.session.setPassword(user.getPassword());
        }
        catch (JSchException je) {
            //TODO: Handle exception properly?
            showMessage("Unable to Connect!\n");
            return false;
        }
        return true;
    }


    /**
     * Connects the channel to the current session.
     * @return true on success, false otherwise.
     */
    private boolean channelConnect() {
        try {
            this.channel = (ChannelSftp)this.session.openChannel("sftp");
            this.channel.connect(timeout);
        }
        catch (JSchException e) {
            showMessage("Unable to Connect!");
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
            this.session.connect(timeout);
        }
        catch (JSchException je) {
            //Connection error
            if (je.getMessage().contains("timeout")) {
                showMessage("Server Timeout!");
                return false;
            }
            else if (je.getMessage().contains("socket")) {
                showMessage("\nUnable to connect to server!\n");
                return false;
            }
            //Unknown Host
            else if(je.getMessage().contains("UnknownHost")) {
                showMessage("Can't reach server!\n");
                return false;

            }

            //HostFileError
            else if(je.getMessage().contains("reject HostKey")) {

                //Prompt user to add key to host file.
                if(session.getHostKey() != null) {
                    showMessage("Add '" + this.hostIP + "' to host file?(Y/N): ");
                    String userInput = sc.nextLine();
                    if(userInput.equalsIgnoreCase("y")) {
                        showMessage("\nCannot connect without known hosts file!");
                        return false;
                    }
                    if(!addHost(session.getHostKey().getKey(), this.knownHostsFile)){
                        showMessage("Error setting known hosts file!");
                        return false;
                    }
                }
                else {
                    showMessage("Unable to reach server!");
                    return false;
                }

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
        return true;
    }


    /**
     * Handles SFTP Menu user input.
     */
    public void mainSFTPMenu() {
        while(checkConnect()) {
            int userInput = CommandMenu.showMainSFTPMenu();
            switch(userInput) {
                case 1: {
                    remoteSFTPMenu();
                } break;
                case 2: {
                    localSFTPMenu();
                } break;
                case 3: {
                    optionsSFTPMenu();
                } break;
                case 0: {
                    this.quit();
                    return;
                }
                default:
                    showMessage("Invalid Command!\n");
            }
        }

    }


    /**
     * Handles options menu system.
     */
    private void optionsSFTPMenu() {
        while(checkConnect()) {
            int userInput = CommandMenu.showOptionsMenu();
            switch(userInput) {
                case 1: {
                    setTimeout();
                } break;
                case 2: {
                    setFileDisplay();
                } break;
                case 3: {
                    showMessage("MORE OPTIONS");
                } break;
                case 4: {
                    showMessage("MORE OPTIONS");
                } break;
                case 5: {
                    return;
                }
                case 0: {
                    this.quit();
                    return;
                }
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }

    }


    /**
     * Handles remote directory menu system for remote management
     */
    private void remoteSFTPDirMenu() {
        while(checkConnect()) {
            int userInput = CommandMenu.showDirectoryMenu("Remote");
            switch(userInput) {
                case 1: {
                    listCurrentRemoteDirectory();
                } break;
                case 2: {
                    listCurrentRemoteFiles();
                } break;
                case 3: {
                    changeRemoteDirectory();
                } break;
                case 4: {
                    createRemoteDir();
                } break;
                case 5: {
                    deleteRemoteDirectory();
                } break;
                case 6: {
                    renameRemoteDirectory();
                } break;
                case 7: {
                    return;
                }
                case 0: {
                    this.quit();
                    return;
                }
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }

    }

    /**
     * Handles remote file menu system for remote management
     */
    private void remoteSFTPFileMenu() {
        while(checkConnect()) {
            int userInput = CommandMenu.showRemoteFileMenu();
            switch(userInput) {
                case 1: {
                    uploadRemoteFile();
                } break;
                case 2: {
                    getMultipleRemote();
                } break;
                case 3: {
                    deleteRemoteFile();
                } break;
                case 4: {
                    listCurrentRemoteFiles();
                } break;
                case 5: {
                    renameRemoteFile();
                } break;
                case 6: {
                    return;
                }
                case 0: {
                    this.quit();
                    return;
                }
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }
    }

    /**
     * Handles remote file and directory menu system for remote management
     */
    private void remoteSFTPMenu() {
        //TODO(gelever): Finish up these calls.
        while(checkConnect()) {
            int userInput = CommandMenu.showManageMenu("Remote");
            switch(userInput) {
                case 1: {
                    remoteSFTPFileMenu();
                } break;
                case 2: {
                    remoteSFTPDirMenu();
                } break;
                case 3: {
                    //TODO(): Finish remote permissions menu and uncomment out.
                    //remoteSFTPPermissionMenu();
                } break;
                case 4: {
                    return;
                }
                case 0: {
                    this.quit();
                    return;
                }
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }

    }

    /**
     * Handles remote file permissions menu system for remote management
     */
    private void remoteSFTPPermissionMenu() {
        //TODO(gelever): Finish up these calls.
        while(checkConnect()) {
            int userInput = CommandMenu.showRemotePermissionsMenu();
            switch(userInput) {
                case 1: {

                } break;
                case 2: {

                } break;
                case 3: {
                    return;
                }
                case 0: {
                    this.quit();
                    return;
                }
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }

    }



    /**
     * Handles local file menu system for local file management.
     */
    private void localSFTPMenu() {
        while(checkConnect()) {
            int userInput = CommandMenu.showLocalFileMenu();
            switch(userInput) {
                case 1: {
                    listCurrentLocalDirectory();
                } break;
                case 2: {
                    changeCurrentLocalDirectory();
                } break;
                case 3: {
                    listCurrentLocalDirectoryFiles();
                } break;
                case 4: {
                    //TODO(gelever): Implement this.
                    renameLocalFile();
                } break;
                case 5: {
                    return;
                }
                case 0: {
                    this.quit();
                    return;
                }
                default:
                    showMessage("\nInvalid Command!\n");
            }
        }

    }


    /**
     * Renames a local file.
     * @param oldName original file name
     * @param newName new file name
     */
    private void renameLocalFile(String oldName, String newName) {
        File oldFile = new File(this.channel.lpwd() + "/" + oldName);
        File newFile = new File(this.channel.lpwd() + "/" + newName);
        if(newFile.exists()) {
            showMessage("\nNew File Name Already Exists!\n");
            return;
        }
        if(!oldFile.renameTo(newFile)) {
            showMessage("\nUnable to Rename File\n");
        }
    }


    /**
     * Prompts the user to rename a local file.
      */
    private void renameLocalFile() {
        showMessage("File to Rename: ");
        String oldFileName = sc.nextLine();

        showMessage("New File Name: ");
        String newFileName = sc.nextLine();
        renameLocalFile(oldFileName, newFileName);
    }


    /**
     * Renames a remote directory or file.
     * @param oldName name of directory or file to be renamed
     * @param newName what to rename the directory or file
     */
    private void renameRemote(String oldName, String newName) {
        try {
            channel.rename(oldName, newName);
        }
        catch (SftpException e) {
            showMessage("Unable to Rename!");
        }
    }


    /**
     * Prompts the user to rename a remote file.
     */
    private void renameRemoteFile() {
        showMessage("File to rename: ");
        String oldFileName = sc.nextLine();

        showMessage("New File Name: ");
        String newFileName = sc.nextLine();
        renameRemote(oldFileName, newFileName);
    }


    /**
     * Prompts the user to rename a remote directory.
     */
    private void renameRemoteDirectory() {
        showMessage("Directory to rename: ");
        String oldFileName = sc.nextLine();

        showMessage("New Directory Name: ");
        String newFileName = sc.nextLine();
        renameRemote(oldFileName, newFileName);
    }


    /**
     * Prompts the user to delete a file.
     */
    private void deleteRemoteFile() {
        showMessage("File to Delete: ");
        deleteRemoteFile(sc.nextLine());
    }


    /**
     * Attempts to delete a remote file.
     * @param fileName file to delete.
     */
    private void deleteRemoteFile(String fileName) {
        try {
            this.channel.rm(fileName);
        }
        catch (SftpException e) {
            showMessage("Unable to Delete File!");
        }
    }


    /**
     * Uploads file to current remote directory.
     * @param fileName file to upload
     */
    private void uploadRemoteFile(String fileName) {
        //TODO: Create overwrite prompt for remote file upload. is possible?
        String absoluteFileName = this.channel.lpwd() + "/" + fileName;
        File testExists = new File(absoluteFileName);
        if(!testExists.exists()) {
            showMessage("Unable to Find Local File: " + fileName );
            return;
        }
        try {
            this.channel.put(absoluteFileName, this.channel.pwd());

        } catch (SftpException e) {
            showMessage("Unable to Upload File!");
        }
    }


    /**
     * Prompts the user to select file to upload to remote directory.
     */
    private void uploadRemoteFile() {
        showMessage("File to Upload: ");
        uploadRemoteFile(sc.nextLine());
    }


    /**
     * changes the current remote directory.
     * @param fileName directory to change into.
     */
    private void changeRemoteDirectory(String fileName) {
        try {
            this.channel.cd(fileName);
        }
        catch (SftpException je) {
            showMessage("Invalid Selection!");
        }
    }


    /**
     * Prompts user to change remote directory.
     */
    private void changeRemoteDirectory() {
        showMessage("Remote Directory: ");
        changeRemoteDirectory(sc.nextLine());
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
    private void listCurrentRemoteDirectory() {
        try {
            System.out.println(this.channel.pwd());
        }
        catch (SftpException je) {
            showMessage("Error in Listing Directory!");
        }
    }


    /**
     * Changes the current local directory.
     */
    private void changeCurrentLocalDirectory(String directory) {
       try {
           this.channel.lcd(directory);
       }
       catch (SftpException e) {
            showMessage("Unable to change local directory!");
       }
    }
    /**
     * Changes the current local directory.
     */
    private void changeCurrentLocalDirectory() {
        showMessage("Directory: ");
        changeCurrentLocalDirectory(sc.nextLine());
    }


    /**
     * Displays the current local directory.
     */
    private void listCurrentLocalDirectory() {
        showMessage(this.channel.lpwd() + "\n");
    }


    /**
     * Shows all files and directories in current local directroy.
     */
    private void listCurrentLocalDirectoryFiles() {
        Path directory = Paths.get(this.channel.lpwd());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*")){
            for (Path path : stream) {
                if (this.fileDisplay) {
                    System.out.println(path);
                }
                else {
                    System.out.println(path.getFileName());
                }
            }
        }
        catch (IOException e) {
            showMessage("Current Local Directory Not Found!");
        }
    }


    /**
     * Retrieves a file from the current remote directory.
     * @param fileName File to receive.
     */
    private void getRemoteFile(String fileName) {
        try {
            this.channel.get(fileName, fileName);
        }
        catch(SftpException e) {
            showMessage("Unable to retrieve remote file: " + fileName + "\n");
        }
    }


    /**
     * Prompts user to receive remote file.
     */
    private void getRemoteFile() {
        showMessage("File Name: ");
        String userInput = sc.nextLine();

        //Checks if file already exists and offers the user to overwrite.
        if(overWriteLocalFile(userInput)) {
            getRemoteFile(userInput);
        }
    }


    /**
     * Prompts user to retrieve multiple remote files.
     */
    private void getMultipleRemote() {
        showMessage("Files (space separated): ");
        String [] files = sc.nextLine().split(" ");
        for (String s: files) {
            getRemoteFile(s);
        }
    }


    /**
     * Creates a new remote directory in the current remote working directory.
     * @param dirName Name of new directory.
     */
    private void createRemoteDir(String dirName) {
        try {
            this.channel.mkdir(dirName);
        }
        catch (SftpException e) {
            showMessage("Unable to create new Directory");
        }
    }


    /**
     * Prompts user to create new remote directory.
     */
    private void createRemoteDir() {
        showMessage("Directory to create: ");
        createRemoteDir(sc.nextLine());
    }


    /**
     * Attempts to delete a remote directory.
     * @param fileName file to delete.
     */
    private void deleteRemoteDirectory(String fileName) {
        try {
            this.channel.rmdir(fileName);
        }
        catch (SftpException e) {
            showMessage("Unable to delete Directory");
        }
    }


    /**
     * Prompts user to delete remote file.
     */
    private void deleteRemoteDirectory() {
        showMessage("Directory to delete: ");
        deleteRemoteDirectory(sc.nextLine());
    }


    /**
     * Sets the timeout for the session.
     * @param timeout time to set for timeout.
     */
    private void setTimeout(int timeout) {
        //TODO(gelever): find out what units these are.  ms?
        try {
            this.session.setTimeout(timeout);
        }
        catch (JSchException e) {
            showMessage("Unable to set timeout!");
        }

    }


    /**
     * Prompts user to set timeout length
     */
    private void setTimeout() {
        int userInput;
        while(true) {
            showMessage("Timeout in Milliseconds: ");
            String userString = sc.nextLine();

            try {
                userInput = Integer.parseInt(userString);
            }
            catch (NumberFormatException e) {
                showMessage("Positive Integers Only!\n");
                continue;
            }
            if (userInput >= 0) {
                setTimeout(userInput);
                break;
            }
            else {
                showMessage("Positive Integers Only!\n");
            }

        }
    }


    /**
     * Checks if local file exists and prompts user whether or not to overwrite local file w/ remote file.
     * @param fileName File to look for to check if it exists.
     * @return true to over write or not found, false otherwise.
     */
    private boolean overWriteLocalFile(String fileName) {
        //Absolute file path
        if(new File(fileName).isFile() || new File(fileName).isDirectory()) {
            showMessage("OverWrite Local File?: " + fileName + " (Y/N): ");
            String userInput = sc.nextLine();
            return userInput.equalsIgnoreCase("y");
        }

        //Relative file path
        File relFileName = new File(this.channel.lpwd() + "/" + fileName);
        if(relFileName.isFile() ||
                relFileName.isDirectory()) {
            showMessage("Local Overwrite " + fileName + "? (Y/N): ");
            String userInput = sc.nextLine();
            return userInput.equalsIgnoreCase("y");
        }
        return true;
    }


    /**
     * Lists files on the remote working directory.
     */
    private void listCurrentRemoteFiles() {
        try {
            Vector ls = channel.ls(channel.pwd());
            if (ls == null) {
                return;
            }
            for (Object s : ls) {
                if (s instanceof  ChannelSftp.LsEntry) {
                    if (this.fileDisplay) {
                        System.out.println(((ChannelSftp.LsEntry)s).getLongname());
                    } else {
                        System.out.println(((ChannelSftp.LsEntry)s).getFilename());
                    }
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
            return false;
        }
        return true;
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
                if (! new File(userHome + ".ssh").isDirectory()) {
                    new File(userHome +"/.ssh").mkdir();
                }
                this.knownHostsFile = userHome + "/.ssh/sftp_hosts";

                fileName = this.knownHostsFile;
            }
            try {
                if (fileName.startsWith("~/")) {
                    fileName = System.getProperty("user.home") + fileName.subSequence(1, fileName.length() );
                }
                File file = new File(fileName);
                File directory = new File(file.getParentFile().getAbsolutePath());
                directory.mkdirs();

                boolean success = file.createNewFile();

                this.jsch.setKnownHosts(fileName);
                this.knownHostsFile = fileName;
            }
            catch (IOException e) {
                showMessage("Error Creating Known Hosts File");
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
                this.portNumber >= 0 &&
                this.user != null;
    }


    /**
     * Attempts to quit the server the connection.
     * @return true on success, false otherwise.
     */
    public boolean quit() {
        if (this.checkConnect()) {
            this.checkConnect = false;
            if (this.session != null) {
                if (this.channel != null) {
                    this.channel.quit();
                }
                this.session.disconnect();
                showMessage("Server Disconnected! \n");
                return true;
            }
        }
        return false;
    }


    /**
     * Displays a message to the user.
     * @param s Message to display.
     */
    public void showMessage(String s) {
        System.out.print(s);
    }
}
