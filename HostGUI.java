import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by mitchcout on 11/20/2017.
 */
public class HostGUI extends JPanel {

    /* Contains host functionality */
    private HostGUIFunctions host;

    private JPanel group1, group2, group3;
    private JPanel group1sub1, group1sub2, group2sub1, group2sub2, group3sub1, group3sub2;

    private JLabel serverHostnameLabel, portLabel, usernameLabel, hostnameLabel, speedLabel, keywordLabel, commandLabel;

    private JButton connectButton, searchButton, goButton;

    private JTable searchTable;

    private JTextField serverHostnameField, portField, usernameField, hostnameField, keywordField, commandField;
    private JComboBox<String> speedField;
    private JTextArea outputArea;

    private JScrollPane outputScrollPane, tablePane;

    private ButtonListener m1;

    private Object[][] searchResults;

    public HostGUI() {
        // init data
        host = new HostGUIFunctions();
        String[] speedOptions = {"Ethernet", "Modem", "T1", "T3"};
        String[] tableColumns = {"Speed", "Hostname", "Filename"};
        searchResults = new Object[][]{};

        // set section headers
        String sectionHeader1 = "Connection";
        String sectionHeader2 = "Search";
        String sectionHeader3 = "FTP";

        // create objects
        group1 = new JPanel();
        group2 = new JPanel();
        group3 = new JPanel();
        group1sub1 = new JPanel();
        group1sub2 = new JPanel();
        group2sub1 = new JPanel();
        group2sub2 = new JPanel();
        group3sub1 = new JPanel();
        group3sub2 = new JPanel();
        serverHostnameLabel = new JLabel("Server Hostname: ");
        portLabel = new JLabel("Port: ");
        usernameLabel = new JLabel("Username: ");
        hostnameLabel = new JLabel("Hostname: ");
        speedLabel = new JLabel("Speed: ");
        keywordLabel = new JLabel("Keyword: ");
        commandLabel = new JLabel("Enter Command: ");
        connectButton = new JButton("Connect");
        searchButton = new JButton("Search");
        goButton = new JButton("Go");
        searchTable = new JTable(searchResults, tableColumns);
        serverHostnameField = new JTextField("", 30);
        portField = new JTextField("", 5);
        usernameField = new JTextField("", 15);
        hostnameField = new JTextField("", 20);
        speedField = new JComboBox<String>(speedOptions);
        keywordField = new JTextField("", 20);
        commandField = new JTextField("", 45);
        outputArea = new JTextArea(5,60);
        outputScrollPane = new JScrollPane(outputArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tablePane = new JScrollPane(searchTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m1 = new ButtonListener();

        // add JButton listeners
        connectButton.addActionListener(m1);
        searchButton.addActionListener(m1);
        goButton.addActionListener(m1);

        // view adjustments
        connectButton.setPreferredSize(new Dimension(150,20));
        searchButton.setPreferredSize(new Dimension(75,20));
        goButton.setPreferredSize(new Dimension(50,20));
        tablePane.setPreferredSize(new Dimension(700, 150));
        outputArea.setEditable(false);
        speedField.setBackground(Color.WHITE);
        goButton.setEnabled(false);
        searchButton.setEnabled(false);

        // set JPanel layout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        group1.setLayout(new BoxLayout(group1, BoxLayout.Y_AXIS));
        group2.setLayout(new BoxLayout(group2, BoxLayout.Y_AXIS));
        group3.setLayout(new BoxLayout(group3, BoxLayout.Y_AXIS));
        group1.setBorder(BorderFactory.createTitledBorder(sectionHeader1));
        group2.setBorder(BorderFactory.createTitledBorder(sectionHeader2));
        group3.setBorder(BorderFactory.createTitledBorder(sectionHeader3));

        // group fields, labels, and buttons into JPanels
        group1sub1.add(serverHostnameLabel);
        group1sub1.add(serverHostnameField);
        group1sub1.add(portLabel);
        group1sub1.add(portField);
        group1sub1.add(connectButton);
        group1sub2.add(usernameLabel);
        group1sub2.add(usernameField);
        group1sub2.add(hostnameLabel);
        group1sub2.add(hostnameField);
        group1sub2.add(speedLabel);
        group1sub2.add(speedField);

        group2sub1.add(keywordLabel);
        group2sub1.add(keywordField);
        group2sub1.add(searchButton);
        group2sub2.add(tablePane);

        group3sub1.add(commandLabel);
        group3sub1.add(commandField);
        group3sub1.add(goButton);
        group3sub2.add(outputScrollPane);

        group1.add(group1sub1);
        group1.add(group1sub2);
        group2.add(group2sub1);
        group2.add(group2sub2);
        group3.add(group3sub1);
        group3.add(group3sub2);

        // add JPanels to master panel
        add(group1);
        add(group2);
        add(group3);
    }

    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == connectButton){
                outputArea.append(">> Connect "+serverHostnameField.getText()+" "+portField.getText()+"\n");
                boolean connected = host.connect(serverHostnameField.getText(), portField.getText(),
                                                usernameField.getText(), hostnameField.getText(),
                                                (String) speedField.getSelectedItem());
                if(connected){
                    outputArea.append("Connected to "+serverHostnameField.getText()+":"+portField.getText()+"\n");
                    connectButton.setEnabled(false);
                    connectButton.setText("Connected");
                    goButton.setEnabled(true);
                    searchButton.setEnabled(true);
                    serverHostnameField.setEnabled(false);
                    portField.setEnabled(false);
                    usernameField.setEnabled(false);
                    hostnameField.setEnabled(false);
                    speedField.setEnabled(false);
                } else {
                    outputArea.append("Could not connect. Check parameters and try again."+"\n");
                }
            } else if(e.getSource() == searchButton){
                boolean success = host.search(keywordField.getText());
            } else if(e.getSource() == goButton){
                outputArea.append(">> "+commandField.getText()+"\n");
                String response = host.enterCommand(commandField.getText());
                if(response.equals("close")) {
                    outputArea.append("Disconnected from server"+"\n");
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                    goButton.setEnabled(false);
                    searchButton.setEnabled(false);
                    serverHostnameField.setEnabled(true);
                    portField.setEnabled(true);
                    usernameField.setEnabled(true);
                    hostnameField.setEnabled(true);
                    speedField.setEnabled(true);
                } else if(response != null) {
                    outputArea.append(response+"\n");
                }
            }
        }
    }
}
