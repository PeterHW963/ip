/**
 * Handles the parsing of user input into commands and arguments that the program can understand.
 * The Parser class provides methods to interpret different types of tasks and extract relevant details.
 */

public class Parser {
    /**
     * Parses the user input to identify the command and extracts details relevant to the command.
     *
     * @param userInput The full input string provided by the user (without trailing or leading whiespaces).
     * @return An array of strings containing the parts of the user input necessary to create tasks.
     * @throws NetherException If the command is not recognized or the input format is incorrect.
     */
    public Command parse(String userInput) throws NetherException {
        String[] processedInput;
        String commandWord = extractCommandWord(userInput);

        switch (commandWord) {
        case "list":
            return new ListCommand();
        case "todo":
            processedInput = extractInputDetails(userInput, "todo");
            return new AddCommand(new TodoTask(processedInput[0]));
        case "deadline":
            processedInput = extractInputDetails(userInput, "deadline");
            return new AddCommand(new DeadlineTask(processedInput[0], processedInput[1]));
        case "event":
            processedInput = extractInputDetails(userInput, "event");
            return new AddCommand(new EventTask(processedInput[0], processedInput[1], processedInput[2]));
        case "mark":
            return new MarkDoneCommand(extractTaskNumber(userInput));
        case "unmark":
            return new MarkNotDoneCommand(extractTaskNumber(userInput));
        case "delete":
            return new DeleteCommand(extractTaskNumber(userInput));
        case "exit":
            return new ExitCommand();
        default:
            throw new NetherException("the command: '" + userInput + "' is not in our database.");
        }

    }

    /**
     * Extracts the command from the user's input string. The command is assumed to be the first word of the input.
     *
     * @param userInput The full input string provided by the user.
     * @return The command in lowercase (e.g., "todo", "deadline", or "event").
     */
    public String extractCommandWord(String userInput) {
        return userInput.split(" ", 2)[0].toLowerCase();
    }

    /**
     * Processes the user input into parts, making it easier to instantiate the respective tasks.
     * Splits the input based on the command and uses regex to identify task details.
     *
     * @param userInput The full input string provided by the user (without leading or trailing whitespaces).
     * @param taskType The type of task ("todo", "deadline", or "event").
     * @return A string array containing the task details to be instantiated by Nether class.
     * @throws NetherException If the input does not follow the expected format or required details are missing.
     */

    private String[] extractInputDetails(String userInput, String taskType) throws NetherException {
        String[] preprocessArray;
        String[] resultArray = new String[]{};

        switch (taskType) {
        case "todo":
            preprocessArray = userInput.split("(?i)todo ", 2);
            if (preprocessArray.length < 2 || preprocessArray[1].trim().isEmpty()) {
                throw new NetherException("the description of a todo cannot be empty.");
            }
            resultArray = new String[]{preprocessArray[1]};
            break;

        case "deadline":
            preprocessArray = userInput.split("(?i)deadline ", 2);
            if (preprocessArray.length < 2 || preprocessArray[1].trim().isEmpty()) {
                throw new NetherException("the description of a deadline cannot be empty.");
            }
            String[] deadlineParts = preprocessArray[1].split("/by ", 2);
            if (deadlineParts.length < 2 || deadlineParts[0].trim().isEmpty() || deadlineParts[1].trim().isEmpty()) {
                throw new NetherException("the description or date/time of a deadline cannot be empty.");
            }

            resultArray = new String[] {deadlineParts[0], deadlineParts[1]};
            break;

        case "event":
            preprocessArray = userInput.split("(?i)event ", 2);
            if (preprocessArray.length < 2 || preprocessArray[1].trim().isEmpty()) {
                throw new NetherException("the description of an event cannot be empty.");
            }
            String[] eventParts = preprocessArray[1].split("/from |/to ", 3);
            if (eventParts.length < 3 || eventParts[0].trim().isEmpty() || eventParts[1].trim().isEmpty()
                    || eventParts[2].trim().isEmpty()) {
                throw new NetherException(
                        "the description, start time, or end time of an event cannot be empty.");
            }

            resultArray = new String[]{eventParts[0], eventParts[1], eventParts[2]};
            break;
        default:
            throw new NetherException("the command: '" + userInput + "' is not in our database");
        }
        return resultArray;
    }

    /**
     * Returns the index/number of the task stated in the user input.
     * Useful for commands like mark/unmark.
     *
     * @param userInput The input string provided by the user.
     * @return The task number (index + 1) to be marked/unmarked if successfully parsed; -1 otherwise.
     */

    public int extractTaskNumber(String userInput) {
        try {
            String[] parts = userInput.split(" ");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return -1;
        }
    }
}
