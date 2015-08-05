package com.company;

import org.junit.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since July 18, 2015
 */
public class TestCommandSFTP {
    private final static SFTPInMemoryServer server = new SFTPInMemoryServer();
    private final static String hostFile = "src/test/java/resources/sftp_hosts_tests";
    private final static CommandSFTP commandSFTP = new CommandSFTP("localhost", SFTPInMemoryServer.PORT, hostFile);
    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final static ByteArrayOutputStream errContent = new ByteArrayOutputStream();



    @BeforeClass
    public static void beforeClass() throws NoSuchMethodException, NoSuchFieldException {
        server.start();
        commandSFTP.setUser(new User("remote user", "password"));
        commandSFTP.connect();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

    }

    @Before
    public void before() {

    }
    @After
    public void after() {
    }
    @AfterClass
    public static void afterClass() {
        server.stop();
        System.setOut(null);
        System.setErr(null);
    }
    @Test
    public void testConnect() throws Exception {
        assertEquals(commandSFTP.checkConnect(), true);
    }

    @Test
    public void testDisconnect() throws Exception {
        assertEquals(commandSFTP.quit(), true);
        assertEquals(commandSFTP.checkConnect(), false);
        assertEquals(commandSFTP.connect(), true);
    }

    @Test
    public void testIsValid() throws Exception {
        assertEquals(commandSFTP.isValid(), true);
    }

    @Test
    public void testSetKnownHostsFile() throws Exception {
        String fileName = System.getProperty("user.home") + "/testHostsFile.txt";

        Field knownHostsFile = commandSFTP.getClass().getDeclaredField("knownHostsFile");
        knownHostsFile.setAccessible(true);
        commandSFTP.setKnownHostsFile(fileName);
        assertEquals(knownHostsFile.get(commandSFTP), fileName);

        File file = new File(fileName);
        assertEquals(file.exists(), true);
        file.delete();

        commandSFTP.setKnownHostsFile(hostFile);
    }

    @Test
    public void testListRemoteFiles() throws Exception {

        String testFile = "testFileRemote.txt";
        File file = new File(testFile);
        file.createNewFile();
        assertEquals(file.exists(), true);

        commandSFTP.uploadRemoteFile(testFile);
        outContent.reset();
        commandSFTP.listCurrentRemoteFiles();

        assertEquals(outContent.toString().contains(testFile), true);
        assertEquals(outContent.toString().contains("NOTREAL.txt"), false);

        commandSFTP.deleteRemoteFile(testFile);

        file.delete();
    }

    @Test
    public void testUploadGetDeleteRemoteFile() throws Exception {
        String testFileName = "testUploadFile.txt";

        outContent.reset();
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(testFileName), false);

        String testFilePath = "src/test/java/resources";
        String originalPath = "../../../..";

        File file = new File("src/test/java/resources/testUploadFile.txt");
        file.createNewFile();
        assertEquals(file.exists(), true);

        commandSFTP.changeCurrentLocalDirectory(testFilePath);


