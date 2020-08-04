package thesis.testing.utils;

import java.util.List;

/**
 * Contains parsed command and additional parameters for Archiver execution
 */
public class CommandContainer {

    // command to be executed by Archiver
    private Command command;

    //additional parameters, differing based on command
    private List<String> params;

    public CommandContainer(Command cmnd, List<String> prms){
        command = cmnd;
        params = prms;
    }

    public Command getCommand() {
        return command;
    }

    public List<String> getParams() {
        return params;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
