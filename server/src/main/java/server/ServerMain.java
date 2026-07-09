package server;

public class ServerMain {
    public static void main(String[] args) {
        // launch javalin server
        Server server = new Server();
        server.run(8080);
    }
}