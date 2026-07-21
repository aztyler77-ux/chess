package dataaccess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseManagerTest {
    // create chess db, open a connection, then check validity
    @Test
    public void connectionWorks() throws Exception {
        DatabaseManager.createDatabase();

        try (var connection = DatabaseManager.getConnection()) {
            assertNotNull(connection);
        }
    }

    @Test
    public void tablesWork() throws Exception {
        // Run table setup code and check if users / auth / games exist
        DatabaseSetup.createTables();
        try (var connection = DatabaseManager.getConnection(); var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SHOW TABLES LIKE 'users'")) {assertTrue(result.next());}
            try (var result = statement.executeQuery("SHOW TABLES LIKE 'auth'")) {assertTrue(result.next());}
            try (var result = statement.executeQuery("SHOW TABLES LIKE 'games'")) {assertTrue(result.next());}
        }
    }
}