package dataaccess;

import java.sql.SQLException;

public class DatabaseSetup {
    public static void createTables() throws DataAccessException {
        DatabaseManager.createDatabase();

        String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(256) NOT NULL, " +
                "password VARCHAR(256) NOT NULL, " +
                "email VARCHAR(256) NOT NULL, " +
                "PRIMARY KEY (username))";

        String authTable = "CREATE TABLE IF NOT EXISTS auth (" +
                "authToken VARCHAR(256) NOT NULL, " +
                "username VARCHAR(256) NOT NULL, " +
                "PRIMARY KEY (authToken))";

        String gamesTable = "CREATE TABLE IF NOT EXISTS games (" +
                "gameID INT NOT NULL AUTO_INCREMENT, " +
                "whiteUsername VARCHAR(256), " +
                "blackUsername VARCHAR(256), " +
                "gameName VARCHAR(256) NOT NULL, " +
                "game LONGTEXT NOT NULL, " +
                "PRIMARY KEY (gameID))";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate(usersTable);
            statement.executeUpdate(authTable);
            statement.executeUpdate(gamesTable);
        } catch (SQLException e) {
            throw new DataAccessException("Could not create tables", e);
        }
    }
}