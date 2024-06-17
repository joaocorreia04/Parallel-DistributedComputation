import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientHandler extends Thread {
    private static final ConcurrentLinkedQueue<ClientHandler> simpleQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<ClientHandler> rankedQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<String, ClientHandler> loggedInUsers = new ConcurrentHashMap<>();
    private static final Lock queueLock = new ReentrantLock();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Database db;
    private ClientHandler opponent;
    private String username;
    private String userChoice;
    private String opponentChoice;
    private CountDownLatch latch;
    public Player player;

    public ClientHandler(Socket socket, Database db) {
        this.socket = socket;
        this.db = db;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String input;
            while ((input = in.readLine()) != null) {
                JSONObject request = (JSONObject) new JSONParser().parse(input);
                String action = (String) request.get("action");

                if ("register".equals(action)) {
                    handleRegister(request);
                } else if ("login".equals(action)) {
                    handleLogin(request);
                } else if ("play".equals(action)) {
                    handlePlay(request);
                } else if ("queue".equals(action)) {
                    addPlayerToQueue();
                } else if ("exit".equals(action)) {
                    sendJSONResponse("Exiting...");
                    break;
                } else {
                    sendJSONResponse("Invalid option, please try again.");
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            logout();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRegister(JSONObject request) throws ParseException {
        String username = (String) request.get("username");
        if (db.userExists(username)) {
            sendJSONResponse("Username already exists");
            return;
        }
        sendJSONResponse("Username accepted");
        try {
            String requestString = in.readLine().trim();
            JSONObject requestObject = (JSONObject) new JSONParser().parse(requestString);
            String password = (String) requestObject.get("password");
            boolean success = db.register(username, password);
            if (success) {
                sendJSONResponse("Registration successful");
            } else {
                sendJSONResponse("Registration failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(JSONObject request) throws ParseException {
        String username = (String) request.get("username");

        if (!db.userExists(username)) {
            sendJSONResponse("User does not exist");
            return;
        }

        if (loggedInUsers.containsKey(username)) {
            sendJSONResponse("User already logged in");
            return;
        }

        sendJSONResponse("Username exists");
        try {
            String requestString = in.readLine().trim();
            JSONObject requestObject = (JSONObject) new JSONParser().parse(requestString);
            String password = (String) requestObject.get("password");
            if (db.checkPassword(username, password)) {
                this.username = username;
                player = db.login(username, password, socket.getChannel());
                loggedInUsers.put(username, this);
                JSONObject response = new JSONObject();
                response.put("message", "Login successful");
                response.put("elo", player.getElo().toString());
                sendJSONResponse(response);
                addPlayerToQueue();
            } else {
                sendJSONResponse("Invalid password");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPlayerToQueue() throws ParseException {
        try {
            // Read the user's response
            String matchmakingString = in.readLine().trim();
            JSONObject matchmakingObject = (JSONObject) new JSONParser().parse(matchmakingString);
            String matchmakingType = (String) matchmakingObject.get("matchmakingType");
            System.out.println("Matchmaking: " + matchmakingType);
            boolean isRanked = matchmakingType.equals("2");

            queueLock.lock();
            try {
                if (isRanked) {
                    // Get the player's ELO
                    Long playerElo = player.getElo();
                    boolean matched = false;

                    // Iterate through the rankedQueue to find a suitable match
                    for (ClientHandler ch : rankedQueue) {
                        Long opponentElo = ch.player.getElo();
                        if (Math.abs(playerElo - opponentElo) <= 250) {
                            opponent = ch;
                            rankedQueue.remove(ch);
                            matched = true;
                            break;
                        }
                    }

                    if (!matched) {
                        rankedQueue.add(this);
                        sendJSONResponse("Waiting for an opponent in ranked queue...");
                    } else {
                        opponent.opponent = this;
                        this.latch = new CountDownLatch(2);
                        opponent.latch = this.latch;
                        sendJSONResponse("Matched with " + opponent.username + " in ranked queue");
                        opponent.sendJSONResponse("Matched with " + this.username + " in ranked queue");
                    }
                } else {
                    if (simpleQueue.isEmpty()) {
                        simpleQueue.add(this);
                        sendJSONResponse("Waiting for an opponent in simple queue...");
                    } else {
                        opponent = simpleQueue.poll();
                        opponent.opponent = this;
                        this.latch = new CountDownLatch(2);
                        opponent.latch = this.latch;
                        sendJSONResponse("Matched with " + opponent.username + " in simple queue");
                        opponent.sendJSONResponse("Matched with " + this.username + " in simple queue");
                    }
                }
            } finally {
                queueLock.unlock();
            }

            System.out.println("Player added to queue, type: " + (isRanked ? "ranked" : "simple"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private void handlePlay(JSONObject request) throws ParseException {
        userChoice = (String) request.get("choice");
        String matchmakingType = (String) request.get("matchmakingType");
        if (opponent == null) {
            sendJSONResponse("No opponent found");
            return;
        }

        latch.countDown();

        try {
            latch.await(); // Wait for both players to make their choices
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        opponentChoice = opponent.userChoice; // Get the opponent's choice from the opponent's instance
        determineResult(matchmakingType);
    }

    private synchronized void determineResult(String matchmakingType) {
        if (userChoice == null || opponentChoice == null) {
            return; // Wait for both players to make their choices
        }

        String result;
        if (userChoice.equals(opponentChoice)) {
            result = "It's a tie!";
        } else if ((userChoice.equals("rock") && opponentChoice.equals("scissors")) ||
                (userChoice.equals("scissors") && opponentChoice.equals("paper")) ||
                (userChoice.equals("paper") && opponentChoice.equals("rock"))) {
            result = "You win!";
        } else {
            result = "You lose!";
        }

        JSONObject response = new JSONObject();
        response.put("opponentChoice", opponentChoice);
        response.put("result", result);
        sendJSONResponse(response);

        JSONObject opponentResponse = new JSONObject();
        opponentResponse.put("opponentChoice", userChoice);
        opponentResponse.put("result", result.equals("You win!") ? "You lose!" : result.equals("You lose!") ? "You win!" : "It's a tie!");
        opponent.sendJSONResponse(opponentResponse);

        // change user's elo if mode is ranked
        if (matchmakingType.equals("2")) {
            if (result.equals("You win!")) {
                player.updateElo(100);
            }
            else if (result.equals("You lose!")) {
                player.updateElo(-100);
            }
        }

        //handlePostGameOptions();
    }

    private void handlePostGameOptions() {
        sendJSONResponse("Game over. Would you like to join the queue again or exit? (queue/exit)");
        opponent.sendJSONResponse("Game over. Would you like to join the queue again or exit? (queue/exit)");
    }

    private void sendJSONResponse(String message) {
        JSONObject response = new JSONObject();
        response.put("message", message);
        out.println(response.toJSONString());
    }

    private void sendJSONResponse(JSONObject response) {
        out.println(response.toJSONString());
    }

    private void logout() {
        if (username != null) {
            loggedInUsers.remove(username);
        }
    }
}
