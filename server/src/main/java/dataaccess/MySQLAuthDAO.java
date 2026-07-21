package dataaccess;
import model.AuthData;
import java.sql.SQLException;

public class MySQLAuthDAO implements AuthDAO {
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not create auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT * FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, authToken);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    String username = result.getString("username");
                    return new AuthData(authToken, username);
                }
            }
        } catch (SQLException e) {throw new DataAccessException("Could not get auth", e);}
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (SQLException e) {throw new DataAccessException("Could not delete auth", e);}
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {throw new DataAccessException("Could not clear auth", e);}
    }
}