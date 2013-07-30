
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.imageio.ImageIO;

import util.Base64;

import javax.sound.sampled.*;

import org.tritonus.share.sampled.AudioFileTypes;
import org.tritonus.share.sampled.Encodings;

import com.skype.*;

@SuppressWarnings("serial")
public class TalkAbroadLogKeeper extends javax.swing.JFrame {
    // version string: 100's = major rev, 10's = minor rev, 1's = build

    private static int VERSION = 120;
    public static String SERVER_CONFIG_FILE = FileUtility.getCurrentDirectory() + "server.config";
    ;
        
	
	//private static boolean debug_mode = true;
	
//	//Set Look & Feel
//	{
//		try {
//			javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	private String splashImageFileName = "images/splash.jpg";
    private String bannerImageFileName = "images/banner.jpg";
    /*
     private String host					= "http://www.talkabroad.org";
     private String authenticationURL	= host + "/java/get_partner_info.php";
     private String uploadURL			= host + "/java/send_file.php";
     private String logURL				= host + "/java/log_entry.php";
     */
    private String partnerName;
//        The following two objects are the main two objects.
    private TalkAbroadWebClient web_client = null;
    private ArrayList<TALKAppointment> appointments = new ArrayList<TALKAppointment>();
    private TALKAppointment selectedAppointment = null;
    private String email;
    private String password;
    private boolean isLoggedIn;
    private Call currentcall = null;
    private Call.Status currentstatus = null;
    private boolean call_inprogress = false;
    private boolean call_connected = false;
    private File appointee_recording = null;
    private File mic_recording = null;
    private Vector<File> appointee_recordings = null;
    private Vector<File> mic_recordings = null;
    private String recording_basedir = "recordings";
    private String recording_dir = null;
    private JButton exitButton;
    private JButton loginButton;
    private JPanel mainPanel;
    private JSplitPane appSplitPane;
    private JScrollPane appointmentScrollPanel;
    private JTable appointmentTable;
    private JPanel appointmentControlPanel;
    private JPanel loginPanel;
    private JTextField emailInput;
    private JPasswordField passwordInput;
    private JLabel passwordInputLabel;
    private JLabel emailInputLabel;
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    private JButton callButton;
    private JButton refreshButton;
    private JButton uploadButton;
    private JButton hangupButton;
    private JPanel topPanel;
    private JProgressBar progressBar;
    private BufferedImage splashImage;
    private BufferedImage bannerImage;
    private JLabel showDescription;
    private javax.swing.JScrollPane assignmentDescScrollPane;
    private javax.swing.JTextPane assignmentDescTextPane;
//         private javax.swing.JTextArea assignmentDescTextPane;

    /**
     * Auto-generated main method to display this JFrame
     */
    public static void main(String[] args) {


//            //  static {
//                try {
//                    System.load("/System/Library/Java/Extensions/libskype.jnilib");
////                    /Users/parthobiswas/skype_jnilib/libskype.jnilib
//                } catch (UnsatisfiedLinkError e) {
//                System.err.println("Native code library failed to load.\n" + e);
//                System.exit(1);
//                }
//            //  }


        try {
//                        UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
        }



        try {
            TalkStrings.loadConfigFile();

//                        Loades the username and passwords on the textfields
            HostServerConfig.loadConfiguration();

            TalkStrings.Language lang;
            if (args.length > 0) {
                lang = TalkStrings.Language.getLanguage(args[0]);
            } else {
                lang = TalkStrings.defaultLanguage;
            }
            TalkStrings.selectLanguage(lang);

        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                TalkAbroadLogKeeper inst = new TalkAbroadLogKeeper();
                inst.setLocationRelativeTo(null);
                inst.setVisible(true);

            }
        });
    }

    /**
     * Constructor. Calls the JFrame constructor and initializes GUI. Sets log
     * in status to false.
     */
    public TalkAbroadLogKeeper() {
        super();
        setIconImage(Toolkit.getDefaultToolkit().getImage("logo/TALK_Icon16.png"));
        debug("Launching application. Initializing GUI.");
        debug("Main app thread name: " + Thread.currentThread().getName());
        isLoggedIn = false;
        initGUI();
        checkSkypeRunning();
    }

