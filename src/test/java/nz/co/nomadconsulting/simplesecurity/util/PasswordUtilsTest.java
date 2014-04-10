package nz.co.nomadconsulting.simplesecurity.util;

import nz.co.nomadconsulting.simplesecurity.util.BinTools;
import nz.co.nomadconsulting.simplesecurity.util.PasswordUtils;

import org.junit.Before;
import org.junit.Test;


public class PasswordUtilsTest {

    private PasswordUtils passwordUtils;
    
    @Before
    public void setup() {
        passwordUtils = new PasswordUtils();
    }
    
    @Test
    public void testGeneratePassword() throws Exception {
        byte[] generateRandomSalt = passwordUtils.generateRandomSalt();
        String password = passwordUtils.createPasswordKey("v0yager1".toCharArray(), generateRandomSalt, 1024);
        System.out.println("password:" + password + " hash:" + BinTools.bin2hex(generateRandomSalt) + " " + password.length() + " " + BinTools.bin2hex(generateRandomSalt).length());
    }
}
