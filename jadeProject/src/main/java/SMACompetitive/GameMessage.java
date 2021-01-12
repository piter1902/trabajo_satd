package main.java.SMACompetitive;


import java.io.Serializable;

public class GameMessage implements Serializable {

    private final Constants.GAME_MESSAGE message;

    private String content;

    public GameMessage(Constants.GAME_MESSAGE message) {
        this.message = message;
        content = null;
    }

    public GameMessage(Constants.GAME_MESSAGE message, String content) {
        this.message = message;
        this.content = content;
    }

    public Constants.GAME_MESSAGE getMessage() {
        return message;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