////////////////////////////////////////////////////////////////////   Partho, you need to check this method     
    /**
     * Initialize the graphical user interface and all associated resources.
     * Banner and splash images are loaded here.
     */
    private void initGUI() {

        try {

            splashImage = ImageIO.read(getClass().getResource(splashImageFileName));
            bannerImage = ImageIO.read(getClass().getResource(bannerImageFileName));
            {
                getContentPane().setLayout(null);
                this.setPreferredSize(new java.awt.Dimension(640, 500));
                this.setSize(640, 500);
                this.setResizable(false);
                this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                this.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        clickCloseButton();
                    }
                });

                {
                    topPanel = new JPanel();
                    getContentPane().add(topPanel);
                    topPanel.setBounds(0, 0, 640, 50);
                    topPanel.setSize(640, 50);
                    topPanel.setPreferredSize(new java.awt.Dimension(640, 50));
                    topPanel.setLayout(null);
                    {
                        loginPanel = new JPanel();
                        topPanel.add(loginPanel);
                        loginPanel.setLayout(null);
                        loginPanel.setBounds(0, 0, 640, 50);
                        loginPanel.setLayout(null);
                        {
                            emailInputLabel = new JLabel();
                            loginPanel.add(emailInputLabel);
                            emailInputLabel.setText(TalkStrings.get(TalkStrings.Label.EMAIL));
                            emailInputLabel.setBounds(6, 12, 109, 15);
                        }
                        {
                            passwordInputLabel = new JLabel();
                            loginPanel.add(passwordInputLabel);
                            passwordInputLabel.setText(TalkStrings.get(TalkStrings.Label.PASSWORD));
                            passwordInputLabel.setBounds(270, 12, 68, 15);
                        }
                        {
                            emailInput = new JTextField();

//                                                        loades the last given username for successfully logging in. 
                            emailInput.setText(HostServerConfig.username);

                            loginPanel.add(emailInput);
                            emailInput.setBounds(115, 6, 142, 27);
                        }
                        {
                            passwordInput = new JPasswordField();

//                                                        loades the last given passwords for successfully logging in. 
                            passwordInput.setText(HostServerConfig.password);

                            loginPanel.add(passwordInput);
                            passwordInput.setBounds(345, 6, 110, 27);
                        }
                        {
                            loginButton = new JButton();
                            loginPanel.add(loginButton);
                            loginButton.setText(TalkStrings.get(TalkStrings.Button.LOGIN));
                            loginButton.setBounds(461, 6, 85, 27);
                            loginButton.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(MouseEvent e) {


                                    email = emailInput.getText();
                                    password = new String(passwordInput.getPassword());

                                    HostServerConfig.SetConfiguration(email, password);



                                    clickLogInButton();


//                                                                        Initate only one window. 
//                                                                        clickLogInButton();
                                }
                            });
                        }
                        {
                            exitButton = new JButton();
                            loginPanel.add(exitButton);
                            exitButton.setText(TalkStrings.get(TalkStrings.Button.EXIT));
                            exitButton.setBounds(552, 6, 82, 27);
                            exitButton.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(MouseEvent e) {
                                    clickExitButton();
                                }
                            });
                        }
                    }
                }


                {
                    mainPanel = new JPanel();
                    getContentPane().add(mainPanel);
                    mainPanel.setBounds(0, 50, 640, 430);
                    mainPanel.setPreferredSize(new java.awt.Dimension(640, 430));
                    mainPanel.setLayout(null);
                    {
                        appSplitPane = new JSplitPane();
                        appSplitPane.setVisible(false);
                        mainPanel.add(appSplitPane);
                        appSplitPane.setBounds(0, 0, 640, 430);
                        appSplitPane.setPreferredSize(new java.awt.Dimension(640, 430));
                        appSplitPane.setSize(640, 430);
                        appSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                        {
                            appointmentScrollPanel = new JScrollPane();
                            appSplitPane.add(appointmentScrollPanel, JSplitPane.LEFT);
                            appointmentScrollPanel.setPreferredSize(new Dimension(640, 100));
                            appointmentScrollPanel.setMinimumSize(new Dimension(640, 100));



                            createAndAddAppointmentTable();

//////////                            {
//////////                                AbstractTableModel appointmentTableModel = new AbstractTableModel() {
//////////                                    public int getColumnCount() {
//////////                                        return 3;
//////////                                    }
//////////
//////////                                    public int getRowCount() {
//////////                                        return appointments.size();
//////////                                    }
//////////
//////////                                    public Object getValueAt(int rowIndex,
//////////                                            int columnIndex) {
//////////                                        String retval = "";
//////////                                        switch (columnIndex) {
//////////                                            case 0:
//////////                                                retval = appointments.get(rowIndex).getFullName();
//////////                                                break;
//////////                                            case 1:
//////////                                                retval = DateFormat.getInstance().format(
//////////                                                        appointments.get(rowIndex).getTime());
//////////                                                break;
//////////                                            case 2:
//////////                                                retval = appointments.get(rowIndex).getStatus();
//////////                                                break;
//////////                                        }
//////////                                        return retval;
//////////                                    }
//////////
//////////                                    public String getColumnName(int columnIndex) {
//////////                                        String retval = "";
//////////                                        switch (columnIndex) {
//////////                                            case 0:
//////////                                                retval = TalkStrings.get(TalkStrings.TableHeader.NAME);
//////////                                                break;
//////////                                            case 1:
//////////                                                retval = TalkStrings.get(TalkStrings.TableHeader.TIME);
//////////                                                break;
//////////                                            case 2:
//////////                                                retval = TalkStrings.get(TalkStrings.TableHeader.STATUS);
//////////                                                break;
//////////                                        }
//////////                                        return retval;
//////////                                    }
//////////                                };
//////////
//////////                                appointmentTable = new JTable();
//////////                                appointmentScrollPanel.setViewportView(appointmentTable);
//////////                                appointmentTable.setModel(appointmentTableModel);
//////////                                appointmentTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//////////                                appointmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//////////                                    public void valueChanged(ListSelectionEvent e) {
//////////                                        clickAppointmentTable();
//////////                                    }
//////////                                });
//////////
//////////                                appointmentTable.setRowSelectionAllowed(true);
//////////                                appointmentTable.setColumnSelectionAllowed(false);
//////////
//////////                            }
//////////







                        }

                        {
                            appointmentControlPanel = new JPanel();
                            appSplitPane.add(appointmentControlPanel, JSplitPane.RIGHT);
                            appointmentControlPanel.setPreferredSize(new Dimension(640, 200));
                            appointmentControlPanel.setLayout(null);
                            appointmentControlPanel.setMinimumSize(new Dimension(640, 150));
                            {
                                welcomeLabel = new JLabel();
                                statusLabel = new JLabel();
                                callButton = new JButton();

                                refreshButton = new JButton();

                                uploadButton = new JButton();
                                hangupButton = new JButton();
                                progressBar = new JProgressBar(0, 100);

                                welcomeLabel.setText(TalkStrings.get(TalkStrings.Label.WELCOME));
                                welcomeLabel.setBounds(12, 12, 284, 14);


                                showDescription = new JLabel("Assignment description shows here.");
                                showDescription.setBounds(300, 12, 250, 14);
                                showDescription.setVisible(true);
                                assignmentDescScrollPane = new javax.swing.JScrollPane();

//                                                                the following 2 line makes the assignment box bigghr.
//                                                                Dimension assignmentDescScrollPaneDimension = new Dimension(250, 800);
//                                                                assignmentDescScrollPane.setMaximumSize(assignmentDescScrollPaneDimension);

                                assignmentDescTextPane = new javax.swing.JTextPane();
//                                                                assignmentDescTextPane = new JTextArea();

                                assignmentDescTextPane.setEditable(false);
                                assignmentDescTextPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Assignment"));
                                assignmentDescTextPane.setForeground(new java.awt.Color(0, 102, 102));
                                assignmentDescTextPane.setText("Description shows here");
                                assignmentDescTextPane.setToolTipText("Assignment descriotion...");
                                assignmentDescScrollPane.setViewportView(assignmentDescTextPane);
                                assignmentDescScrollPane.setBounds(250, 12, 375, 120);


                                statusLabel.setText("");
                                statusLabel.setBounds(210, 43, 284, 14);

                                progressBar.setBounds(210, 63, 284, 14);
                                progressBar.setValue(0);
                                progressBar.setVisible(false);

                                callButton.setText(TalkStrings.get(TalkStrings.Button.CALL));
                                callButton.setBounds(12, 38, 88, 25);
                                callButton.addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent evt) {
                                        clickCallButton();
                                    }
                                });


                                refreshButton.setText(TalkStrings.get(TalkStrings.Button.REFRESH));
                                refreshButton.setBounds(63, 80, 88, 25);
                                refreshButton.addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent evt) {


                                        clickLogInButton(); // Refreshes the appointment list after clicking refresh button.


//                                        Creates the Appointment JTable newly and shows the new informations on it.
                                        createAndAddAppointmentTable();

                                    }
                                });



                                // This button is not added to the main frame
                                hangupButton.setText(TalkStrings.get(TalkStrings.Button.HANGUP));
                                hangupButton.addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent evt) {
                                        clickHangupButton();
                                    }
                                });

                                uploadButton.setText(TalkStrings.get(TalkStrings.Button.UPLOAD));
                                uploadButton.setBounds(111, 38, 88, 25);
                                uploadButton.addMouseListener(new MouseAdapter() {
                                    public void mouseClicked(MouseEvent evt) {
                                        clickUploadButton();
                                    }
                                });

                                appointmentControlPanel.add(welcomeLabel);
                                appointmentControlPanel.add(statusLabel);
                                appointmentControlPanel.add(callButton);

                                appointmentControlPanel.add(refreshButton);

                                appointmentControlPanel.add(uploadButton);
                                appointmentControlPanel.add(progressBar);
