import java.io.*;
import java.net.*;
import java.util.Date;
import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MultiThreadServer extends JFrame implements Runnable {
    // Text area for displaying contents
    private JTextArea ta;

    // Number a client
    private int clientNo = 0;

    public MultiThreadServer() {
    	
        ta = new JTextArea(10, 10);
        JScrollPane sp = new JScrollPane(ta);
        this.add(sp);
        this.setTitle("MultiThreadServer");
        this.setSize(400, 200);
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(8000);
            ta.append("MultiThreadServer started at "
                    + new Date() + '\n');

            while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();

                // Increment clientNo
                clientNo++;

                ta.append("Starting thread for client " + clientNo +
                        " at " + new Date() + '\n');

                // Find the client's host name, and IP address
                InetAddress inetAddress = socket.getInetAddress();
                ta.append("Client " + clientNo + "'s host name is "
                        + inetAddress.getHostName() + "\n");
                ta.append("Client " + clientNo + "'s IP Address is "
                        + inetAddress.getHostAddress() + "\n");

                // Create and start a new thread for the connection
                new Thread(new HandleAClient(socket, clientNo)).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // Define the thread class for handling new connection
    class HandleAClient implements Runnable {
        private Socket socket; // A connected socket
        private int clientNum;

        /** Construct a thread */
        public HandleAClient(Socket socket, int clientNum) {
            this.socket = socket;
            this.clientNum = clientNum;
        }

        /** Run a thread */
        public void run() {
            try {
                // Create data input and output streams
                DataInputStream inputFromClient = new DataInputStream(
                        socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(
                        socket.getOutputStream());
                String actionCommand;
                String username;
                String password;
                // Continuously serve the client

                while (true) {
                    actionCommand = inputFromClient.readUTF();
                    switch (actionCommand) {
                        case "LOGIN":
                            username = inputFromClient.readUTF();
                            password = inputFromClient.readUTF();
                            ta.append("Username: " + username + "\n");
                            ta.append("Password: " + password + "\n");
                            if (checkCredentials(username, password)) {
                                // if succeed
                                ta.append("Client No." + clientNo + " logs in" + "\n");

                                outputToClient.writeUTF("Login succeeds");
                                outputToClient.flush();
                                break;
                            } else {
                                outputToClient.writeUTF("Login fails");
                                outputToClient.flush();
                            }
                            break;
                        case "SIGNUP":
                            username = inputFromClient.readUTF();
                            password = inputFromClient.readUTF();
                            ta.append("New user signup\n");
                            ta.append("Username: " + username + "\n");
                            ta.append("Password: " + password + "\n");
                            SQLiteConnection.insertUser(username, password);
                            outputToClient.writeUTF("Signup succeeds");
                            outputToClient.flush();
                            break;
                        case "ADD_EVENT":
                            String eventName = inputFromClient.readUTF();
                            username = inputFromClient.readUTF();
                            String dayOfWeek = inputFromClient.readUTF();
                            String startTime = inputFromClient.readUTF();
                            String endTime = inputFromClient.readUTF();
                            String location = inputFromClient.readUTF();
                            int emergencyLevel = inputFromClient.readInt();
                            if (!isValid(eventName,username,dayOfWeek,startTime,endTime,location,emergencyLevel)){
                                outputToClient.writeUTF("Illegal event");
                                outputToClient.flush();
                            }else{
                                if (hasTimeConflict(eventName,username,dayOfWeek,startTime,endTime,location,emergencyLevel)){
                                    outputToClient.writeUTF("Time conflict");
                                    outputToClient.flush();
                                }else{
                                    if (!SQLiteConnection.insertEvent(eventName, username, dayOfWeek, startTime, endTime, location, emergencyLevel)){
                                        outputToClient.writeUTF("Add fails");
                                        outputToClient.flush();
                                    }else{
                                        outputToClient.writeUTF("Event added");
                                        outputToClient.flush();
                                    }
                                }
                            } 
                            break;
                        case "DELETE_EVENT":
                            username = inputFromClient.readUTF();
                            int eventId = inputFromClient.readInt();

                            if (!SQLiteConnection.dropEvent(username,eventId)){
                                outputToClient.writeUTF("Delete fails: No event found or No permission");
                                outputToClient.flush();
                            }else{
                                outputToClient.writeUTF("Delete succeeds");
                                outputToClient.flush();
                            }
                            break;
                        case "LOGOUT":
                            username = null;
                            password = null;
                            break;
                    }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean hasTimeConflict(String eventName, String username, String week, String startTime,
            String endTime, String location, int emergencyLevel) {
        return SQLiteConnection.isEventConflict(eventName,week,startTime,endTime,location,emergencyLevel);
    }

    public boolean isValid(String eventName, String username, String dayOfWeek, 
    String startTime, String endTime, String location, int emergencyLevel) {
        
        //check if the start time and end time is legal
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            LocalTime start = LocalTime.parse(startTime, timeFormatter);
            LocalTime end = LocalTime.parse(endTime, timeFormatter);
    
            return end.isAfter(start);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid time format");
            return false;
        }

    }

    public boolean checkCredentials(String username, String password) {
        return SQLiteConnection.checkLogin(username, password);
    }
    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static void main(String[] args) {
        SQLiteConnection.dropUserInfoTable();
        SQLiteConnection.createUserInfoTable();
        SQLiteConnection.createEventsTable();
        SQLiteConnection.insertUser("admin", "123456");
        MultiThreadServer mts = new MultiThreadServer();
        mts.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mts.setVisible(true);
    }

    
}