package com.company;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * CommandSFTP Tester.
 *
 * @author <Authors name>
 * @since <pre>Jul 6, 2015</pre>
 * @version 1.0
 */
public class CommandSFTPTest {
   CommandSFTP validCommand;
   CommandSFTP invalidCommand;

   @Before
   public void before() throws Exception {
      //Assumes knownhostfile exists
      //Tests CommandSFTP requires valid password
      validCommand = new CommandSFTP("linux.cecs.pdx.edu", 22, System.getProperty("user.home") + "/.ssh/sftp_hosts");
      invalidCommand = new CommandSFTP();
      validCommand.setUser(new User("gelever", "PASSWORD NOT REAL"));
   }

   @After
   public void after() throws Exception {
   }

   /**
    * Method: isConnected()
    */
   @Test
   public void testIsConnected() throws Exception {
//TODO: Test goes here...
      assertEquals(false, validCommand.isConnected());
      validCommand.connect();
      assertEquals(true, validCommand.isConnected());
      validCommand.quit();
   }

   /**
    * Method: connect(User user)
    */
   @Test
   public void testConnect() throws Exception {
//TODO: Test goes here...

      boolean success = validCommand.connect();
      assertEquals(true, success);
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
}