//                                                                appointmentControlPanel.setBackground(Color.red);

                                // Initially disable the call and upload button
                                // until an appointment is selected
                                callButton.setEnabled(false);

                                refreshButton.setEnabled(true);

                                uploadButton.setEnabled(false);


//                                                                Here i have to add a text pane which shows the description of the table                                                              
//                                                                appointmentControlPanel.add(showDescription);                                                               
                                appointmentControlPanel.add(assignmentDescScrollPane);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(ERROR);
        }

    }

    public void createAndAddAppointmentTable() {
        AbstractTableModel appointmentTableModel = new AbstractTableModel() {
            public int getColumnCount() {
                return 3;
            }

            public int getRowCount() {
                return appointments.size();
            }

            public Object getValueAt(int rowIndex,
                    int columnIndex) {
                String retval = "";
                switch (columnIndex) {
                    case 0:
                        retval = appointments.get(rowIndex).getFullName();
                        break;
                    case 1:
                        retval = DateFormat.getInstance().format(
                                appointments.get(rowIndex).getTime());
                        break;
                    case 2:
                        retval = appointments.get(rowIndex).getStatus();
                        break;
                }
                return retval;
            }

            public String getColumnName(int columnIndex) {
                String retval = "";
                switch (columnIndex) {
                    case 0:
                        retval = TalkStrings.get(TalkStrings.TableHeader.NAME);
                        break;
                    case 1:
                        retval = TalkStrings.get(TalkStrings.TableHeader.TIME);
                        break;
                    case 2:
                        retval = TalkStrings.get(TalkStrings.TableHeader.STATUS);
                        break;
                }
                return retval;
            }
        };

        appointmentTable = new JTable();
        appointmentScrollPanel.setViewportView(appointmentTable);
        appointmentTable.setModel(appointmentTableModel);
        appointmentTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                clickAppointmentTable();
            }
        });

        appointmentTable.setRowSelectionAllowed(true);
        appointmentTable.setColumnSelectionAllowed(false);

    }

    private void checkSkypeRunning() {
        int max_tries = 50;
        int sleep_interval = 100;
        int count = 0;
        SkypeMonitor monitor = new SkypeMonitor();
        new Thread(monitor).start();

        debug("Checking to see if Skype is running...");
        try {
            while (!monitor.isRunning()) {
                while (!monitor.isRunning() && count < max_tries) {
                    debug("Skype not running. Sleeping for " + sleep_interval + "ms (count=" + count + ")");
                    Thread.sleep(sleep_interval);
                    count++;
                }
                if (!monitor.isRunning()) {
                    displaySkypeNotRunning();
                }
                count = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    ////////////////////////////////////////////////////////////////////   Partho, you need to check this method         
    public void clickCallButton() {
        if (!callButton.isEnabled()) {
            return;
        }

        debug("Call button clicked.");
        StringBuilder message = new StringBuilder();
        message.append(TalkStrings.get(TalkStrings.Message.CALL_CONFIRM)).append(selectedAppointment.getFullName()).append("?");

        Object[] buttons = {
            TalkStrings.get(TalkStrings.Button.CALL),
            TalkStrings.get(TalkStrings.Button.CANCEL)
        };
        int n = JOptionPane.showOptionDialog(this,
                message.toString(),
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, //do not use a custom Icon
                buttons, //the titles of buttons
                buttons[0]); //default button title
        if (n == JOptionPane.YES_OPTION) {
            callAppointee();
        }

    }

    public void clickHangupButton() {
        try {
            currentcall.finish();
        } catch (Exception e) {
            displayError(e);
        }
    }

    public void clickUploadButton() {
        if (!uploadButton.isEnabled()) {
            return;
        }

        Object[] buttons = {
            TalkStrings.get(TalkStrings.Button.UPLOAD),
            TalkStrings.get(TalkStrings.Button.CANCEL)};
        int n = JOptionPane.showOptionDialog(this,
                TalkStrings.get(TalkStrings.Message.UPLOAD_CONFIRM),
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, //do not use a custom Icon
                buttons, //the titles of buttons
                buttons[0]); //default button title
        if (n == JOptionPane.YES_OPTION) {
            uploadConversation();
        }
    }

    /**
     * Activates when the window's default close button is clicked. If the user
     * is logged in, first display a modal dialog to confirm if they want to
     * quit.
     */
    public void clickCloseButton() {
        if (isLoggedIn) {
            Object[] buttons = {
                TalkStrings.get(TalkStrings.Button.EXIT),
                TalkStrings.get(TalkStrings.Button.CANCEL)};
            // display confirmation dialog window
            int n = JOptionPane.showOptionDialog(this,
                    TalkStrings.get(TalkStrings.Message.EXIT_CONFIRM),
                    "",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, //do not use a custom Icon
                    buttons, //the titles of buttons
                    buttons[0]); //default button title

            if (n == JOptionPane.YES_OPTION) {
                ArrayList<File> list = getUnusuedFiles();
                if (!list.isEmpty()) {
                    n = JOptionPane.showConfirmDialog(this,
                            TalkStrings.get(TalkStrings.Message.DELETE_OLD_RECS),
                            "",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE); //default button title
                    if (n == JOptionPane.YES_OPTION) {
                        ListIterator<File> itr = list.listIterator();
                        while (itr.hasNext()) {
                            itr.next().delete();
                        }

                    }
                }

                clickExitButton();
            }
        } else {
            clickExitButton();
        }
    }

    /**
     * Exits the application.
     */
    public void clickExitButton() {
        System.exit(EXIT_ON_CLOSE);
    }

////////////////////////////////////////////////////////////////////   Partho, you need to check this method  		
    /**
     * Checks log in information against the database on the server. If
     * successful, pulls appointment information for the table and switches
     * display of the top and main panels by removing the splash screen and
     * replacing the login interface with a banner.
     */
    public void clickLogInButton() {
        debug("=== LOG IN BUTTON CLICKED. CONTACTING SERVER. === ");
        String result;

        try {
            debug("Constructing token to send to server.");

            web_client = new TalkAbroadWebClient(
                    email,
                    password);
            result = web_client.send_request(TalkStrings.getConfig("host") + TalkStrings.getConfig("authenticationURL"));

            debug("Received result:\n" + result);
            if (!result.equals("0")) {
                appointments = new ArrayList<TALKAppointment>();

                // decrypt the base64 encoded partner information
                byte[] encrypted_result = Base64.decode(result.getBytes());
                debug("Encrypted result length: " + encrypted_result.length);
                result = new String(web_client.decrypt_aes_data(encrypted_result));

                debug("Decrypted partner data:\n" + result);
                // parse user config info such as timezone offset from GMT to properly format appointment times
                String[] lines = result.split("\\n");
                lines[0] = lines[0].replace(".", "");
                Integer version = Integer.valueOf(lines[0]);

                if (version <= VERSION) {
                    String[] partner_line = lines[1].split(";;");

                    // check to see if the recording base dir exists
                    File folder = new File(recording_basedir);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    // now set the recording dir to the subfolder of the partner
                    recording_dir = recording_basedir + "/" + partner_line[0];

                    // check to see if the partner specific recording folder exists
                    folder = new File(recording_dir);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    partnerName = partner_line[1];

                    // get a calendar object representing the system's local time
                    Calendar cal = Calendar.getInstance();
                    long zone_offset = cal.get(Calendar.ZONE_OFFSET);
                    long dst_offset = cal.get(Calendar.DST_OFFSET);

                    debug("Current local timezone set to " + cal.getTimeZone().getDisplayName());
                    debug("ZONE_OFFSET: " + (zone_offset / (1000 * 60 * 60)) + " HOURS");
                    debug("DST_OFFSET: " + (dst_offset / (1000 * 60 * 60)) + " HOURS");
                    for (int i = 2; i < lines.length; i++) {
                        String[] appointment_line = lines[i].split(";;");
                        // set the UTC time in milliseconds
                        long utc_time = Long.parseLong(appointment_line[4]) * 1000;
                        int appointment_id = Integer.parseInt(appointment_line[0]);
                        cal.setTimeInMillis(utc_time);

                        // add appointments to the list with the offset date / time
                        appointments.add(
                                new TALKAppointment(
                                appointment_id,
                                appointment_line[1],
                                appointment_line[2],
                                appointment_line[3],
                                cal.getTime(),
                                //                                                        This line fatches the appoinment status from the Server soon after login.
                                appointment_line[5],
                                //                                                        This line fetches the description from the server
                                appointment_line[6]));


//                                                Added by partho for test
                        for (int k = 0; k <= appointments.size() - 1; k++) {
                            System.out.println(appointments.get(k).getStatus());
                            System.out.println(appointments.get(k).getId());
                        }


                    }


//                                        Thhis line shows the Welcome Screen
                    welcomeLabel.setText(TalkStrings.get(TalkStrings.Label.WELCOME) + " " + partnerName);

                    isLoggedIn = true;
                    appSplitPane.setVisible(true);
                    loginPanel.setVisible(false);

                    appSplitPane.setDividerLocation(200);
                    appointmentScrollPanel.repaint();
                    appointmentControlPanel.repaint();
                    this.repaint();

                } else {
                    JOptionPane.showMessageDialog(this, TalkStrings.get(TalkStrings.Message.OLD_VERSION));
                }
            } else {
                JOptionPane.showMessageDialog(this, TalkStrings.get(TalkStrings.Message.AUTH_FAILED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

////////////////////////////////////////////////////////////////////   Partho, you need to check this method  
    public void clickAppointmentTable() {
        int rowIndex = appointmentTable.getSelectedRow();
        // check to see if a row is selected and there isn't a current call
        if (rowIndex > -1) {
            // set current appointment and enable buttons
            selectedAppointment = appointments.get(rowIndex);
            loadRecordingFiles();
            callButton.setEnabled(true);


//                        Here shows the appointment descriotions
//                        showDescription.setText(selectedAppointment.getDescription());
//                        System.out.println(selectedAppointment.getDescription());                        





            if (appointee_recordings.size() > 0 && mic_recordings.size() > 0) {
                uploadButton.setEnabled(true);
            } else {
                uploadButton.setEnabled(false);
            }

        } else {
            // clear current appointment and disable buttons
            selectedAppointment = null;
            callButton.setEnabled(false);
            uploadButton.setEnabled(false);
        }
    }

    /**
     * Build the prefix for the recording files using the selected appointment
     * ID. This is a utility function to aid with building file names for
     * recording files. Example: "recordings/12345-"
     *
     * @return A string of the recording file prefix
     */
    public String getRecordingFilePrefix(int id) {
        StringBuilder prefix = new StringBuilder();
        prefix.append(recording_dir).append("/").append(selectedAppointment.getId()).append("-");
        return prefix.toString();
    }

    /**
     * Build the appointee recording file based on the selected appointment and
     * the recording iteration. This is a utility function to aid with loading
     * recordings for multi-session conversations.
     *
     * @param session The number of the session
     * @return A file object representing the recording.
     */
    public File getAppointeeRecordingFile(int id, int session) {
        StringBuilder temp = new StringBuilder();
        temp.append(getRecordingFilePrefix(id)).append("app-").append(session).append(".wav");
        return new File(temp.toString());
    }

    /**
     * Build the mic recording file based on the selected appointment and the
     * recording iteration. This is a utility function to aid with loading
     * recordings for multi-session conversations.
     *
     * @param session The number of the session
     * @return A file object representing the recording.
     */
    public File getMicRecordingFile(int id, int session) {
        StringBuilder temp = new StringBuilder();
        temp.append(getRecordingFilePrefix(id)).append("mic-").append(session).append(".wav");
        return new File(temp.toString());
    }

    /**
     * Obtain the next session number for the selected appointment based on the
     * current size of the recording file vectors appointee_recordings and
     * mic_recordings.
     *
     * @return
     */
    public int getNextRecordingSessionNumber() {
        return Math.min(appointee_recordings.size(), mic_recordings.size()) + 1;
    }

    /**
     * Returns a File object that represents the final mixdown MP3 of the
     * conversation.
     *
     * @return a File object pointing to the final recording.
     */
    public File getFinalRecordingFile(int id) {
        return new File(getRecordingFilePrefix(id) + "final.mp3");
    }

    /**
     * Utility method to parse the integer appointment ID from the name of a
     * recording file.
     *
     * @param in : an audio recording file associated with an appointment
     * @return the integer id of the appointment associated with this recording
     * file
     */
    public int getIdFromRecordingFile(File in) {
        String[] tokens = in.getName().split("-");
        return Integer.valueOf(tokens[0]);
    }

    /**
     * Returns an ArrayList with recording files no longer necessary. Only audio
     * files associated with appontments that are not nece
     *
     * @param in_files
     *
     */
    public ArrayList<File> getUnusuedFiles() {
        ArrayList<File> retval = new ArrayList<File>();
        File dir = new File(recording_dir);
        ArrayList<Integer> ids = new ArrayList<Integer>();
        String[] current_files;

        ListIterator<TALKAppointment> itr = appointments.listIterator();
        while (itr.hasNext()) {
            ids.add(itr.next().getId());
        }

        FilenameFilter wav_filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".wav");
            }
        };
        FilenameFilter mp3_filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        };

        current_files = dir.list(wav_filter);
        for (int i = 0; i < current_files.length; i++) {
            File current = new File(recording_dir + "/" + current_files[i]);
            if (!ids.contains(getIdFromRecordingFile(current))) {
                retval.add(current);
            }
        }

        current_files = dir.list(mp3_filter);
        for (int i = 0; i < current_files.length; i++) {
            File current = new File(recording_dir + "/" + current_files[i]);
            if (!ids.contains(getIdFromRecordingFile(current))) {
                retval.add(current);
            }
        }

        return retval;
    }

    /**
     * Loads any existing recording sessions for the selected appointment.
     */
    public void loadRecordingFiles() {





//                        Here shows the appointment descriotions by the side of call button          
        debug("Loading existing recording files for appointment " + selectedAppointment.getId());
        debug("Assignment: " + selectedAppointment.getDescription());
//                showDescription.setText("Assignment: " + selectedAppointment.getDescription());
        assignmentDescTextPane.setText("Assignment: " + selectedAppointment.getDescription());











        appointee_recordings = new Vector<File>();
        mic_recordings = new Vector<File>();

        File appointee = null;
        File mic = null;
        int i = 1;
        boolean done = false;
        while (!done) {
            // Load both files
            appointee = getAppointeeRecordingFile(selectedAppointment.getId(), i);
            mic = getMicRecordingFile(selectedAppointment.getId(), i);

            // Check to see if both files exist. If so, add to the list of recordings
            if (appointee.exists() && mic.exists()) {
                debug("Found recorded session files:\n" + appointee.getAbsolutePath() + "\n" + mic.getAbsolutePath());
                appointee_recordings.add(appointee);
                mic_recordings.add(mic);
                i++;
            } else {
                done = true;
            }
        }
    }

//        I think, for third update i need to change on this method
    public void callAppointee() {
        // Check to see if the recordings folder exists. If not, create it.

//                String dirPath = "C:\\LogKeeper_App_Data\\recordings";
//		File rec_dir = new File (dirPath);
        File rec_dir = new File("recordings");
        if (!rec_dir.exists()) {
            rec_dir.mkdir();
        }

        class CallAppointee extends SwingWorker<String, String> {

            private JLabel label;

            public CallAppointee(JLabel label) {
                this.label = label;
            }

            public String doInBackground() {
                debug("Calling appointee.");
                String skypeName = selectedAppointment.getSkypeName();
                Call.Status current_status = null;
                boolean connected = false;
                try {
                    log(LogStatus.CALL_INITIATED);
                    currentcall = Skype.call(skypeName);
                    boolean done = false;
                    while (!done) {
                        Call.Status status = currentcall.getStatus();
                        if (status != current_status) {
                            current_status = status;
                            debug("Call status changed: " + current_status);
                            switch (current_status) {
                                case ROUTING:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.ROUTING));
                                    break;
                                case RINGING:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.RINGING));
                                    break;
                                case INPROGRESS:
                                    connected = true;
                                    publish(TalkStrings.get(TalkStrings.CallStatus.CONNECTED));
                                    appointee_recording = getAppointeeRecordingFile(selectedAppointment.getId(), getNextRecordingSessionNumber());
                                    mic_recording = getMicRecordingFile(selectedAppointment.getId(), getNextRecordingSessionNumber());
                                    currentcall.setFileOutput(appointee_recording);
                                    currentcall.setFileCaptureMic(mic_recording);
                                    debug("Recording appointee audio file: " + appointee_recording.getAbsolutePath());
                                    debug("Recording mic audio file: " + mic_recording.getAbsolutePath());
                                    log(LogStatus.CALL_CONNECTED);
                                    break;
                                case VM_PLAYING_GREETING:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.VM));
                                    break;
                                case FAILED:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.FAILED));
                                    log(LogStatus.CALL_FAILED);
                                    done = true;
                                    break;
                                case FINISHED:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.FINISHED));
                                    log(LogStatus.CALL_FINISHED);
                                    done = true;
                                    break;
                                case CANCELLED:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.CANCELLED));
                                    log(LogStatus.CALL_CANCELLED);
                                    done = true;
                                    break;
                                case BUSY:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.BUSY));
                                    log(LogStatus.CALL_INCOMPLETE, "busy");
                                    done = true;
                                    break;
                                case MISSED:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.MISSED));
                                    log(LogStatus.CALL_INCOMPLETE, "missed");
                                    done = true;
                                    break;
                                case REFUSED:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.REFUSED));
                                    log(LogStatus.CALL_INCOMPLETE, "refused");
                                    done = true;
                                    break;
                                case VM_RECORDING:
                                    // do nothing
                                    break;
                                case VM_SENT:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.FINISHED));
                                    log(LogStatus.CALL_INCOMPLETE, "vm sent");
                                    done = true;
                                    break;
                                case VM_CANCELLED:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.FINISHED));
                                    log(LogStatus.CALL_INCOMPLETE, "vm cancelled");
                                    done = true;
                                    break;
                                case VM_FAILED:
                                    publish(TalkStrings.get(TalkStrings.CallStatus.FAILED));
                                    log(LogStatus.CALL_INCOMPLETE, "vm failed");
                                    done = true;
                                    break;
                                case UNPLACED:
                                    break;
                                default:
                                    debug("Unhandled call status returned: " + current_status);
                                    break;
                            }
                        }
                        Thread.sleep(100);
                    }
                    if (connected) {
                        appointee_recordings.add(appointee_recording);
                        mic_recordings.add(mic_recording);
                    }
                } catch (Exception e) {
                    displayError(e);
                }
                debug("Exiting call worker thread.");



                clickLogInButton(); // Refreshes the appointment list after processing each call


                return "";
            }

            public void process(List<String> chunks) {
                for (String status : chunks) {
                    label.setText(TalkStrings.get(TalkStrings.Label.CALL_STATUS) + status);
                }
            }
