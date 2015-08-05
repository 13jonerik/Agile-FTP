package com.company;

import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since July 18, 2015
 * Inspired and Modified from http://blogs.mulesoft.com/dev/mule-dev/making-sftp-testing-portable/
 */
public class SFTPInMemoryServer {
    public static final int PORT = 6969;
    private SshServer sshServer;

    public SFTPInMemoryServer() {
        Security.addProvider(new BouncyCastleProvider());
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(PORT);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("src/test/java/resources/hostkeys"));


        SftpSubsystem.Factory factory = new SftpSubsystem.Factory();
        sshServer.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(factory));
        sshServer.setCommandFactory(new ScpCommandFactory());
        sshServer.setShellFactory(new ProcessShellFactory());
        sshServer.setPasswordAuthenticator(PasswordAuthenticator());
    }

    private PasswordAuthenticator PasswordAuthenticator() {
        return (arg0, arg1, arg2) -> true;
    }

    public void start(){
        try {
            sshServer.start();
        } catch (IOException e) {
            System.err.println("Error Starting In Memory Server");
        }
    }

    public void stop(){
        try {
            sshServer.stop();
        } catch (InterruptedException e) {
            System.err.println("Error Starting In Memory Server");
            sshServer = null;
        }
    }
}

