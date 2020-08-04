package thesis.testing.system.archivingMocks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import thesis.testing.system.oldbMocks.DataPoint;
import thesis.testing.utils.ArchivingException;
import thesis.testing.utils.Command;
import thesis.testing.utils.CommandContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * CLI application Mockito demo used for storage, retrieval and searching of data from the archiving API
 */
public class Archiver {

    // mock of archiver, to which method calls to Archiver are delegated to
    private Archiver archiverMock;
    private ArchivingApiClient client;

    public Archiver() throws ArchivingException {
        archiverMock = Mockito.mock(Archiver.class);
        client = new ArchivingApiClient();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                System.out.println("Archiver usage");
                System.out.println("------------------------------------------");
                System.out.println("archiver store <dp_name> <dp_value> <dp_valid>              stores data point with passed parameters to storage and displays its packet id");
                System.out.println("archiver get <packet_id> <packet_id> ...                    displays data inside data packets specified with their ids");
                System.out.println("archiver search ( pointId:<dp_name> | date:<capture_date> ) displays packet ids containing data points. Can be based on data point name(format: dp_<point number>) or by capture date(format: yyyy-MM-dd) ");
                System.out.println("archiver help                                               displays this help message");
                System.out.println();
                System.out.println();
                return null;
            }
        }).when(archiverMock).displayHelp();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String[] args = (String[]) invocation.getArguments()[0];
                if (args[0].equals("help")){
                    displayHelp();
                    return null;
                }
                try {
                    Command command = Command.valueOf(args[0].toUpperCase());
                    List<String> params = Arrays.asList(Arrays.copyOfRange(args,1,args.length));
                    return new CommandContainer(command,params);
                }catch (IllegalArgumentException e){
                    throw new ArchivingException("Illegal command.",e);
                }
            }
        }).when(archiverMock).parseCommand(Matchers.any(String[].class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CommandContainer commandContainer = (CommandContainer) invocation.getArguments()[0];
                if (commandContainer == null) return null;
                switch (commandContainer.getCommand()){
                    case STORE:
                        if (commandContainer.getParams().size() == 3){
                            List<String> params = commandContainer.getParams();
                            try{
                                int value = Integer.parseInt(params.get(1));
                                boolean valid = Boolean.parseBoolean(params.get(2));
                                List<UUID> packetId = storeData(Arrays.asList(new DataPoint[]{new DataPoint(params.get(0),value,valid)}));
                                System.out.println(String.format("Stored data packet with id: %s",packetId.get(0).toString()));
                            }catch (Exception e){
                                throw new ArchivingException(e);
                            }
                            return null;
                        }
                        displayHelp();
                        throw new ArchivingException("Invalid number of parameters for store command.");
                    case GET:
                        List<UUID> packetIdsGet = new ArrayList<>();
                        for (String id: commandContainer.getParams()){
                            try{
                                packetIdsGet.add(UUID.fromString(id));
                            }catch (IllegalArgumentException e){
                             throw new ArchivingException("Provided packet id is invalid.",e);
                            }
                        }
                        List<DataPacket> dataPackets = displayData(packetIdsGet);
                        if (dataPackets.isEmpty()){
                            System.out.println("No such packets");
                            return null;
                        }
                        for (DataPacket dataPacket: dataPackets){
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            String jsonPacket = gson.toJson(dataPacket);
                            System.out.println(jsonPacket);
                        }
                        break;
                    case SEARCH:
                        if (commandContainer.getParams().get(0).matches("pointId:dp_\\d+") || commandContainer.getParams().get(0).matches("date:\\d{4}-\\d{2}-\\d{2}")) {
                            List<UUID> packetIdsSearch = queryDataPacketSearch(commandContainer.getParams().get(0));
                            for (UUID packetId:packetIdsSearch){
                                System.out.println(packetId.toString());
                            }
                            return null;
                        }
                        displayHelp();
                        throw new ArchivingException("invalid search query.");
                    case DELETE:
                        System.out.println("Deletion of data is not supported.");
                        break;
                }
                return null;
            }
        }).when(archiverMock).executeCommand(Matchers.any(CommandContainer.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return client.storeData((List<DataPoint>) invocation.getArguments()[0]);
            }
        }).when(archiverMock).storeData(Matchers.anyListOf(DataPoint.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return client.getData((List<UUID>) invocation.getArguments()[0]);
            }
        }).when(archiverMock).displayData(Matchers.anyListOf(UUID.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return client.searchData((String) invocation.getArguments()[0]);
            }
        }).when(archiverMock).queryDataPacketSearch(Matchers.anyString());

    }

    public static void main(String[] args) throws ArchivingException {
        Archiver archiver = new Archiver();
        archiver.executeCommand(archiver.parseCommand(args));
    }
    /**
     * Method simulates parsing of arguments given by user
     * @param args arguments to be parsed
     * @return CommandContainer object containing command and individual parsed parameters for it
     * @throws ArchivingException if there was a problem parsing command
     */
    public CommandContainer parseCommand(String[] args) throws ArchivingException {
        return archiverMock.parseCommand(args);
    }

    /**
     * Executes command based on passed parameteres
     * @param commandContainer contains command and additional parameters needed for execution
     * @throws ArchivingException if there is a problem with command execution
     */
    public void executeCommand(CommandContainer commandContainer) throws ArchivingException {
        archiverMock.executeCommand(commandContainer);
    }
    /**
     * Handles execution od GET command for multiple dataPackets and displays their data.
     * @param packetIds of dataPackets to be displayed
     * @return list of dataPackets
     */
    public List<DataPacket> displayData(List<UUID> packetIds){
        return archiverMock.displayData(packetIds);
    }

    /**
     * Handles execution of QUERY command.
     * @param query based on which packets are downloaded
     * @return list of packet UUID's matching the given query
     */
    public List<UUID> queryDataPacketSearch(String query){
        return archiverMock.queryDataPacketSearch(query);
    }

    /**
     * Handles execution of STORE command for multiple data points
     * @param dataPoints to be stored
     * @return list of store data packets ids
     */
    public List<UUID> storeData(List<DataPoint> dataPoints){
        return archiverMock.storeData(dataPoints);
    }

    /**
     * Displays help message for archiver usage
     */
    public void displayHelp(){
        archiverMock.displayHelp();
    }
}
