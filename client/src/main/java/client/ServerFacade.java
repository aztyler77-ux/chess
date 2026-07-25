package client;

public class ServerFacade {
    private final String serverUrl;
    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }
}