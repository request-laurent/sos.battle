package com.sos.ui5.battle.utils.security;

import java.security.MessageDigest;

public class SHA {
  /**
   * Permet d'obtenir une clé de hashage SHA pour crypter les mots de passe
   */
  public static String getHash(String password) throws Exception {
    String hex = "";
    int h = -1;
    MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
    msgDigest.update(password.getBytes());
    byte[] hash = msgDigest.digest();

    for (int i = 0; i < hash.length; i++) {
      h = hash[i] & 0xFF;
      if (h < 16) {
        hex += "0";
      }
      hex += Integer.toString(h, 16).toUpperCase();
    }
    return hex;
  }

}
