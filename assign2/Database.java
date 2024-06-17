import java.io.*;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class Database {
    private final File file;
    private final JSONObject database;
    private final SecureRandom random = new SecureRandom();

    public Database(String filename) throws IOException, ParseException {
        this.file = new File(filename);

        if (!file.exists()) {
            createEmptyFile();
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }

        this.database = (JSONObject) new JSONParser().parse(content.toString());
    }

    private void createEmptyFile() throws IOException {
        JSONObject obj = new JSONObject();
        obj.put("database", new JSONArray());
        try (FileWriter fileWriter = new FileWriter(this.file)) {
            fileWriter.write(obj.toJSONString());
        }
    }

    public void backup() {
        try (FileWriter writer = new FileWriter(this.file)) {
            writer.write(this.database.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateToken(String username, String password) {
        String input = username + password + random.nextLong();
        return BCrypt.hashpw(input, BCrypt.gensalt());
    }

    public synchronized Player login(String username, String password, SocketChannel socket) {
        JSONArray databaseArr = (JSONArray) this.database.get("database");
        for (Object obj : databaseArr) {
            JSONObject user = (JSONObject) obj;
            String storedUsername = (String) user.get("username");
            String storedPassword = (String) user.get("password");

            if (storedUsername.equals(username) && BCrypt.checkpw(password, storedPassword)) {
                String token = generateToken(username, storedPassword);
                user.put("token", token);
                backup();  // Save changes to the file
                return new Player(username, password, token, ((Number) user.get("elo")).longValue(), socket);
            }
        }
        return null;
    }

    public synchronized boolean checkPassword(String username, String password) {
        JSONArray databaseArr = (JSONArray) this.database.get("database");
        for (Object obj : databaseArr) {
            JSONObject user = (JSONObject) obj;
            String storedUsername = (String) user.get("username");
            String storedPassword = (String) user.get("password");

            //System.out.println("Comparing: username=" + username + ", password=" + password + " with storedUsername=" + storedUsername + ", storedPassword=" + storedPassword);

            if (storedUsername.equals(username) && BCrypt.checkpw(password, storedPassword)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean userExists(String username) {
        JSONArray databaseArr = (JSONArray) this.database.get("database");
        for (Object obj : databaseArr) {
            JSONObject user = (JSONObject) obj;
            if (user.get("username").equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean register(String username, String password) {
        JSONArray databaseArr = (JSONArray) this.database.get("database");

        for (Object obj : databaseArr) {
            JSONObject user = (JSONObject) obj;
            if (user.get("username").equals(username)) {
                return false;
            }
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String token = generateToken(username, hashedPassword);

        JSONObject newUser = new JSONObject();
        newUser.put("username", username);
        newUser.put("password", hashedPassword);
        newUser.put("elo", 1000L);  // Default ELO score
        newUser.put("token", token);

        databaseArr.add(newUser);
        backup();  // Save changes to the file
        return true;
    }

    public synchronized void updateElo(Player player, int value) {
        JSONArray databaseArr = (JSONArray) this.database.get("database");

        for (Object obj : databaseArr) {
            JSONObject user = (JSONObject) obj;
            if (user.get("username").equals(player.getUsername())) {
                Long elo = ((Number) user.get("elo")).longValue() + value;
                user.put("elo", elo);
                backup();  // Save changes to the file
                return;
            }
        }
    }

    public synchronized void invalidateToken(Player player) {
        JSONArray databaseArr = (JSONArray) this.database.get("database");
        for (Object obj : databaseArr) {
            JSONObject user = (JSONObject) obj;
            if (user.get("username").equals(player.getUsername())) {
                user.put("token", "");
                backup();  // Save changes to the file
                return;
            }
        }
    }

    public synchronized void resetTokens() {
        JSONArray databaseArray = (JSONArray) this.database.get("database");
        for (Object obj : databaseArray) {
            JSONObject user = (JSONObject) obj;
            user.put("token", "");
        }
        backup();  // Save changes to the file
    }

    public String[] getLeaderboard() {
        String[] leaderboard = new String[5];
        JSONArray databaseArr = (JSONArray) this.database.get("database");
        List<JSONObject> userList = new ArrayList<>();
        for (Object obj : databaseArr) {
            userList.add((JSONObject) obj);
        }
        userList.sort((a, b) -> Long.compare(((Number) b.get("elo")).longValue(), ((Number) a.get("elo")).longValue()));
        for (int i = 0; i < 5 && i < userList.size(); i++) {
            JSONObject user = userList.get(i);
            String username = (String) user.get("username");
            Long elo = ((Number) user.get("elo")).longValue();
            leaderboard[i] = username + " - " + elo;
        }
        return leaderboard;
    }
}
