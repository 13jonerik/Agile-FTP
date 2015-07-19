package com.company;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;


/**
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since July 18, 2015
 */
public class CommandMockTests {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private PrintStream original = System.out;
    private Method method;
    private CommandSFTP validCommand;
    private CommandSFTP invalidCommand;
    private String host = "linux.cecs.pdx.edu";
    private String user = "USER";
    private String password = "PASSWORD";


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
//TODO: Test goes here...
        assertEquals(false, validCommand.isConnected());
    }

    /**
     * Method: isConnected()
     */
    @Test
    public void testIsConnected() throws Exception {
//TODO: Test goes here...
        validCommand.connect();
        assertEquals(true, validCommand.isConnected());
    }

    /**
     * Method: connect(User user)
     */
    @Test
    public void testConnect() throws Exception {
//TODO: Test goes here...

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
//TODO: Test goes here...

        validCommand.connect();
        boolean success = validCommand.quit();

        assertEquals(true, success);
    }

    /**
     * Method: isValid()
     */
    @Test
    public void testIsValid() throws Exception {
//TODO: Test goes here...
        assertEquals(true, validCommand.isValid());
        assertEquals(false, invalidCommand.isValid());
    }

    @Test
    public void testCheckConnected() throws Exception {
        method = validCommand.getClass().getDeclaredMethod("checkConnected");
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
        Boolean result;
        String fileName = System.getProperty("user.home") + "/testHostsFile.txt";
        result = (Boolean) method.invoke(validCommand, fileName);
        assertEquals(result, true);

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

        result = (Boolean) method.invoke(validCommand, "key", fileName);

        assertEquals(result, true);

        File file = new File(fileName);
        boolean exists = file.exists();
        assertEquals(exists, true);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String addedToHost = br.readLine();
        assertEquals(addedToHost, this.host + " ssh-rsa " + "key");
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
        File file = new File(System.getProperty("user.home") + "/" + testFile);
        file.createNewFile();
        boolean exists = file.exists();
        assertEquals(exists, true);

        methodUpload.invoke(validCommand, testFile);
        methodList.invoke(validCommand);

        assertEquals(outContent.toString().contains(testFile), true);
        assertEquals(outContent.toString().contains("NOTREAL.txt"), false);

        methodDelete.invoke(validCommand, testFile);


    }

    @Test
    @SuppressWarnings("SftpException")
    public void testSetTimeout() throws Exception {
        validCommand.connect();
        method = validCommand.getClass().getDeclaredMethod("setTimeout", int.class);
        method.setAccessible(true);

        method.invoke(validCommand, 100000);
        assertEquals(outContent.toString().contains("Unable"), false);
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
        File file = new File(System.getProperty("user.home") + "/" + testFile);
        file.createNewFile();
        boolean exists = file.exists();
        assertEquals(exists, true);

        methodUpload.invoke(validCommand, testFile);
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
    public void testChangeRemoteDirectory() throws Exception {
        validCommand.connect();
        Method methodChangeDir = validCommand.getClass().getDeclaredMethod("changeRemoteDirectory", String.class);
        Method methodListDir = validCommand.getClass().getDeclaredMethod("listCurrentRemoteDirectory");
        Method methodCreate = validCommand.getClass().getDeclaredMethod("createRemoteDir", String.class);
        Method methodDelete = validCommand.getClass().getDeclaredMethod("deleteRemoteDirectory", String.class);

        methodChangeDir.setAccessible(true);
        methodListDir.setAccessible(true);
        methodCreate.setAccessible(true);
        methodDelete.setAccessible(true);

        outContent.reset();

        String testDir = "TESTDIRECTORY";
        methodCreate.invoke(this.validCommand, testDir);
        methodChangeDir.invoke(this.validCommand, testDir);
        methodListDir.invoke(this.validCommand);

        assertEquals(outContent.toString().contains(testDir), true);

        methodChangeDir.invoke(this.validCommand, "..");
        methodDelete.invoke(this.validCommand, testDir);

    }


}
