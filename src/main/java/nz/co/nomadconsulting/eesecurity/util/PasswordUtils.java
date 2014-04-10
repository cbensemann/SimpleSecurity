package nz.co.nomadconsulting.eesecurity.util;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class PasswordUtils {

    private int saltLength = 32;

    private String hashAlgorithm = "PBKDF2WithHmacSHA1";


    public PasswordUtils() {
    }


    public PasswordUtils(final String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }


    public byte[] generateRandomSalt() {
        final byte[] salt = new byte[saltLength];
        new SecureRandom().nextBytes(salt);

        return salt;
    }


    public String createPasswordKey(final char[] password, final byte[] salt,
            final int iterations) throws GeneralSecurityException {
        final PBEKeySpec passwordKeySpec = new PBEKeySpec(password, salt,
                iterations, 256);
        final SecretKeyFactory secretKeyFactory = SecretKeyFactory
                .getInstance(hashAlgorithm);
        final SecretKey passwordKey = secretKeyFactory
                .generateSecret(passwordKeySpec);
        passwordKeySpec.clearPassword();

        return BinTools.bin2hex(passwordKey.getEncoded());
    }
}
