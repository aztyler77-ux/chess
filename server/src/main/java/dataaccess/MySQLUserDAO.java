package dataaccess;
import model.UserData;
import java.sql.SQLException;

public class MySQLUserDAO implements UserDAO {
    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    String password = result.getString("password");
                    String email = result.getString("email");
                    return new UserData(username, password, email);
                }
            }
        } catch (SQLException e) {throw new DataAccessException("Could not get user", e);}
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {throw new DataAccessException("Could not clear users", e);}
    }
}