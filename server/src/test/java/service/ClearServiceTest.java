package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.MemoryGameDAO;

import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearServiceTest {
    @Test
    public void clearSuccess() throws DataAccessException {
        // fake data
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        userDAO.createUser(new UserData("tyler", "password", "email"));
        authDAO.createAuth(new AuthData("token", "tyler"));
        int gameID = gameDAO.createGame("test game");

        // clear
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        clearService.clear();

        // check
        assertNull(userDAO.getUser("tyler"));
        assertNull(authDAO.getAuth("token"));
        assertNull(gameDAO.getGame(gameID));
    }
}