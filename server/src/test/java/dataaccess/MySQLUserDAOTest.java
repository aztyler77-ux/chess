package dataaccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLUserDAOTest {
    private MySQLUserDAO userDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseSetup.createTables();
        userDAO = new MySQLUserDAO();
        userDAO.clear();
    }

    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("player", "password", "email");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("player");
        assertEquals(user, result);
    }

    @Test
    public void createUserFail() throws DataAccessException {
        UserData user = new UserData("player", "password", "email");
        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("player", "password", "email");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("player");
        assertEquals("player", result.username());
        assertEquals("password", result.password());
        assertEquals("email", result.email());
    }

    @Test
    public void getUserFail() throws DataAccessException {
        UserData result = userDAO.getUser("not a player");
        assertNull(result);
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("player", "password", "email"));
        userDAO.clear();

        assertNull(userDAO.getUser("player"));
    }
}