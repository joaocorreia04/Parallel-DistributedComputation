import java.nio.channels.SocketChannel;

public class Player {

    private final String username;
    private final String password;
    private final String token;
    private Long elo;
    private SocketChannel socket;
    private Database db = Server.db;

    Player(String username, String password, String token, Long elo, SocketChannel socket) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.elo = elo;
        this.socket = socket;
    }

    public String getUsername() {
        return this.username;
    }

    public Long getElo() {
        return this.elo;
    }

    public void updateElo(int value) {
        db.updateElo(this, value);
    }

    public SocketChannel getSocket() {
        return this.socket;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public String getToken() {
        return token;
    }

    public boolean equals(Player player) {
        return this.username.equals(player.getUsername());
    }

    public boolean equalElo(Player player) {
        return this.elo == player.getElo();
    }
}