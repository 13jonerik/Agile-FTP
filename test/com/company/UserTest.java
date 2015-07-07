package com.company;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User test cases
 * @author Stephan Gelever
 * @version 1.0 alpha
 * @since July 5, 2015
 */
public class UserTest {
    private User invalidUser;
    private User validUser;

    @Before
    public void setUp() throws Exception {
        invalidUser = new User();
        validUser = new User("name", "pass");
    }

    @Test
    public void testGetUserName() throws Exception {
        assertEquals ("name", validUser.getUserName());
        assertEquals ("", invalidUser.getUserName());
    }

    @Test
    public void testinValidUser() throws Exception {
        assertEquals (false, invalidUser.validUser());

    }

    @Test
    public void testValidUser() throws Exception {
        assertEquals(true, validUser.validUser());
    }

    @Test
    public void testGetPassphraseIsEmpty() throws Exception {
        assertEquals ("", invalidUser.getPassphrase());
        assertEquals("", validUser.getPassphrase());
    }

    @Test
    public void testGetPassword() throws Exception {
        assertEquals ("pass", validUser.getPassword());
        assertEquals ("", invalidUser.getPassword());

    }
}