
import java.io.*;
import java.net.URL;
import java.util.*;

public final class TalkStrings {

    public static final Language defaultLanguage = Language.ENGLISH;
    private static Language selectedLanguage = defaultLanguage; // default language
    private static final Hashtable<String, String> languageStrings = new Hashtable<String, String>();
    private static final Hashtable<String, String> configStrings = new Hashtable<String, String>();

    public static enum Language {

        ENGLISH("en"),
        SPANISH("sp");
        private String stringVal;

        Language(String stringVal) {
            this.stringVal = stringVal;
        }

        public String toString() {
            return this.stringVal;
        }

        public boolean equals(String stringVal) {
            return this.stringVal.equals(stringVal);
        }

        public static Language getLanguage(String stringVal) {
            if (stringVal != null) {
                if (ENGLISH.equals(stringVal)) {
                    return ENGLISH;
                } else if (SPANISH.equals(stringVal)) {
                    return SPANISH;
                }
            }

            return null;
        }
    }

    public static enum Message {

        AUTH_FAILED("MESSAGE_AUTH_FAILED"),
        EXIT_CONFIRM("MESSAGE_EXIT_CONFIRM"),
        CALL_CONFIRM("MESSAGE_CALL_CONFIRM"),
        UPLOAD_CONFIRM("MESSAGE_UPLOAD_CONFIRM"),
        SKYPE_NOT_CONNECTED("MESSAGE_SKYPE_NOT_CONNECTED"),
        SKYPE_ERROR("MESSAGE_SKYPE_ERROR"),
        SKYPE_NOT_RUNNING("MESSAGE_SKYPE_NOT_RUNNING"),
        EXISTING_MP3("MESSAGE_EXISTING_MP3"),
        OLD_VERSION("MESSAGE_OLD_VERSION"),
        DELETE_OLD_RECS("MESSAGE_DELETE_OLD_RECS");
        private String stringVal;

        Message(String stringVal) {
            this.stringVal = stringVal;
        }

        public String toString() {
            return this.stringVal;
        }
    }

    public static enum Button {

        EXIT("BUTTON_EXIT"),
        LOGIN("BUTTON_LOGIN"),
        CALL("BUTTON_CALL"),
        REFRESH("BUTTON_REFRESH"),
        HANGUP("BUTTON_HANGUP"),
        UPLOAD("BUTTON_UPLOAD"),
        CANCEL("BUTTON_CANCEL"),
        CONTINUE("BUTTON_CONTINUE");
        private String stringVal;

        Button(String stringVal) {
            this.stringVal = stringVal;
        }

        public String toString() {
            return this.stringVal;
        }
    }

    public static enum Label {

        EMAIL("LABEL_EMAIL"),
        PASSWORD("LABEL_PASSWORD"),
        WELCOME("LABEL_WELCOME"),
        ERROR_OCCURRED("LABEL_ERROR_OCCURRED"),
        CONVERTING_MP3("LABEL_CONVERTING_MP3"),
        BYTES_CONVERTED("LABEL_BYTES_CONVERTED"),
        BYTES_UPLOADED("LABEL_BYTES_UPLOADED"),
        FINALIZING_UPLOAD("LABEL_FINALIZING_UPLOAD"),
        CALL_STATUS("LABEL_CALL_STATUS");
        private String stringVal;

        Label(String stringVal) {
            this.stringVal = stringVal;
        }

        public String toString() {
            return this.stringVal;
        }
    }

    public static enum CallStatus {

        ROUTING("CALLSTATUS_ROUTING"),
        RINGING("CALLSTATUS_RINGING"),
        CONNECTED("CALLSTATUS_CONNECTED"),
        FAILED("CALLSTATUS_FAILED"),
        FINISHED("CALLSTATUS_FINISHED"),
        CANCELLED("CALLSTATUS_CANCELLED"),
        BUSY("CALLSTATUS_BUSY"),
        REFUSED("CALLSTATUS_REFUSED"),
        MISSED("CALLSTATUS_MISSED"),
        VM("CALLSTATUS_VM");
        private String stringVal;

        CallStatus(String stringVal) {
            this.stringVal = stringVal;
        }

        public String toString() {
            return this.stringVal;
        }
    }

    public static enum TableHeader {

        NAME("TABLEHEADER_NAME"),
        TIME("TABLEHEADER_TIME"),
        STATUS("TABLEHEADER_STATUS");
        private String stringVal;

        TableHeader(String stringVal) {
            this.stringVal = stringVal;
        }

        public String toString() {
            return this.stringVal;
        }
    }

    public static String get(Message label) {
        return get(label.toString());
    }

    public static String get(Button label) {
        return get(label.toString());
    }

    public static String get(Label label) {
        return get(label.toString());
    }

    public static String get(CallStatus label) {
        return get(label.toString());
    }

    public static String get(TableHeader label) {
        return get(label.toString());
    }

    public static String get(String label) {
        return languageStrings.get(label);
    }

    public static String getConfig(String label) {
        return configStrings.get(label);
    }

    public static void selectLanguage(Language selection)
            throws FileNotFoundException, IOException {
        if (selection != null) {
            selectedLanguage = selection;
        }
        loadLanguageFiles();
    }

    public static void loadConfigFile()
            throws FileNotFoundException, IOException {

        InputStream config_file_stream;
        File config_file = new File("config.txt");
        if (config_file.exists()) {
            config_file_stream = new FileInputStream(new File("config.txt"));
        } else {
            config_file_stream = TalkAbroadLogKeeper.class.getResourceAsStream("config.txt");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(config_file_stream, "UTF8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("::");
            configStrings.put(tokens[0], tokens[1]);
            TalkAbroadLogKeeper.debug("Config value set: " + tokens[0] + " = " + tokens[1]);
        }
    }

    private static void loadLanguageFiles()
            throws FileNotFoundException, IOException {
        TalkAbroadLogKeeper.debug("Selected language: " + selectedLanguage);

        InputStream language_file_stream = TalkAbroadLogKeeper.class.getResourceAsStream("languages/" + selectedLanguage + ".txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(language_file_stream, "UTF8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\t");
            languageStrings.put(tokens[0], tokens[1]);
        }
    }

    public static Language currentLanguage() {
        return selectedLanguage;
    }
}


