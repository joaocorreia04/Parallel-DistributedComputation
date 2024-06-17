import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.simple.parser.ParseException;

public class Server {
    private int port;
    public static Database db;

    public Server(int port, String dbFile) throws IOException, ParseException {
        this.port = port;
        Server.db = new Database(dbFile);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                Thread.ofVirtual().start(() -> new ClientHandler(socket, db).run());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Server <port> <dbFile>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String dbFile = args[1];
        try {
            Server server = new Server(port, dbFile);
            server.start();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
