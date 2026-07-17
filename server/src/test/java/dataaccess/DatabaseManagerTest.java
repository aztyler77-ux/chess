package dataaccess;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseManagerTest {
// create chess db, open a connection, then check validity
    @Test
    public void connectionWorks() throws Exception {
        DatabaseManager.createDatabase();
        try (var connection = DatabaseManager.getConnection()) {
            assertNotNull(connection);
        }
    }
}