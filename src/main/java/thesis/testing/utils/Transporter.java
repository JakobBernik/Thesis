package thesis.testing.utils;

/**
 * Used for serialization over http for archiving and oldb API.
 */
public class Transporter {
    private Command action;
    private String data;

    public Transporter(Command command, String jsonData) {
        action = command;
        data = jsonData;
    }

    public Command getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    public void setAction(Command action) {
        this.action = action;
    }

    public void setData(String data) {
        this.data = data;
    }

}
