package game.logic.player;

import game.gui.utils.UIUtils;
import game.logic.exception.DataLoadingException;
import game.logic.exception.GameException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class PlayersManager implements Serializable {
    private static final String PLAYERS_DATA_FILE_LOCATION;
    /**
     * All played and their best scores.
     * If null - failed to load data, thus data won't be saved
     */
    private Map<String, Integer> players;
    private static PlayersManager instance;

    static {
        instance = null;
    }

    static {
        PLAYERS_DATA_FILE_LOCATION = "/data/players.bin";
    }

    public static PlayersManager getInstance() {
        if (instance == null) {
            instance = new PlayersManager();
        }
        return instance;
    }

    public boolean isNewPlayer(String nickname) {
        return !players.containsKey(nickname);
    }

    private PlayersManager() {
        try {
            loadPlayers();
        } catch (DataLoadingException e) {
            UIUtils.processException(null, e);
        }
    }

    private PlayersManager(boolean flag) {
        players = new HashMap<>();
    }

    public void addPlayer(String nickname) {
        if (!players.containsKey(nickname)) {
            players.put(nickname, 0);
        }
    }

    public String getRandomPlayer() {
        if (players.isEmpty()) {
            throw new GameException("There is no player registered in the game");
        }
        return players.entrySet().stream().findAny().get().getKey();
    }

    public String[] getScoreBoard() {
        List<String> scoreBoard = players.entrySet().stream().toList()
                .stream().sorted((a, b) -> b.getValue() - a.getValue())
                .map(e -> e.getKey() + " - " + e.getValue()).toList();
        String[] array = new String[scoreBoard.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = scoreBoard.get(i);
        }
        return array;
    }

    public void updatePlayer(String nickname, int score) {
        if (!players.containsKey(nickname)) {
            throw new GameException("Cannot update non-existing player");
        } else {
            if (players.get(nickname) < score) {
                System.out.println("Player " + nickname + " has beat his last best score");
                players.replace(nickname, score);
            }
        }
    }

    // save players map via serialization
    public void savePlayers() {
        try {
            var os = getClass().getResource(PLAYERS_DATA_FILE_LOCATION);
            assert os != null;
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(os.toURI())));
            oos.writeObject(players);
            oos.flush();
            oos.close();
            System.out.println("Players are saved to file");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // load players map via serialization
    private void loadPlayers() throws DataLoadingException {
        try {
            var is = getClass().getResourceAsStream(PLAYERS_DATA_FILE_LOCATION);
            ObjectInputStream ois = new ObjectInputStream(is);
            players = (Map<String, Integer>) ois.readObject();
            System.out.println("Players are loaded from file: ");
            for (var entry : players.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
            if (players.isEmpty()) {
                System.out.println("<empty>");
            }
            ois.close();
        } catch (Exception e) {
            players = null;
            System.out.println("Failed to load players data. Cause: " + e.getClass().getCanonicalName()
                    + " message: " + e.getMessage());
            throw new DataLoadingException("Failed to load players data. Your best score won't be saved");
        }
    }

    // run this main if Failed to load players data Error occurs
    public static void main(String[] args) {
        PlayersManager manager = new PlayersManager(true);
        manager.addPlayer("vova");
        manager.updatePlayer("vova", 2110);
        manager.addPlayer("bob");
        manager.updatePlayer("bob", 980);
        manager.addPlayer("san4ez");
        manager.updatePlayer("san4ez", 125);
        manager.savePlayers();
    }
}
