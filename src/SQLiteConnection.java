import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class SQLiteConnection {

    private static Connection connect() {
        String url = "jdbc:sqlite:mydatabase.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createUserInfoTable() {
        String sql = "CREATE TABLE IF NOT EXISTS UserInfo (\n"
                + " username text PRIMARY KEY,\n"
                + " password text NOT NULL\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("UserInfo table created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertUser(String username, String password) {
        String sql = "INSERT INTO UserInfo(username, password) VALUES(?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            System.out.println("User inserted: " + username);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static boolean checkLogin(String username, String password) {
        String sql = "SELECT password FROM UserInfo WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs  = pstmt.executeQuery();

            // Only expecting a single result
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    public static void dropUserInfoTable() {
        String sql = "DROP TABLE IF EXISTS UserInfo";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("UserInfo table dropped.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void dropEventsTable() {
        String sql = "DROP TABLE IF EXISTS Events";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Events table dropped.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void createEventsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Events (\n"
                + " eventID integer PRIMARY KEY AUTOINCREMENT,\n"
                + " eventName text NOT NULL,\n"
                + " hostName text NOT NULL,\n"
                + " week text NOT NULL,\n"
                + " startTime text,\n"
                + " endTime text,\n"
                + " location text,\n"
                + " emergencyLevel integer\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Events table created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static boolean isEventConflict(String eventName, String week, String startTime, String endTime, String location, int emergencyLevel) {
        String sql = "SELECT * FROM Events WHERE week = ? AND location = ? AND NOT (endTime <= ? OR startTime >= ?)";
    
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, week);
            pstmt.setString(2, location);
            pstmt.setString(3, startTime);
            pstmt.setString(4, endTime);
    
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Conflict exists
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false; // No conflict
    }
    
    public static boolean insertEvent(String eventName, String hostName, String week, String startTime, String endTime, String location, int emergencyLevel) {
        String sql = "INSERT INTO Events(eventName, hostName, week, startTime, endTime, location, emergencyLevel) VALUES(?,?,?,?,?,?,?)";
    
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, eventName);
            pstmt.setString(2, hostName);
            pstmt.setString(3, week);
            pstmt.setString(4, startTime);
            pstmt.setString(5, endTime);
            pstmt.setString(6, location);
            pstmt.setInt(7, emergencyLevel);
            pstmt.executeUpdate();
            System.out.println("Event inserted: " + eventName);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static Vector<Vector<Object>> getAllEvents() {
        Vector<Vector<Object>> events = new Vector<>();
        String sql = "SELECT * FROM Events";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("eventID"));
                row.add(rs.getString("eventName"));
                row.add(rs.getString("hostName"));
                row.add(rs.getString("week"));
                row.add(rs.getString("startTime"));
                row.add(rs.getString("endTime"));
                row.add(rs.getString("location"));
                row.add(rs.getInt("emergencyLevel"));

                events.add(row);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return events;
    }

    public static boolean dropEvent(String username, int eventId) {
        String sqlCheckHost = "SELECT hostname FROM Events WHERE eventID = ?";
        String sqlDelete = "DELETE FROM Events WHERE eventID = ?";

        try (Connection conn = connect();
            PreparedStatement pstmtCheckHost = conn.prepareStatement(sqlCheckHost);
            PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {

            boolean isAllowedToDelete = false;

            if ("admin".equals(username)) {
                isAllowedToDelete = true;
            } else {
                pstmtCheckHost.setInt(1, eventId);
                ResultSet rs = pstmtCheckHost.executeQuery();
                if (rs.next()) {
                    String hostName = rs.getString("hostname");
                    if (username.equals(hostName)) {
                        isAllowedToDelete = true;
                    }
                }
            }

            if (isAllowedToDelete) {
                pstmtDelete.setInt(1, eventId);
                pstmtDelete.executeUpdate();
                System.out.println("Event deleted successfully.");
                return true;
            } else {
                System.out.println("Can't delete event");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error occurred: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        dropUserInfoTable();
        dropEventsTable();
        createUserInfoTable();
        insertUser("Alice", "alice123");
        insertUser("Bob", "bob123");
        createEventsTable();
        insertEvent("meeting", "admin", "M", "10:00", "11:00", "room100", 0);
    }

    
}
