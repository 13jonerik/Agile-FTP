package com.company;

import com.jcraft.jsch.*;

import java.io.*;
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
    private final static String [] hostChecking = {"StrictHostKeyChecking", "ask"};
    private User user = null;

    private JSch jsch = null;
    private Session session = null;
    private ChannelSftp channel = null;

    private int timeout = 10000;

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
        if (this.session != null && this.session.isConnected() && this.checkConnect) {
            return true;
        }
        else {
            this.quit();
            return false;
        }
    }


    /**
     * Prompts user to enter server information.
     */
    public void promptInfo() {
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
    public boolean connect() throws JSchException, IOException {
        if (!isValid()) {
            showMessage("\nRequires Valid Server Information!\n");
            return false;
        }

        this.setKnownHostsFile(this.knownHostsFile);
        this.setSession();
        this.connectSession();
        this.channelConnect();

        this.checkConnect = this.session.isConnected();
        this.user.clearPass();
        return true;
    }


    /**
     * Requests a session from jsch with the server information and sets the user password.
     * @throws JSchException If an error occurs when setting up the session.
     */
    private void setSession() throws JSchException {
        this.session = this.jsch.getSession(this.user.getUserName(), this.hostIP, this.portNumber);
        this.session.setConfig(hostChecking[0], hostChecking[1]);
        this.session.setUserInfo(this.user);
        this.session.setPassword(this.user.getPassword());
    }


    /**
     * Connects the channel to the current session.
     * @throws JSchException If an error occurs when setting up the session.
     */
    private void channelConnect() throws JSchException{
        this.channel = (ChannelSftp)this.session.openChannel("sftp");
        this.channel.connect(this.timeout);
    }


    /**
     * Attempts to correct the current session.
     * @throws JSchException If an error occurs when setting up the session.
     * @throws IOException If an error occurs when setting the known hosts file.
     */
    private void connectSession() throws JSchException, IOException {
        this.session.connect(this.timeout);
    }


    /**
     * Renames a local file.
     * @param oldName original file name
     * @param newName new file name
     */
    public void renameLocalFile(String oldName, String newName) throws IOException {
        if (!this.checkConnect()) {
            return;
        }
        File oldFile = new File(this.channel.lpwd() + "/" + oldName);
        File newFile = new File(this.channel.lpwd() + "/" + newName);
        if(newFile.exists()) {
            showMessage("\nNew File Name Already Exists!\n");
            return;
        }
        if(!oldFile.renameTo(newFile)) {
            throw new IOException("Unable to Rename File");
        }
    }


    /**
     * Prompts the user to rename a local file.
     */
    public void renameLocalFile() throws IOException {
        if (!this.checkConnect()) {
            return;
        }
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
    public void renameRemote(String oldName, String newName) throws SftpException{
        if (!this.checkConnect()) {
            return;
        }
        channel.rename(oldName, newName);
    }


    /**
     * Prompts the user to rename a remote file.
     */
    public void renameRemoteFile() throws SftpException{
        if (!this.checkConnect()) {
            return;
        }
        showMessage("File to rename: ");
        String oldFileName = sc.nextLine();

        showMessage("New File Name: ");
        String newFileName = sc.nextLine();
        renameRemote(oldFileName, newFileName);
    }


    /**
     * Prompts the user to rename a remote directory.
     */
    public void renameRemoteDirectory() throws SftpException{
        if (!this.checkConnect()) {
            return;
        }
        showMessage("Directory to rename: ");
        String oldFileName = sc.nextLine();

        showMessage("New Directory Name: ");
        String newFileName = sc.nextLine();
        renameRemote(oldFileName, newFileName);
    }


    /**
     * Prompts the user to delete a file.
     */
    public void deleteRemoteFile() throws SftpException{
        showMessage("File to Delete: ");
        deleteRemoteFile(sc.nextLine());
    }


    /**
     * Attempts to delete a remote file.
     * @param fileName file to delete.
     */
    public void deleteRemoteFile(String fileName) throws  SftpException{
        if (!this.checkConnect()) {
            return;
        }
        this.channel.rm(fileName);
    }


    /**
     * Uploads file to current remote directory.
     * @param fileName file to upload
     */
    public void uploadRemoteFile(String fileName) throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        //TODO: Create overwrite prompt for remote file upload. is possible?
        String absoluteFileName = this.channel.lpwd() + "/" + fileName;
        File testExists = new File(absoluteFileName);
        if(!testExists.exists()) {
            showMessage("Unable to Find Local File: " + fileName );
            return;
        }
        this.channel.put(absoluteFileName, this.channel.pwd());

    }


    /**
     * Prompts the user to select file to upload to remote directory.
     */
    public void uploadRemoteFile() throws SftpException {
        showMessage("File to Upload: ");
        uploadRemoteFile(sc.nextLine());
    }


    /**
     * changes the current remote directory.
     * @param fileName directory to change into.
     */
    public void changeRemoteDirectory(String fileName) throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        this.channel.cd(fileName);
    }


    /**
     * Prompts user to change remote directory.
     */
    public void changeRemoteDirectory() throws SftpException {
        showMessage("Remote Directory: ");
        changeRemoteDirectory(sc.nextLine());
    }


    /**
     * Sets whether or not to display full file information.
     */
    public void setFileDisplay() {
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
    public void listCurrentRemoteDirectory() throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        System.out.println(this.channel.pwd());
    }


    /**
     * Changes the current local directory.
     * @param directory Directory to change into.
     */
    public void changeCurrentLocalDirectory(String directory) throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        this.channel.lcd(directory);
    }
    /**
     * Changes the current local directory.
     */
    public void changeCurrentLocalDirectory() throws SftpException {
        showMessage("Directory: ");
        changeCurrentLocalDirectory(sc.nextLine());
    }


    /**
     * Displays the current local directory.
     */
    public void listCurrentLocalDirectory() {
        if (!this.checkConnect()) {
            return;
        }
        showMessage(this.channel.lpwd() + "\n");
    }


    /**
     * Shows all files and directories in current local directory.
     */
    public void listCurrentLocalDirectoryFiles() throws IOException{
        if (!this.checkConnect()) {
            return;
        }
        Path directory = Paths.get(this.channel.lpwd());
        DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*");
        System.out.println();
        for (Path path : stream) {
            if (this.fileDisplay) {
                System.out.println(path);
            }
            else {
                System.out.println(path.getFileName());
            }
        }
        stream.close();
    }


    /**
     * Retrieves a file from the current remote directory.
     * @param fileName File to receive.
     */
    public void getRemoteFile(String fileName) throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        this.channel.get(fileName, fileName);
    }


    /**
     * Prompts user to receive remote file.
     */
    public void getRemoteFile() throws SftpException {
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
    public void getMultipleRemote() throws SftpException {
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
    public void createRemoteDir(String dirName) throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        this.channel.mkdir(dirName);
    }


    /**
     * Prompts user to create new remote directory.
     */
    public void createRemoteDir() throws SftpException {
        showMessage("Directory to create: ");
        createRemoteDir(sc.nextLine());
    }


    /**
     * Attempts to delete a remote directory.
     * @param fileName file to delete.
     */
    public void deleteRemoteDirectory(String fileName) throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        if (!fileName.startsWith("/")) {
            fileName = channel.pwd() + "/" + fileName;
        }
        Vector<ChannelSftp.LsEntry> ls = channel.ls(fileName);
        if (ls == null ) {
            return;
        }
        for (ChannelSftp.LsEntry item : ls) {
            if (item != null) {
                boolean isDir = item.getAttrs().isDir();
                boolean isRootDir = item.getFilename().equals(".") || item.getFilename().equals("..");
                String filePath = fileName + "/" + item.getFilename();
                if (isDir) {
                    if (!isRootDir) {
                        this.deleteRemoteDirectory(filePath);
                    }
                } else {
                    this.deleteRemoteFile(filePath);
                }
            }
        }
        this.channel.rmdir(fileName);
    }


    /**
     * Prompts user to delete remote file.
     */
    public void deleteRemoteDirectory() throws SftpException {
        showMessage("Directory to delete: ");
        deleteRemoteDirectory(sc.nextLine());
    }


    /**
     * Sets the timeout for the session.
     * @param timeout time to set for timeout.
     */
    public void setTimeout(int timeout) throws JSchException {
        if (!this.checkConnect()) {
            return;
        }
        this.session.setTimeout(timeout);
    }


    /**
     * Prompts user to set timeout length
     */
    public void setTimeout() throws JSchException {
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
     * Gets the current timeout.
     * @return the current timeout. -1 if unable to retrieve.
     */
    public int getTimeout() {
        if (!this.checkConnect()) {
            return -1;
        }
        return this.session.getTimeout();
    }


    /**
     * Checks if local file exists and prompts user whether or not to overwrite local file w/ remote file.
     * @param fileName File to look for to check if it exists.
     * @return true to over write or not found, false otherwise.
     */
    public boolean overWriteLocalFile(String fileName) {
        if (!this.checkConnect()) {
            return false;
        }

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
    public void listCurrentRemoteFiles() throws SftpException {
        if (!this.checkConnect()) {
            return;
        }
        Vector ls = channel.ls(channel.pwd());
        if (ls == null) {
            return;
        }
        System.out.println();
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


    /**
     * Sets the known hosts file.  Creates it if it doesn't exist.
     * Creates default file at user.home/.ssh/sftp_hosts if no file is specified.
     * @param fileName Where to create the file.
     * @throws JSchException When unable to set host.
     * @throws IOException When unable to create known host file.
     */
    public void setKnownHostsFile(String fileName) throws JSchException, IOException{
        if (fileName.equals("")) {
            String userHome = System.getProperty( "user.home" );
            if (! new File(userHome + ".ssh").isDirectory()) {
                new File(userHome +"/.ssh").mkdir();
            }
            this.knownHostsFile = userHome + "/.ssh/sftp_hosts";

            fileName = this.knownHostsFile;
        }
        else if (fileName.startsWith("~/")) {
            fileName = System.getProperty("user.home") + fileName.subSequence(1, fileName.length() );
        }
        File file = new File(fileName);

        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                File directory = new File(file.getParentFile().getAbsolutePath());
                directory.mkdirs();
            }
            file.createNewFile();
        }

        this.jsch.setKnownHosts(fileName);
        this.knownHostsFile = fileName;
    }


    /**
     * Determines if all the required information has been set.
     * @return true if everything is valid, false otherwise.
     */
    public boolean isValid() {
        return this.jsch != null &&
                this.hostIP != null &&
                !this.hostIP.equals("") &&
                this.knownHostsFile != null &&
                this.portNumber >= 0 &&
                this.user != null;
    }


    /**
     * Attempts to quit the server the connection.
     * @return true on success, false otherwise.
     */
    public boolean quit() {
        if (this.checkConnect) {
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
