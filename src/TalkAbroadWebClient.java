
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import util.Base64;

/**
 * Client object that talks to the TalkAbroad server. This is used by the main
 * app to send and receive communications with the server. RSA public key
 * encryption is used to authenticate users with the specified email and
 * password, as well AES symmetric encryption for sensitive data transmissions.
 *
 * @author Masahiro Knittel
 *
 */
public class TalkAbroadWebClient {

    private final String publickeyURL = "http://www.talkabroad.org/java/get_public_key.php";
    private final int max_upload_size = 100 * 1024; // max upload size in bytes
    private SecretKeySpec aes_data_key;
    private Cipher aes;
    private Cipher rsa;
    private byte[] aes_iv = null;
    private String auth_email;
    private String auth_password;
    private int file_bytes_written = 0;

    /**
     * Constructs a client that talks to the TalkAbroad server. The public key
     * is obtained from the server via HTTP request. The AES key and IV is
     * initialized and sent with the security token for every request that is
     * sent to the server for symmetric communication.
     *
     * @param email the email address of the user logging in
     * @param password the password of the user logging in
     */
    public TalkAbroadWebClient(String email, String password) {
        this.auth_email = email;
        this.auth_password = password;

        TalkAbroadLogKeeper.debug("Initializing crytographic functionality.");
        try {
            // Begin initializing the cryptographic functions of the app for data transmission

            // Initialize the RSA cipher object
            TalkAbroadLogKeeper.debug("Initializing RSA cipher.");
            rsa = Cipher.getInstance("RSA");

            URL key_url = new URL(publickeyURL);
            BufferedReader input = new BufferedReader(new InputStreamReader(key_url.openStream()));
            TalkAbroadLogKeeper.debug("Reading public key from server...");

            String data = "";
            String publickey = "";

            while ((data = input.readLine()) != null) {
                if (!data.equals("-----BEGIN PUBLIC KEY-----") && !data.equals("-----END PUBLIC KEY-----")) {
                    publickey = publickey.concat(data);
                }
            }
            TalkAbroadLogKeeper.debug("Public key obtained:\n" + publickey);
            TalkAbroadLogKeeper.debug("Constructing RSA cipher from public key.");

            rsa.init(Cipher.ENCRYPT_MODE,
                    KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(
                    Base64.decodeFast(publickey))));

            // Generate an AES key
            TalkAbroadLogKeeper.debug("Generating AES secret key.");
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            aes_data_key = new SecretKeySpec(kg.generateKey().getEncoded(), "AES");

            TalkAbroadLogKeeper.debug("Generated AES data key base64 encoded: " + Base64.encodeToString(aes_data_key.getEncoded(), false));

            TalkAbroadLogKeeper.debug("Initializing AES cipher.");
            aes = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Performing a validation is necessary in order to initialize an IV used by the app.
            // It is also necessary to ensure the symmetric cipher is usable at launch.
            String validationString = "Hello Java!";
            String validationResult = new String(decrypt_aes_data(encrypt_aes_data(validationString.getBytes())));
            TalkAbroadLogKeeper.debug("Result of AES validation: " + validationResult);

            if (!validationResult.equals(validationString)) {
                throw (new Exception("Unable to perform AES self-validation. Result is different from original."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String generate_auth_token() {
        String auth_token = Base64.encodeToString(aes_data_key.getEncoded(), false)
                + ":"
                + Base64.encodeToString(aes_iv, false)
                + ":"
                + auth_email
                + ":"
                + auth_password
                + ":"
                + TalkStrings.currentLanguage().toString();

        TalkAbroadLogKeeper.debug("Assembling auth_token as: " + auth_token);

        try {
            auth_token =
                    Base64.encodeToString(
                    rsa.doFinal(auth_token.getBytes()),
                    false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TalkAbroadLogKeeper.debug("Generated auth token: " + auth_token);
        return auth_token;
    }

    public int get_file_bytes_written() {
        return file_bytes_written;
    }

//        I have to work with the following 4 functions to change the STATUS functionality
    /**
     * Sends a request to the TalkAbroadLogKeeper server.
     *
     * This is an overloaded method for simpler requests with no data to send.
     *
     * @param address URL of the script on the server
     * @return the result of the HTTP POST transaction
     */
    public String send_request(String address) {
        return send_request(address, null);
    }

    /**
     * Sends a request to the TalkAbroadLogKeeper server. The only parameter
     * required to have actual data is the address. The data parameter contains
     * a key:value hash table equivalent to an associative array sent to the
     * server for POST request data.
     *
     * This is an overloaded method for simpler requests that require no file
     * uploads.
     *
     * @param address URL of the script on the server
     * @param data a hashtable of strings containing key:value pairs
     * @return the result of the HTTP POST transaction
     */
    public String send_request(String address, Hashtable<String, String> data) {
        return send_request(address, data, (File) null);
    }

    /**
     * Sends a request to the TalkAbroadLogKeeper server. The only parameter
     * required to have actual data is the address. The data parameter contains
     * a key:value hash table equivalent to an associative array sent to the
     * server for POST request data.
     *
     * This is an overloaded method that automates uploads of files larger than
     * max_upload_size by making multiple send_request calls to the server.
     *
     * @param address URL of the script on the server
     * @param data a hashtable of strings containing key:value pairs
     * @param upload_file a file to upload
     * @return the result of the HTTP POST transaction
     */
    public String send_request(String address, Hashtable<String, String> data, File upload_file) {
        file_bytes_written = 0;
        if (upload_file == null) {
            return send_request(address, data, null, null, -1);
        } else {
            StringBuilder response = new StringBuilder();
            String filename = upload_file.getName();
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                DigestInputStream stream = new DigestInputStream(new FileInputStream(upload_file), md5);
                byte[] buffer = new byte[max_upload_size];
                int i = 0;
                int read_bytes;
                while ((read_bytes = stream.read(buffer)) != -1) {
                    i++;
                    data.put("part_num", Integer.toString(i));
                    String result = send_request(address, data, filename, buffer, read_bytes);
                    TalkAbroadLogKeeper.debug("Received response from server after uploading file part:\n" + result);
                }
                data.put("digest", toHex(md5.digest()));
                data.put("part_num", Integer.toString(-1));
                data.put("parts", Integer.toString(i));
                return send_request(address, data);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return response.toString();
        }
    }

    /**
     * Sends a request to the TalkAbroadLogKeeper server. The only parameter
     * required to have actual data is the address. The data parameter contains
     * a key:value hash table equivalent to an associative array sent to the
     * server for POST request data. File uploads are accomplished by sending
     * chunks of data up to a maximum upload size as set in the private class
     * property max_upload_size. For files larger than this size, multiple calls
     * to this method are necessary. This is automated by calling this method
     * with the overloaded method with the (String, Hashtable, File) signature.
     * This method is private and as such, the overloaded methods must be used
     * instead.
     *
     * @param address URL of the script on the server
     * @param data a hashtable of strings containing key:value pairs
     * @param filename name of a file to upload
     * @param chunk byte array containing the file data
     * @param length number of bytes in the byte array containing actual data
     * @return the result of the HTTP POST transaction
     */
    private String send_request(String address, Hashtable<String, String> data, String filename, byte[] chunk, int length) {
        String response = null;
        int buffer_size = 10240;
        try {
            if (data == null) {
                data = new Hashtable<String, String>();
            }
            data.put("token", generate_auth_token());

            StringBuilder boundary = new StringBuilder();
            boundary.append("--");

            // Generate random boundary string for the request
            Random random = new Random();
            final String boundary_chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            for (int i = 0; i < 32; i++) {
                boundary.append(boundary_chars.charAt(random.nextInt(boundary_chars.length())));
            }

            // Initialize the request data builder
            StringBuilder request_data = new StringBuilder();

            // Loop through each of the data keys and append to request as form data
            Enumeration<String> keys = data.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = (String) data.get(key);

                request_data.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n")
                        .append("\r\n")
                        .append(value).append("\r\n");
            }

            TalkAbroadLogKeeper.debug("Opening connection.");
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            TalkAbroadLogKeeper.debug("Constructed POST request (before file data):\n" + request_data.toString());

            TalkAbroadLogKeeper.debug("Sending request.");

            OutputStreamWriter connection_writer = new OutputStreamWriter(connection.getOutputStream());
            connection_writer.write(request_data.toString());

            if (filename != null) {
                request_data = new StringBuilder();
                if (length != -1) {
                    request_data.append("--").append(boundary).append("\r\n")
                            .append("Content-Disposition: attachment; name=\"file\"; filename=\"").append(filename).append(".b64\"\r\n")
                            .append("Content-Transfer-Encoding: base64").append("\r\n")
                            .append("\r\n");

                    connection_writer.write(request_data.toString());

                    TalkAbroadLogKeeper.debug("Sending file data with byte array length " + length);
                    ByteArrayInputStream file_stream = new ByteArrayInputStream(chunk);

                    int bytes_left = length;

                    // While there are still bytes left to read
                    while (bytes_left > 0) {
                        // TalkAbroadLogKeeper.debug("Bytes remaining to encode " + bytes_left);

                        // Calculate how many more to read, the lesser of the
                        // buffer size or the remaining bytes
                        int bytes_to_read = Math.min(bytes_left, buffer_size);
                        byte[] buf = new byte[bytes_to_read];
                        // Read bytes into the buffer 
                        int read_bytes = file_stream.read(buf, 0, bytes_to_read);
                        connection_writer.write(Base64.encodeToChar(buf, true));
                        connection_writer.write("\r\n");
                        bytes_left -= read_bytes;
                        file_bytes_written += read_bytes;
                    }
                }
            }
            connection_writer.write("--" + boundary + "--\r\n\r\n");
            connection_writer.flush();

            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            response = "";
            char[] cbuf = new char[buffer_size];
            int result_length = 0;

            // Concatenate only as much information as was actually read.
            while ((result_length = input.read(cbuf)) != -1) {
                response = response.concat(new String(cbuf).substring(0, result_length));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return response.trim();
    }

    /**
     * Encrypts data using the object's AES key/IV.
     *
     * @param data clear byte array to encrypt
     * @return byte array of encrypted data
     */
    public byte[] encrypt_aes_data(byte[] data) {
        try {
            // First, check and see if an IV has already been created.
            // If not, let the JCE system generate one and store the result in the class.
            if (aes_iv == null) {
                aes.init(Cipher.ENCRYPT_MODE, aes_data_key);
                aes_iv = aes.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            } else {
                aes.init(Cipher.ENCRYPT_MODE, aes_data_key, new IvParameterSpec(aes_iv));
            }
            return aes.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypts data encrypted with this object's AES key/IV.
     *
     * @param data crypt byte array to decrypt
     * @return byte array of decrypted data
     */
    public byte[] decrypt_aes_data(byte[] data) {
        try {
            aes.init(Cipher.DECRYPT_MODE, aes_data_key, new IvParameterSpec(aes_iv));
            return aes.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts a byte array into a String representing the hexadecimal
     * equivalent of the data. Each byte is represented as two hexadecimal
     * characters in lower case.
     *
     * @param buf byte array to convert to hexadecimal
     * @return hexadecimal representation of the byte array
     */
    private static String toHex(byte[] buf) {
        char[] HEX_CHARS = "0123456789abcdef".toCharArray();
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }
}


