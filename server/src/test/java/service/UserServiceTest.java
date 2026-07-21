package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;

import model.AuthData;
import model.UserData;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import service.exception.AlreadyTakenException;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceTest {
    @Test
    public void registerSuccess() throws DataAccessException, BadRequestException, AlreadyTakenException {
        // fake data
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        UserService userService = new UserService(userDAO, authDAO);
        RegisterRequest request = new RegisterRequest("player", "password", "email");

        // register
        RegisterResult result = userService.register(request);

        // check
        assertEquals("player", result.username());
        assertNotNull(result.authToken());

        UserData storedUser = userDAO.getUser("player");
        assertNotNull(storedUser);
        assertNotEquals("password", storedUser.password());
        assertTrue(BCrypt.checkpw("password", storedUser.password()));

        assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    public void registerFail() throws DataAccessException {
        // fake data
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        userDAO.createUser(new UserData("player", "password", "email"));
        UserService userService = new UserService(userDAO, authDAO);
        RegisterRequest request = new RegisterRequest("player", "password", "email");

        // failure = already registered
        AlreadyTakenException error = assertThrows(AlreadyTakenException.class, () -> userService.register(request));

        // check
        assertEquals("Error: already taken", error.getMessage());
    }

    @Test
    public void loginSuccess() throws DataAccessException, BadRequestException, UnauthorizedException {
        // fake data
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();

        String hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
        userDAO.createUser(new UserData("player", hashedPassword, "email"));

        UserService userService = new UserService(userDAO, authDAO);
        LoginRequest request = new LoginRequest("player", "password");

        // login
        LoginResult result = userService.login(request);

        // check
        assertEquals("player", result.username());
        assertNotNull(result.authToken());
        assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    public void loginFail() throws DataAccessException {
        // fake data
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();

        String hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
        userDAO.createUser(new UserData("player", hashedPassword, "email"));

        UserService userService = new UserService(userDAO, authDAO);

        // failure = wrong pass
        LoginRequest request = new LoginRequest("player", "wrong password");
        UnauthorizedException error = assertThrows(UnauthorizedException.class, () -> userService.login(request));

        // check
        assertEquals("Error: unauthorized", error.getMessage());
    }

    @Test
    public void logoutSuccess() throws DataAccessException, UnauthorizedException {
        // fake data
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token", "player"));
        UserService userService = new UserService(userDAO, authDAO);
        LogoutRequest request = new LogoutRequest("token");

        // logout
        userService.logout(request);

        // check
        assertNull(authDAO.getAuth("token"));
    }

    @Test
    public void logoutFail() throws DataAccessException {
        // fake data
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData("token", "player"));
        UserService userService = new UserService(userDAO, authDAO);

        // failure = token doesn't exist
        LogoutRequest request = new LogoutRequest("wrong token");
        UnauthorizedException error = assertThrows(UnauthorizedException.class, () -> userService.logout(request));

        // check
        assertEquals("Error: unauthorized", error.getMessage());
        assertNotNull(authDAO.getAuth("token"));
    }
}