        outContent.reset();
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(testFileName), false);

        commandSFTP.uploadRemoteFile(testFileName);
        commandSFTP.changeCurrentLocalDirectory(originalPath);

        outContent.reset();
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(testFileName), true);

        new File(testFileName).delete();
        commandSFTP.changeRemoteDirectory(testFilePath);
        commandSFTP.getRemoteFile(testFileName);
        commandSFTP.changeRemoteDirectory(originalPath);
        assertEquals(new File(testFileName).exists(), true);

        commandSFTP.deleteRemoteFile(testFileName);
        outContent.reset();
        assertEquals(outContent.toString().contains(testFileName), false);

        file.delete();
        new File(testFileName).delete();
    }



    @Test
    public void testListAndChangeRemoteDirectory() throws Exception {
        outContent.reset();

        String testDir = "TESTDIRECTORY";
        commandSFTP.createRemoteDir(testDir);
        commandSFTP.changeRemoteDirectory(testDir);

        outContent.reset();
        commandSFTP.listCurrentRemoteDirectory();
        assertEquals(outContent.toString().contains(testDir), true);

        commandSFTP.changeRemoteDirectory("..");
        commandSFTP.deleteRemoteDirectory(testDir);
    }

    @Test
    public void testRenameRemoteFile() throws Exception {
        String testFile = "TESTFILEUPLOAD";
        File file = new File(testFile);
        file.createNewFile();
        assertEquals(file.exists(), true);

        String newTestFileName = "NEWTESTFile";

        commandSFTP.uploadRemoteFile(testFile);

        outContent.reset();
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(testFile), true);

        outContent.reset();
        commandSFTP.renameRemote(testFile, newTestFileName);
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(newTestFileName), true);
        assertEquals(outContent.toString().contains(testFile), false);

        outContent.reset();
        commandSFTP.deleteRemoteFile(newTestFileName);
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(newTestFileName), false);

        file.delete();

    }
    @Test
    public void testRenameDirectory() throws Exception {
        String testDir = "TESTDIRECTORY";
        String newTestDirName = "NEWTESTDIR";

        commandSFTP.createRemoteDir(testDir);

        outContent.reset();
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(testDir), true);

        outContent.reset();
        commandSFTP.renameRemote(testDir, newTestDirName);
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(newTestDirName), true);

        outContent.reset();
        commandSFTP.deleteRemoteDirectory(newTestDirName);
        commandSFTP.listCurrentRemoteFiles();
        assertEquals(outContent.toString().contains(newTestDirName), false);
    }

    @Test
    public void testRenameLocalFile() throws Exception {
        String testFileName = "TESTFILENAME";
        String newFileName = "NEWFILENAME";
        String testFileDir = "src/test/java/resources/";
        File testFile = new File(testFileDir + testFileName);
        testFile.createNewFile();
        assertEquals(testFile.exists(), true);
        commandSFTP.changeCurrentLocalDirectory(testFileDir);
        commandSFTP.renameLocalFile(testFileName, newFileName);
        assertEquals(new File(testFileDir + newFileName).exists(), true);
        assertEquals(new File(testFileDir + testFileName).exists(), false);

        new File(testFileDir + newFileName).delete();
    }

    @Test
    public void testListAndChangeLocalDirectory() throws Exception {
        String newDirectory = "TESTDIR";
        File file = new File(newDirectory);
        file.mkdir();
        assertEquals(file.exists(), true);

        outContent.reset();

        commandSFTP.listCurrentLocalDirectory();
        assertEquals(outContent.toString().contains(newDirectory), false);

        commandSFTP.changeCurrentLocalDirectory(newDirectory);

        outContent.reset();
        commandSFTP.listCurrentLocalDirectory();
        assertEquals(outContent.toString().contains(newDirectory), true);
        commandSFTP.changeCurrentLocalDirectory("..");

        file.delete();
    }

    @Test
    public void testListLocalFiles() throws Exception {
        String newFile = "TESTLOCALFILE";

        outContent.reset();
        commandSFTP.listCurrentLocalDirectoryFiles();
        assertEquals(outContent.toString().contains(newFile), false);

        File file = new File(newFile);
        file.createNewFile();
        assertEquals(file.exists(), true);

        outContent.reset();
        commandSFTP.listCurrentLocalDirectoryFiles();
        assertEquals(outContent.toString().contains(newFile), true);

        file.delete();

        outContent.reset();
        commandSFTP.listCurrentLocalDirectoryFiles();
        assertEquals(outContent.toString().contains(newFile), false);
    }

    @Test
    public void testSetGetTimeOut() throws Exception {
        int timeout = 10000;
        assertEquals(commandSFTP.getTimeout(), 0);
        commandSFTP.setTimeout(timeout);
        assertEquals(commandSFTP.getTimeout(), timeout);
    }

}
