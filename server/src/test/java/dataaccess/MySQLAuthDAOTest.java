package dataaccess;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLAuthDAOTest {
    private MySQLAuthDAO authDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        DatabaseSetup.createTables();
        authDAO = new MySQLAuthDAO();
        authDAO.clear();
    }

    @Test
    public void createAuthSuccess() throws DataAccessException {
        // fake auth
        AuthData auth = new AuthData("token", "player");

        authDAO.createAuth(auth);

        // check
        AuthData result = authDAO.getAuth("token");
        assertEquals(auth, result);
    }

    @Test
    public void createAuthFail() throws DataAccessException {
        AuthData auth = new AuthData("token", "player");
        authDAO.createAuth(auth);

        // failure = token already exists
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token", "player");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("token");

        assertEquals("token", result.authToken());
        assertEquals("player", result.username());
    }

    @Test
    public void getAuthFail() throws DataAccessException {
        // token doesnt exist
        AuthData result = authDAO.getAuth("wrong token");

        assertNull(result);
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token", "player"));

        authDAO.deleteAuth("token");

        assertNull(authDAO.getAuth("token"));
    }

    @Test
    public void deleteAuthFail() throws DataAccessException {
        authDAO.createAuth(new AuthData("token", "player"));

        // tries deleting a different token
        authDAO.deleteAuth("wrong token");

        assertNotNull(authDAO.getAuth("token"));
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "player1"));
        authDAO.createAuth(new AuthData("token2", "player2"));

        authDAO.clear();

        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
    }
}