package fr.antoma.spaceport_manager.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Hachage des mots de passe en SHA-256 avec un sel aléatoire, pour ne jamais
 * les stocker en clair.
 */
final class PasswordHasher {

  private static final SecureRandom RANDOM = new SecureRandom();

  private PasswordHasher() {
  }

  static String newSalt() {
    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);
    return HexFormat.of().formatHex(salt);
  }

  static String hash(String salt, char[] password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(salt.getBytes(StandardCharsets.UTF_8));
      digest.update(new String(password).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Compare deux empreintes en temps constant. */
  static boolean matches(String expectedHash, String actualHash) {
    return MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8),
        actualHash.getBytes(StandardCharsets.UTF_8));
  }
}
