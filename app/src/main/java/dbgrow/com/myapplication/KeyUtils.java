package dbgrow.com.myapplication;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtils {
    private Context context;

    public KeyUtils(Context ctx) {
        context = ctx;
    }

    public void generateKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keys = kpg.generateKeyPair();

            String privateKey = "-----BEGIN PRIVATE KEY-----" + Base64.encodeToString(keys.getPrivate().getEncoded(), Base64.DEFAULT) + "-----END PRIVATE KEY-----";
            String publicKey = "-----BEGIN PUBLIC KEY-----" + Base64.encodeToString(keys.getPublic().getEncoded(), Base64.DEFAULT) + "-----END PUBLIC KEY-----";

            Log.i(getClass().getSimpleName(), privateKey);
            Log.i(getClass().getSimpleName(), publicKey);

            FileOutputStream outputStream;

            outputStream = context.openFileOutput("private.pem", Context.MODE_PRIVATE);
            outputStream.write(keys.getPrivate().getEncoded());
            outputStream.close();

            outputStream = context.openFileOutput("public.pem", Context.MODE_PRIVATE);
            outputStream.write(keys.getPublic().getEncoded());
            outputStream.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clearKeys() {
        File file = new File(context.getFilesDir(), "private.pem");
        file.delete();

        file = new File(context.getFilesDir(), "public.pem");
        file.delete();
    }

    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        /*return KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(getPrivateKeyString().getBytes()));*/

        /* Generate private key. */

        File file = new File(context.getFilesDir(), "private.pem");
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();

        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pvt = kf.generatePrivate(ks);
        return pvt;
    }

    public PublicKey getPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        /*return KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(getPrivateKeyString().getBytes()));*/

        /* Generate private key. */

        File file = new File(context.getFilesDir(), "public.pem");
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();

        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pvt = kf.generatePublic(ks);
        return pvt;
    }

    public String getPublicKeyString() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        return ("-----BEGIN PUBLIC KEY-----" + Base64.encodeToString(getPublicKey().getEncoded(), Base64.DEFAULT) + "-----END PUBLIC KEY-----").replaceAll("\r", "").replaceAll("\n", "");
    }

    public String signToHexString(String plainText) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(getPrivateKey());
        privateSignature.update(plainText.getBytes());

        byte[] signature = privateSignature.sign();
        return byteArrayToHexString(signature);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes());

        byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);
        return publicSignature.verify(signatureBytes);
    }

    String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }
}
