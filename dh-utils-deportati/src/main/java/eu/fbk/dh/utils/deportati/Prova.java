package eu.fbk.dh.utils.deportati;

import com.sun.crypto.provider.SunJCE;

import javax.crypto.Cipher;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Base64;
import java.util.stream.BaseStream;

public class Prova {

    public static void main(String[] args) {
        try {
            Security.addProvider(new SunJCE());
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update("LOGIN".getBytes());
            DESKeySpec key = new DESKeySpec(md.digest());
            SecretKeySpec DESKEy = new SecretKeySpec(key.getKey(), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(2, DESKEy);

            String data = new String(cipher.doFinal(Base64.getDecoder().decode("UvTy3qI9euFQ1TKtyegfQA==")));

            System.out.println(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
