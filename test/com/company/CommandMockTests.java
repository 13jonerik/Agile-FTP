package com.company;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;


/**
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since July 18, 2015
 */
public class CommandMockTests {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private Method method;
    private PrintStream orig = System.out;
    private CommandSFTP validCommand;
    private CommandSFTP invalidCommand;
    private String host = "linux.cecs.pdx.edu";
    private String user = "user";
    private String password = "password";


    @Before
    public void before() throws Exception {
        //Assumes knownhostfile exists
        //Tests CommandSFTP requires valid password
        validCommand = new CommandSFTP(host, 22, System.getProperty("user.home") + "/.ssh/sftp_hosts");
        invalidCommand = new CommandSFTP();
        validCommand.setUser(new User(user, password));
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void after() throws Exception {
        validCommand.quit();
        System.setOut(null);
        System.setErr(null);
    }


    /**
     * Method: isNotConnected()
     */
    @Test
    public void testIsNotConnected() throws Exception {
        assertEquals(false, validCommand.checkConnect());
    }

    /**
     * Method: checkConnect()
     */
    @Test
    public void testIsConnected() throws Exception {
        validCommand.connect();
        assertEquals(true, validCommand.checkConnect());
    }

    /**
     * Method: connect(User user)
     */
    @Test
    public void testConnect() throws Exception {

        boolean success = validCommand.connect();
        assertEquals(true, success);
        //assert=
        validCommand.quit();
    }

    /**
     * Method: quit()
     */
    @Test
    public void testQuit() throws Exception {
        validCommand.connect();
        boolean success = validCommand.quit();

        assertEquals(true, success);
    }

    /**
     * Method: isValid()
     */
    @Test
    public void testIsValid() throws Exception {
        assertEquals(true, validCommand.isValid());
        assertEquals(false, invalidCommand.isValid());
    }

    @Test
    public void testCheckConnected() throws Exception {
        method = validCommand.getClass().getDeclaredMethod("checkConnect");
        method.setAccessible(true);
        Boolean result;
        result = (Boolean) method.invoke(invalidCommand);
        assertEquals(result, false);
        validCommand.connect();
        result = (Boolean) method.invoke(validCommand);
        assertEquals(result, true);
    }

    @Test
    @SuppressWarnings("JSchException")
    public void testSetKnownHostsFile() throws Exception {
        method = validCommand.getClass().getDeclaredMethod("setKnownHostsFile", String.class);
        method.setAccessible(true);
        Field knownHost = validCommand.getClass().getDeclaredField("knownHostsFile");
        knownHost.setAccessible(true);
        String fileName = System.getProperty("user.home") + "/testHostsFile.txt";

        method.invoke(validCommand, fileName);
        Object val = knownHost.get(validCommand);

        assertEquals(val.equals(fileName), true);

        File file = new File(fileName);
        boolean exists = file.exists();
        assertEquals(exists, true);
        file.delete();
    }



    @Test
    public void testAddHost() throws Exception{
        method = validCommand.getClass().getDeclaredMethod("addHost", String.class, String.class);
        method.setAccessible(true);
        Boolean result;
        String fileName = System.getProperty("user.home") + "/testHostsFile.txt";

        method.invoke(validCommand, "key", fileName);


        File file = new File(fileName);
        boolean exists = file.exists();
        assertEquals(exists, true);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String addedToHost = br.readLine();
        assertEquals(addedToHost, this.host + " ssh-rsa " + "key");
        br.close();
        file.delete();

    }

    @Test
    @SuppressWarnings("SftpException")
    public void testListCurrentRemoteFiles() throws Exception{

        Method methodList = validCommand.getClass().getDeclaredMethod("listCurrentRemoteFiles");
        Method methodUpload = validCommand.getClass().getDeclaredMethod("uploadRemoteFile", String.class);
        Method methodDelete = validCommand.getClass().getDeclaredMethod("deleteRemoteFile", String.class);
        methodList.setAccessible(true);
        methodUpload.setAccessible(true);
        methodDelete.setAccessible(true);

        validCommand.connect();

        String testFile = "testFileRemote.txt";
        File file = new File(testFile);
        file.createNewFile();
        assertEquals(file.exists(), true);

        methodUpload.invoke(validCommand, testFile);
        outContent.reset();
        methodList.invoke(validCommand);

        assertEquals(outContent.toString().contains(testFile), true);
        assertEquals(outContent.toString().contains("NOTREAL.txt"), false);

        methodDelete.invoke(validCommand, testFile);

        file.delete();


    }


    @Test
    public void testCreateDeleteRemoteDir() throws Exception {
        validCommand.connect();
        Method methodCreate = validCommand.getClass().getDeclaredMethod("createRemoteDir", String.class);
        Method methodDelete = validCommand.getClass().getDeclaredMethod("deleteRemoteDirectory", String.class);
        Method methodList = validCommand.getClass().getDeclaredMethod("listCurrentRemoteFiles");

        methodCreate.setAccessible(true);
        methodDelete.setAccessible(true);
        methodList.setAccessible(true);

        String testDirectory = "TESTDIRECTORY";

        methodCreate.invoke(validCommand, testDirectory);
        outContent.reset();
        methodList.invoke(validCommand);

        assertEquals(outContent.toString().contains(testDirectory), true);
        outContent.reset();

        methodDelete.invoke(validCommand, testDirectory);
        methodList.invoke(validCommand);

        assertEquals(outContent.toString().contains(testDirectory), false);
    }

    @Test
    public void testUploadGetDeleteRemoteFile() throws Exception {
        validCommand.connect();

        Method methodRemoteFile = validCommand.getClass().getDeclaredMethod("getRemoteFile", String.class);
        Method methodList = validCommand.getClass().getDeclaredMethod("listCurrentRemoteFiles");
        Method methodUpload = validCommand.getClass().getDeclaredMethod("uploadRemoteFile", String.class);
        Method methodDelete = validCommand.getClass().getDeclaredMethod("deleteRemoteFile", String.class);
        methodList.setAccessible(true);
        methodRemoteFile.setAccessible(true);
        methodUpload.setAccessible(true);
        methodDelete.setAccessible(true);

        String testFile = "testUploadFile.txt";
        File file = new File(testFile);
        file.createNewFile();
        boolean exists = file.exists();
        assertEquals(exists, true);

        methodUpload.invoke(validCommand, testFile);

        outContent.reset();
        methodList.invoke(validCommand);

        assertEquals(outContent.toString().contains(testFile), true);

        file.delete();

        methodRemoteFile.invoke(validCommand, testFile);

        assertEquals(file.exists(), true);

        methodDelete.invoke(validCommand, testFile);

        outContent.reset();

        assertEquals(outContent.toString().contains(testFile), false);

        file.delete();
    }



    @Test
    public void testListAndChangeRemoteDirectory() throws Exception {
        validCommand.connect();
        Method methodChangeFile = validCommand.getClass().getDeclaredMethod("changeRemoteDirectory", String.class);
        Method methodListDir = validCommand.getClass().getDeclaredMethod("listCurrentRemoteDirectory");
        Method methodCreate = validCommand.getClass().getDeclaredMethod("createRemoteDir", String.class);
        Method methodDelete = validCommand.getClass().getDeclaredMethod("deleteRemoteDirectory", String.class);

        methodChangeFile.setAccessible(true);
        methodListDir.setAccessible(true);
        methodCreate.setAccessible(true);
        methodDelete.setAccessible(true);

        outContent.reset();

        String testDir = "TESTDIRECTORY";
        methodCreate.invoke(this.validCommand, testDir);
        methodChangeFile.invoke(this.validCommand, testDir);

        outContent.reset();
        methodListDir.invoke(this.validCommand);
        assertEquals(outContent.toString().contains(testDir), true);

        methodChangeFile.invoke(this.validCommand, "..");
        methodDelete.invoke(this.validCommand, testDir);

    }

    @Test
    public void testRenameRemoteFile() throws Exception {
        validCommand.connect();
        Method methodRename = validCommand.getClass().getDeclaredMethod("renameRemote", String.class, String.class);
        Method methodListDir = validCommand.getClass().getDeclaredMethod("listCurrentRemoteFiles");
        Method methodCreate = validCommand.getClass().getDeclaredMethod("uploadRemoteFile", String.class);
        Method methodDelete = validCommand.getClass().getDeclaredMethod("deleteRemoteFile", String.class);

        methodCreate.setAccessible(true);
        methodDelete.setAccessible(true);
        methodRename.setAccessible(true);
        methodListDir.setAccessible(true);

        String testFile = "TESTFILEUPLOAD";
        File file = new File(testFile);
        file.createNewFile();
        boolean exists = file.exists();
        assertEquals(exists, true);
        String newTestFileName = "NEWTESTFile";

        methodCreate.invoke(this.validCommand, testFile);

        outContent.reset();
        methodListDir.invoke(this.validCommand);
        assertEquals(outContent.toString().contains(testFile), true);

        outContent.reset();
        methodRename.invoke(this.validCommand, testFile, newTestFileName);
        methodListDir.invoke(this.validCommand);
        assertEquals(outContent.toString().contains(newTestFileName), true);
        assertEquals(outContent.toString().contains(testFile), false);

        outContent.reset();
        methodDelete.invoke(this.validCommand, newTestFileName);
        methodListDir.invoke(this.validCommand);
        assertEquals(outContent.toString().contains(newTestFileName), false);

        file.delete();

    }
    @Test
    public void testRenameDirectory() throws Exception {
        validCommand.connect();
        Method methodRename = validCommand.getClass().getDeclaredMethod("renameRemote", String.class, String.class);
        Method methodListDir = validCommand.getClass().getDeclaredMethod("listCurrentRemoteFiles");
        Method methodCreate = validCommand.getClass().getDeclaredMethod("createRemoteDir", String.class);
        Method methodDelete = validCommand.getClass().getDeclaredMethod("deleteRemoteDirectory", String.class);

        methodCreate.setAccessible(true);
        methodDelete.setAccessible(true);
        methodRename.setAccessible(true);
        methodListDir.setAccessible(true);

        String testDir = "TESTDIRECTORY";
        String newTestDirName = "NEWTESTDIR";

        methodCreate.invoke(this.validCommand, testDir);

        outContent.reset();
        methodListDir.invoke(this.validCommand);
        assertEquals(outContent.toString().contains(testDir), true);

        outContent.reset();
        methodRename.invoke(this.validCommand, testDir, newTestDirName);
        methodListDir.invoke(this.validCommand);
        assertEquals(outContent.toString().contains(newTestDirName), true);

        outContent.reset();
        methodDelete.invoke(this.validCommand, newTestDirName);
        methodListDir.invoke(this.validCommand);
        assertEquals(outContent.toString().contains(newTestDirName), false);

    }

    @Test
    public void testRenameLocalFile() throws Exception {
        validCommand.connect();
        Method methodRename = validCommand.getClass().getDeclaredMethod("renameLocalFile", String.class, String.class);
        methodRename.setAccessible(true);

        String testFileName = "TESTFILENAME";
        String newFileName = "NEWFILENAME";
        File testFile = new File(testFileName);
        testFile.createNewFile();
        assertEquals(testFile.exists(), true);

        methodRename.invoke(this.validCommand, testFileName, newFileName);
        assertEquals(new File(newFileName).exists(), true);
        assertEquals(new File(testFileName).exists(), false);

        new File(newFileName).delete();
    }

    @Test
    public void testListAndChangeLocalDirectory() throws Exception {
        validCommand.connect();
        Method methodChangeLocalDir = validCommand.getClass().getDeclaredMethod("changeCurrentLocalDirectory", String.class);
        Method methodListCurrentDir = validCommand.getClass().getDeclaredMethod("listCurrentLocalDirectory");

        methodChangeLocalDir.setAccessible(true);
        methodListCurrentDir.setAccessible(true);
        String newDirectory = "TESTDIR";
        File file = new File(newDirectory);
        file.mkdir();
        assertEquals(file.exists(), true);

        outContent.reset();

        methodListCurrentDir.invoke(validCommand);
        assertEquals(outContent.toString().contains(newDirectory), false);

        methodChangeLocalDir.invoke(validCommand, newDirectory);

        outContent.reset();
        methodListCurrentDir.invoke(validCommand);

        assertEquals(outContent.toString().contains(newDirectory), true);

        file.delete();
    }

    @Test
    public void testListLocalFiles() throws Exception {
        validCommand.connect();
        Method methodListLocalFiles = validCommand.getClass().getDeclaredMethod("listCurrentLocalDirectoryFiles");
        Method methodListCurrentDir = validCommand.getClass().getDeclaredMethod("listCurrentLocalDirectory");

        methodListLocalFiles.setAccessible(true);
        methodListCurrentDir.setAccessible(true);

        String newFile = "TESTFILE";

        outContent.reset();
        methodListCurrentDir.invoke(validCommand);
        assertEquals(outContent.toString().contains(newFile), false);

        File file = new File(newFile);
        file.createNewFile();
        assertEquals(file.exists(), true);

        outContent.reset();
        methodListLocalFiles.invoke(validCommand);
        assertEquals(outContent.toString().contains(newFile), true);

        file.delete();

        outContent.reset();
        methodListLocalFiles.invoke(validCommand);
        assertEquals(outContent.toString().contains(newFile), false);
    }

    @Test
    public void testSetGetTimeOut() throws Exception {
        validCommand.connect();
        Method methodGetTimeout = validCommand.getClass().getDeclaredMethod("getTimeout");
        Method methodSetTimeout = validCommand.getClass().getDeclaredMethod("setTimeout", int.class);

        int timeout = 10000;

        methodGetTimeout.setAccessible(true);
        methodSetTimeout.setAccessible(true);

        assertEquals(methodGetTimeout.invoke(validCommand), 0);

        methodSetTimeout.invoke(validCommand, timeout);

        methodSetTimeout.invoke(invalidCommand, timeout);

        assertEquals(methodGetTimeout.invoke(validCommand), timeout);


    }



}
