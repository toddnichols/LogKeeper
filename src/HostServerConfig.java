
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class HostServerConfig {

    public static String username = "";
    public static String password = "";

    public static void loadConfiguration() {
        if (new File(TalkAbroadLogKeeper.SERVER_CONFIG_FILE).canRead()) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(TalkAbroadLogKeeper.SERVER_CONFIG_FILE));

                username = properties.get("username").toString();
                password = properties.get("password").toString();


            } catch (Exception e) {
                e.getStackTrace();
            }
        } else {
            storeConfiguration();
        }
    }

    public static void storeConfiguration() {
        try {
            new File(TalkAbroadLogKeeper.SERVER_CONFIG_FILE).createNewFile();
            Properties properties = new Properties();
            properties.put("username", username);
            properties.put("password", password);

            properties.store(new FileOutputStream(TalkAbroadLogKeeper.SERVER_CONFIG_FILE),
                    "TalkAbroad LogKeeper Username & Password configuration file");
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public static void SetConfiguration(String Username, String Password) {
        username = Username;
        password = Password;

        storeConfiguration();
    }
}