//                        clickLogInButton();
        }

        /**
         * This internal class handles the closing of the dialog box that is
         * shown when the user is converting or uploading the recorded mixdown
         * final audio file.
         *
         * @author Masahiro Knittel
         *
         */
        class StatusPropertyListener implements PropertyChangeListener {

            JDialog dialog;

            public StatusPropertyListener(JDialog dialog) {
                this.dialog = dialog;
            }

            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("state")
                        && event.getNewValue() == SwingWorker.StateValue.DONE) {
                    dialog.setVisible(false);
                    clickAppointmentTable();
                }

            }
        }

        // This JLabel is passed to the internal SwingWorker classes as a means of
        // notifying the end user of the progress of the conversion/upload process
        JLabel status = new JLabel();
        JDialog statusDialog = new JDialog(this, TalkStrings.get(TalkStrings.Label.CALL_STATUS), true);
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout());
        status.setBounds(0, 0, 200, 20);
        hangupButton.setBounds(0, 30, 200, 30);
        displayPanel.add(status, BorderLayout.NORTH);
        displayPanel.add(hangupButton, BorderLayout.SOUTH);
        statusDialog.add(displayPanel);
        statusDialog.setResizable(false);
        int statusDialogWidth = 200;
        int statusDialogHeight = 80;
        int statusDialogX = this.getX() + (this.getWidth() / 2) - (statusDialogWidth / 2);
        int statusDialogY = this.getY() + (this.getHeight() / 2) - (statusDialogHeight / 2);
        statusDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        statusDialog.setBounds(statusDialogX, statusDialogY, statusDialogWidth, statusDialogHeight);

        CallAppointee callWorker = new CallAppointee(status);
        callWorker.addPropertyChangeListener(new StatusPropertyListener(statusDialog));
        callWorker.execute();
        statusDialog.setVisible(true);
    }

    public void uploadConversation() {

        /**
         * This internal class is a SwingWorker class that processes the mixing
         * of the recorded audio files in the background while interacting with
         * a JLabel to notify the user of how many bytes have been written.
         *
         * @author Masahiro Knittel
         *
         */
        class ConversionTask extends SwingWorker<Integer, Integer> {

            private JLabel label;

            public ConversionTask(JLabel label) {
                this.label = label;
            }

            // The main worker method that mixes the audio
            public Integer doInBackground() {
                debug("Mixing recorded audio files into MP3.");
                File outfile = getFinalRecordingFile(selectedAppointment.getId());
                if (outfile.exists()) {
                    outfile.delete();
                }
                // Construct the arrays that hold the multiple conversation segments
                AudioInputStream[] appointee_streams = new AudioInputStream[appointee_recordings.size()];
                AudioInputStream[] mic_streams = new AudioInputStream[mic_recordings.size()];

                try {
                    // Loop through each segment and construct an AudioInputStream from it
                    for (int i = 0; i < appointee_streams.length; i++) {
                        appointee_streams[i] = AudioSystem.getAudioInputStream(appointee_recordings.get(i));
                        mic_streams[i] = AudioSystem.getAudioInputStream(mic_recordings.get(i));
                    }
                } catch (Exception e) {
                    displayError(e);
                }

                // Construct the mixing array used to mix down the mic and remote audio streams
                AudioInputStream[] mixing_streams = new AudioInputStream[2];
                mixing_streams[0] = new ConcatenatingAudioInputStream(appointee_streams);
                mixing_streams[1] = new ConcatenatingAudioInputStream(mic_streams);

                // Set the MP3 output properties
                System.setProperty("tritonus.lame.quality", "lowest");
                System.setProperty("tritonus.lame.bitrate", "32");

                /**
                 * This internal class implements the runnable interface and is
                 * spawned by the SwingWorker thread so that the actual writing
                 * of the audio file is in its own isolated thread. This permits
                 * monitoring of how many bytes have been mixed down within the
                 * SwingWorker thread and updating the UI via the JLabel object
                 * passed to the SwingWorker's constructor.
                 *
                 * @author Masahiro Knittel
                 *
                 */
                class WritingThread implements Runnable {

                    private AudioInputStream mix;
                    private AudioFormat.Encoding encoding;
                    private AudioFileFormat.Type type;

                    public WritingThread(
                            AudioFormat.Encoding encoding,
                            AudioFileFormat.Type type,
                            AudioInputStream mix) {
                        this.encoding = encoding;
                        this.type = type;
                        this.mix = mix;
                    }

                    public void run() {
                        try {
                            debug("Writing audio file...");
                            File outfile = getFinalRecordingFile(selectedAppointment.getId());
                            AudioSystem.write(AudioSystem.getAudioInputStream(encoding, mix), type, outfile);
                        } catch (Exception e) {
                            displayError(e);
                        }
                    }
                }

                // Spawn a new thread to write the audio file
                Thread writingThread = new Thread(
                        new WritingThread(
                        Encodings.getEncoding("MPEG1L3"),
                        AudioFileTypes.getType("MP3", "mp3"),
                        new MixingAudioInputStream(mixing_streams)));

                // Start mixing
                writingThread.start();

                // Update the JLabel passed to the constructor of this object
                // with how many bytes have been written to the file
                int size = 0;
                while (writingThread.isAlive()) {
                    size = (int) getFinalRecordingFile(selectedAppointment.getId()).length();
                    publish(size);
                    // debug("Worker thread publishing size " + size);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        displayError(e);
                    }
                }
                size = (int) getFinalRecordingFile(selectedAppointment.getId()).length();
                return size;
            }

            protected void process(List<Integer> chunks) {
                for (Integer size : chunks) {
                    label.setText(TalkStrings.get(TalkStrings.Label.BYTES_CONVERTED) + size);
                }
            }
        };

        /**
         * This internal class is a SwingWorker class that processes the
         * uploading of the recorded audio files in the background while
         * interacting with a JLabel to notify the user of how many bytes have
         * been written.
         *
         * @author Masahiro Knittel
         *
         */
        class UploadTask extends SwingWorker<Integer, Integer> {

            private JLabel label;

            public UploadTask(JLabel label) {
                this.label = label;
            }

            public Integer doInBackground() {
                debug("Uploading MP3 to server.");
                /**
                 * This internal class implements the runnable interface and is
                 * spawned by the SwingWorker thread so that the actual
                 * transmission of the audio file is in its own isolated thread.
                 * This permits monitoring of how many bytes have been sent to
                 * the server by the SwingWorker thread and updating the UI via
                 * the JLabel object passed to the SwingWorker's constructor.
                 *
                 * @author Masahiro Knittel
                 *
                 */
                class UploadingThread implements Runnable {

                    public void run() {
                        try {
                            debug("Writing audio file...");
                            File outfile = getFinalRecordingFile(selectedAppointment.getId());
                            Hashtable<String, String> values = new Hashtable<String, String>();
                            values.put("id", Integer.toString(selectedAppointment.getId()));
                            String response = web_client.send_request(TalkStrings.getConfig("host") + TalkStrings.getConfig("uploadURL"), values, outfile);
                            debug("Received response from server:\n" + response);
                        } catch (Exception e) {
                            displayError(e);
                        }
                    }
                }

                // Spawn a new thread to upload the file
                Thread uploadingThread = new Thread(new UploadingThread());
                // Start uploading
                uploadingThread.start();

                // Update the JLabel passed to the constructor of this object
                // with how many bytes have been sent to the server
                int size = 0;
                while (uploadingThread.isAlive()) {
                    size = web_client.get_file_bytes_written();
                    publish(size);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        displayError(e);
                    }
                }
                size = web_client.get_file_bytes_written();
                return size;
            }

            protected void process(List<Integer> chunks) {
                for (Integer size : chunks) {
                    if (size > 0) {
                        label.setText(TalkStrings.get(TalkStrings.Label.BYTES_UPLOADED) + size);
                    } else {
                        label.setText(TalkStrings.get(TalkStrings.Label.FINALIZING_UPLOAD));
                    }
                }
            }
        }

        /**
         * This internal class handles the closing of the dialog box that is
         * shown when the user is converting or uploading the recorded mixdown
         * final audio file.
         *
         * @author Masahiro Knittel
         *
         */
        class StatusPropertyListener implements PropertyChangeListener {

            JDialog dialog;

            public StatusPropertyListener(JDialog dialog) {
                this.dialog = dialog;
            }

            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("state")
                        && event.getNewValue() == SwingWorker.StateValue.DONE) {
                    dialog.setVisible(false);
                }

            }
        }

        // Check to see if a final mixdown file already exists and display confirmation dialog
        boolean convert = false;
        if (getFinalRecordingFile(selectedAppointment.getId()).exists()) {
            int n = JOptionPane.showConfirmDialog(
                    null,
                    TalkStrings.get(TalkStrings.Message.EXISTING_MP3),
                    "",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE); //default button title
            if (n == JOptionPane.YES_OPTION) {
                convert = true;
            }
        } else {
            convert = true;
        }

        // This JLabel is passed to the internal SwingWorker classes as a means of
        // notifying the end user of the progress of the conversion/upload process
        JLabel status = new JLabel();
        JDialog statusDialog = new JDialog(this, TalkStrings.get(TalkStrings.Label.CONVERTING_MP3), true);
        statusDialog.add(status);
        statusDialog.setResizable(false);
        int statusDialogWidth = 200;
        int statusDialogHeight = 50;
        int statusDialogX = this.getX() + (this.getWidth() / 2) - (statusDialogWidth / 2);
        int statusDialogY = this.getY() + (this.getHeight() / 2) - (statusDialogHeight / 2);
        statusDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        statusDialog.setBounds(statusDialogX, statusDialogY, statusDialogWidth, statusDialogHeight);

        if (convert) {
            ConversionTask converter = new ConversionTask(status);
            converter.addPropertyChangeListener(new StatusPropertyListener(statusDialog));
            converter.execute();
            statusDialog.setVisible(true);
        }
        UploadTask uploader = new UploadTask(status);
        uploader.addPropertyChangeListener(new StatusPropertyListener(statusDialog));
        uploader.execute();
        statusDialog.setVisible(true);

        // We are done with the dialog box so let's dispose of it
        statusDialog.dispose();
        clickLogInButton(); // Refreshes the appointment list after processing audio file
        loadRecordingFiles();
    }

    /**
     * Displays a JOPtionPane indicating that an error occurred with Skype
     */
    public void displayError(Exception e) {
        if (e instanceof NotAttachedException) {
            JOptionPane.showMessageDialog(null, TalkStrings.get(TalkStrings.Message.SKYPE_NOT_CONNECTED));
        } else if (e instanceof SkypeException) {
            JOptionPane.showMessageDialog(null, TalkStrings.get(TalkStrings.Message.SKYPE_ERROR));
        } else {
            e.printStackTrace();
            System.exit(ERROR);
        }
        statusLabel.setText(TalkStrings.get(TalkStrings.Label.ERROR_OCCURRED));
    }

    public void displayErrorText(String s) {
        JOptionPane.showMessageDialog(mainPanel, s);
    }

    public void displaySkypeNotRunning() {
        Object[] buttons = {
            TalkStrings.get(TalkStrings.Button.CONTINUE),
            TalkStrings.get(TalkStrings.Button.EXIT)
        };
        int n = JOptionPane.showOptionDialog(this,
                TalkStrings.get(TalkStrings.Message.SKYPE_NOT_RUNNING),
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, //do not use a custom Icon
                buttons, //the titles of buttons
                buttons[0]); //default button title
        if (n == JOptionPane.NO_OPTION) {
            clickExitButton();
        }
    }

    public static enum LogStatus {

        CALL_INITIATED(1),
        CALL_CANCELLED(2),
        CALL_FAILED(3),
        CALL_CONNECTED(4),
        CALL_FINISHED(5),
        CALL_INCOMPLETE(6),
        CALL_UPLOADED(7),
        CALL_STATUS(0);
        private int intVal;

        LogStatus(int intVal) {
            this.intVal = intVal;
        }

        public String toString() {
            return Integer.toString(intVal);
        }
    }

    public void log(LogStatus status) {
        log(status, null);
    }

    public void log(LogStatus status, String details) {
        Hashtable<String, String> data = new Hashtable<String, String>();
        data.put("id", Integer.toString(selectedAppointment.getId()));
        data.put("status", status.toString());
        if (details != null) {
            data.put("details", details);
        }
        String result = web_client.send_request(TalkStrings.getConfig("host") + TalkStrings.getConfig("logURL"), data);
        debug("Log entry result: \n" + result);
    }

    /**
     * Overrides the JFrame's default paint method. This method first checks to
     * see if the user is logged in, and if not, displays the splash image in
     * the main panel. If the user is logged in, it instead displays the banner
     * image in the top panel where the log in interface was.
     *
     * @param g Graphics object passed to the paint method by the framework.
     *
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (!isLoggedIn) {
            mainPanel.getGraphics().drawImage(splashImage, 0, 0, null);
        } else {
            topPanel.getGraphics().drawImage(bannerImage, 0, 0, null);
        }
    }

    private static String toHex(byte[] buf) {
        char[] HEX_CHARS = "0123456789abcdef".toCharArray();
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    /**
     * Debug output to send to console.
     *
     * @param s
     */
    public static void debug(String s) {
        if (TalkStrings.getConfig("debug").toLowerCase().equals("yes")) {
            System.out.println("JAVA(" + Thread.currentThread().getName() + "): " + s);
        }
    }
}


