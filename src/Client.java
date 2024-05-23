
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Client extends JFrame {
    // IO streams
    
    DataOutputStream toServer = null;
    DataInputStream fromServer = null;
    
    CardLayout cardLayout = new CardLayout();
    JPanel cardPanel = new JPanel(cardLayout);
    JPanel frontPanel;
    JPanel mainPanel;
    JTextArea textArea = null;
    JTextField usernameField = null;
    JPasswordField passwordField = null;
    Socket socket = null;
    JButton loginButton;
    JButton signUpButton;
    JButton openButton;
    JButton closeButton;
    String user;

    JPanel topPanel;
    JPanel eventPanel;
    JTextField messegeTextField;
    JScrollPane bottomScrollPane;

    JTable table;
    DefaultTableModel tableModel;

    JPanel topRow;
    JTextField eventNameField;
    JPanel middleRow;
    String[] dayOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    String[] times = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
    JComboBox<String> dayDropdown;
    JComboBox<String> startTimeDropdown;
    JComboBox<String> endTimeDropdown;
    JTextField locationField;
    Integer[] emergencyLevels = {1, 2, 3, 4, 5};
    JComboBox<Integer> emergencyLevelDropdown;
    JPanel bottomRow;
    JButton addButton;
    JScrollPane tableScrollPane;
    JButton refreshButton;
    JPanel deletePanel;
    JTextField numberTextField;
    JButton deleteButton;
    JButton logoutButton;
    JPanel loginPanel;
    

    public Client() {
        super("Client");

        // Initialize components
        
        frontPanel = new JPanel(new BorderLayout());
        mainPanel = new JPanel(new BorderLayout());

        cardPanel.add(frontPanel, "FrontPanel");
        cardPanel.add(mainPanel, "MainPanel");

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        textArea = new JTextArea(10, 30);  // Adjusted size
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea); // Added scroll pane for text area
    
        // Login panel
        loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        signUpButton = new JButton("Sign up");
        signUpButton.addActionListener(new signUpButtionListener());
        loginPanel.add(signUpButton);
        loginButton = new JButton("Login");
        loginPanel.add(loginButton);
        loginButton.addActionListener(new LoginButtonListener());
        
    
        // Control panel
        JPanel controlPanel = new JPanel();
        openButton = new JButton("Open Connection");
        closeButton = new JButton("Close Connection");
        controlPanel.add(openButton);
        controlPanel.add(closeButton);
        
        frontPanel.add(controlPanel, BorderLayout.NORTH);
        frontPanel.add(loginPanel, BorderLayout.CENTER);
        frontPanel.add(scrollPane, BorderLayout.SOUTH); // Adding scroll pane

    
        // Event listeners
        closeButton.addActionListener((e) -> {
            try {
                socket.close();
                textArea.append("connection closed\n");
            } catch (Exception e1) {
                System.err.println("error");
            }
        });
        openButton.addActionListener(new OpenConnectionListener());
    
        // Frame properties
        add(cardPanel);
        cardLayout.show(cardPanel, "FrontPanel");
        setSize(500, 400); // Adjusted size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    

    class OpenConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            try {
                socket = new Socket("localhost", 8000);
                textArea.append("connected");
                toServer = new DataOutputStream(socket.getOutputStream());
                fromServer = new DataInputStream(socket.getInputStream());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                textArea.append("connection Failure");
            }
        }

    }

    class signUpButtionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Establish connection if not already connected
                if (socket == null || socket.isClosed()) {
                    socket = new Socket("localhost", 8000);
                    toServer = new DataOutputStream(socket.getOutputStream());
                    fromServer = new DataInputStream(socket.getInputStream());
                }
                toServer.writeUTF("SIGNUP");
                toServer.flush();
                // Retrieve username and password
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
    
                // Send credentials to server
                toServer.writeUTF(username);
                toServer.writeUTF(password);
                toServer.flush();
    
                // Receive confirmation from server
                String response = fromServer.readUTF();
                textArea.append(response + "\n");
                if (response.equals("Signup succeeds")){
                    textArea.append("Please login in");
                    
                }
    
            } catch (IOException ex) {
                textArea.append("Error: " + ex.getMessage() + "\n");
            }
        }

    }
    
    class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Establish connection if not already connected
                if (socket == null || socket.isClosed()) {
                    socket = new Socket("localhost", 8000);
                    toServer = new DataOutputStream(socket.getOutputStream());
                    fromServer = new DataInputStream(socket.getInputStream());
                }
                //send command
                toServer.writeUTF("LOGIN");
                toServer.flush();
                // Retrieve username and password
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
    
                // Send credentials to server
                toServer.writeUTF(username);
                toServer.writeUTF(password);
                toServer.flush();
    
                // Receive confirmation from server
                String response = fromServer.readUTF();
                textArea.append(response + "\n");
                if (response.equals("Login succeeds")){
                    //open a new window 
                    user = username;
                    initialize_mainPanel();
                    cardLayout.show(cardPanel,"MainPanel");
                    setSize(getPreferredSize());
                }
    
            } catch (IOException ex) {
                textArea.append("Error: " + ex.getMessage() + "\n");
            }
        }
    }
    
    class addActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String eventName = eventNameField.getText();
            String dayOfWeek = (String) dayDropdown.getSelectedItem();
            String startTime = (String) startTimeDropdown.getSelectedItem();
            String endTime = (String) endTimeDropdown.getSelectedItem();
            String location = locationField.getText();
            int emergencyLevel = (Integer) emergencyLevelDropdown.getSelectedItem();
    
            // Now, send this data to the server
            sendEventToServer(eventName, dayOfWeek, startTime, endTime, location, emergencyLevel);
            refreshTable();
        }
    }
    
    class deleteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                //send command
                toServer.writeUTF("DELETE_EVENT");
                toServer.flush();

                toServer.writeUTF(user);
                int eventID = Integer.parseInt(numberTextField.getText());
                toServer.writeInt(eventID);
                toServer.flush();
    
                // Receive confirmation from server
                String response = fromServer.readUTF();
                messegeTextField.setText(response);
                refreshTable();
    
            } catch (IOException ex) {
                textArea.append("Error: " + ex.getMessage() + "\n");
            }
            
        }

    }
    
    class LogoutListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                toServer.writeUTF("LOGOUT");
                toServer.flush();
                cardLayout.show(cardPanel,"FrontPanel");
                loginPanel.setSize(getPreferredSize());
                repaint();
                textArea.append("Successfully logged out\n");

            } catch (IOException e1) {
                textArea.append("Error: " + e1.getMessage() + "\n");
            }
            
        }

    }
    public void initialize_mainPanel() {
        // Top control section
        topPanel = new JPanel(new BorderLayout());

        //MessegeTextField
        messegeTextField = new JTextField();
        messegeTextField.setText("Welcome " + user + " !");
        messegeTextField.setEditable(false);
        topPanel.add(messegeTextField, BorderLayout.NORTH);

        //EventPanel 
        eventPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        initializeEventPanel();

        topPanel.add(eventPanel, BorderLayout.CENTER);


        //Bottom table section
        initializa_table();
        bottomScrollPane = new JScrollPane(tableScrollPane);

        //delete Panel
        deletePanel = new JPanel(new FlowLayout());
        deletePanel.add(new JLabel("To delete event, select the EventID: "));
        numberTextField = new JTextField();
        numberTextField.setPreferredSize(new Dimension(150, 20));
        deletePanel.add(numberTextField);
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new deleteListener());
        deletePanel.add(deleteButton);

        logoutButton = new JButton("Log out");
        logoutButton.addActionListener(new LogoutListener());
        deletePanel.add(logoutButton);


        // Adding components to mainPanel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(bottomScrollPane, BorderLayout.CENTER);
        mainPanel.add(deletePanel, BorderLayout.SOUTH);
    }

    private void initializeEventPanel() {
        // Top row of event panel
        topRow = new JPanel(new FlowLayout());
        topRow.add(new JLabel("Event Name:"));
        eventNameField = new JTextField(20);
        topRow.add(eventNameField);
        eventPanel.add(topRow);

        // Middle row: Day, Start Time, End Time, Location, Emergency Level
        middleRow = new JPanel(new FlowLayout());
        middleRow.add(new JLabel("On"));
        dayDropdown = new JComboBox<>(dayOfWeek);
        middleRow.add(dayDropdown);

        middleRow.add(new JLabel("from"));
        startTimeDropdown = new JComboBox<>(times);
        middleRow.add(startTimeDropdown);
        middleRow.add(new JLabel("to"));
        endTimeDropdown = new JComboBox<>(times);
        middleRow.add(endTimeDropdown);
        middleRow.add(new JLabel("at"));
        locationField = new JTextField(20);
        middleRow.add(locationField);
        middleRow.add(new JLabel("Emergency level:"));
        emergencyLevelDropdown = new JComboBox<>(emergencyLevels);
        middleRow.add(emergencyLevelDropdown);
        eventPanel.add(middleRow);

        // Bottom row: Add Button
        bottomRow = new JPanel(new FlowLayout());
        addButton = new JButton("Add Event");
        addButton.addActionListener(new addActionListener());
        bottomRow.add(addButton);

        refreshButton = new JButton("Refresh Table");
        refreshButton.addActionListener((e)->{
            refreshTable();
        });
        bottomRow.add(refreshButton);

        eventPanel.add(bottomRow);


    }
    
    private void sendEventToServer(String eventName, String dayOfWeek, String startTime, String endTime, String location, int emergencyLevel) {
        try {
            toServer.writeUTF("ADD_EVENT");
            toServer.flush();
            toServer.writeUTF(eventName);
            toServer.writeUTF(user);
            toServer.writeUTF(dayOfWeek);
            toServer.writeUTF(startTime);
            toServer.writeUTF(endTime);
            toServer.writeUTF(location);
            toServer.writeInt(emergencyLevel);
            toServer.flush();
    
            // handle server response
            String response = fromServer.readUTF();
            messegeTextField.setText(response);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void initializa_table() {
        // Column names
        String[] columnNames = {"EventID", "EventName", "Hostname", "Day", "Start Time", "End Time", "Location", "Emergency Level"};

        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0); // 0 signifies the rows start empty
        refreshTable();
        table = new JTable(tableModel);

        // Set up the scroll pane and add it to the table
        tableScrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
    }

    private void populateTable() {
        Vector<Vector<Object>> events = SQLiteConnection.getAllEvents();
        for (Vector<Object> row : events) {
            tableModel.addRow(row);
        }
    }
    
    public void refreshTable() {
        tableModel.setRowCount(0); // Clear existing data
        populateTable(); // Repopulate with fresh data
    }


    public static void main(String[] args) {
        Client c = new Client();
        c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.setVisible(true);
    }
    
}
