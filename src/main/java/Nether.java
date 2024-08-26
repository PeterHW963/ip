/**
 * Produces greetings for users and initializes chatbot.
 */

import java.sql.Array;
import java.util.Scanner;
import java.util.ArrayList;

public class Nether {
    private static final String STORAGE_FILE_PATH = "./data/nether.txt";

    private static final String EXIT_COMMAND = "bye";

    private static final String LIST_COMMAND = "list";
    private static final String MARK_DONE_COMMAND = "mark";
    private static final String MARK_NOT_DONE_COMMAND = "unmark";
    private static final String DELETE_COMMAND = "delete";

    private static final String TODO_TASK_COMMAND = "todo";
    private static final String DEADLINE_TASK_COMMAND = "deadline";
    private static final String EVENT_TASK_COMMAND = "event";

    /**
     * The main method where the program starts.
     * Initializes the application, takes user input, and responds based on commands.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Storage storage = new Storage(STORAGE_FILE_PATH);

        String logo = " _   _      _   _        \n"
                + "| \\ | | ___| |_| |__  ___ _ __ \n"
                + "|  \\| |/ _ \\ __| '_ \\/ _ \\ '__|\n"
                + "| |\\  |  __/ |_| | | ||__/ |  \n"
                + "|_| \\_|\\___|\\__|_| |_\\___|_|\n";
        ArrayList<Task> tasks = new ArrayList<>(storage.loadTasks());
        int counter = tasks.size();

        System.out.println("Hello from\n" + logo);
        printHorizontalLine();
        System.out.println("Hello sir! I'm Nether");
        System.out.println("What can I do for you today?");
        printHorizontalLine();

        Scanner scanner = new Scanner(System.in);
        System.out.print("");
        while (scanner.hasNextLine()) {
            try {
                String userInput = scanner.nextLine().trim();
                if (userInput.equalsIgnoreCase(EXIT_COMMAND)) {
                    break;
                }

                if (userInput.equalsIgnoreCase(LIST_COMMAND)) {
                    printHorizontalLine();
                    printList(tasks);
                    printHorizontalLine();
                    continue;
                }

                if (userInput.toLowerCase().startsWith(MARK_DONE_COMMAND)
                        || userInput.toLowerCase().startsWith(MARK_NOT_DONE_COMMAND)
                        || userInput.toLowerCase().startsWith(DELETE_COMMAND)) {
                    printHorizontalLine();
                    int taskNumber = extractTaskNumber(userInput);
                    if (taskNumber != -1 && taskNumber <= counter) {
                        Task taskToMark = tasks.get(taskNumber - 1);
                        if (userInput.toLowerCase().startsWith(MARK_DONE_COMMAND)) {
                            taskToMark.markAsDone();
                            System.out.println("Well done! I've marked this task as done:");
                            System.out.println("  " + taskToMark);
                        } else if (userInput.toLowerCase().startsWith(MARK_NOT_DONE_COMMAND)) {
                            taskToMark.markAsNotDone();
                            System.out.println("Understood, I've marked this task as not done:");
                            System.out.println("  " + taskToMark);
                        } else {
                            tasks.remove(taskToMark);
                            counter--;
                            System.out.println("Noted, I've removed this task from the list:");
                            System.out.println("  " + taskToMark);
                            System.out.println("Now you have " + counter + " task" + (counter > 1 ? "s" : "")
                                    + " in the list.");
                        }
                        storage.saveTasks(tasks);
                    } else {
                        throw new NetherException("you inputted an invalid task index.");
                    }
                    printHorizontalLine();
                    continue;
                }

                // If none of the special command above are input, run the code below:
                printHorizontalLine();

                String[] processedInput;
                if (userInput.toLowerCase().startsWith(TODO_TASK_COMMAND)) {
                    processedInput = extractInputDetails(userInput, TODO_TASK_COMMAND);
                    System.out.println("Got it. I've added this task:");
                    tasks.add(new TodoTask(processedInput[0]));
                } else if (userInput.toLowerCase().startsWith(DEADLINE_TASK_COMMAND)) {
                    processedInput = extractInputDetails(userInput, DEADLINE_TASK_COMMAND);
                    System.out.println("Got it. I've added this task:");
                    tasks.add(new DeadlineTask(processedInput[0], processedInput[1]));
                } else if (userInput.toLowerCase().startsWith(EVENT_TASK_COMMAND)) {
                    processedInput = extractInputDetails(userInput, EVENT_TASK_COMMAND);
                    System.out.println("Got it. I've added this task:");
                    tasks.add(new EventTask(processedInput[0], processedInput[1], processedInput[2]));
                } else {
                    extractInputDetails(userInput, userInput);
                }

                System.out.println("  " + tasks.get(tasks.size() - 1).toString());
                counter++;
                // distinguishing singular and plural
                System.out.println("Now you have " + counter + " task" + (counter > 1 ? "s" : "") + " in the list.");
                printHorizontalLine();

                storage.saveTasks(tasks);
            } catch (NetherException e) {
                System.out.println("Sir, " + e.getMessage());
                printHorizontalLine();
            }
        }

        // exit message
        printHorizontalLine();
        System.out.println("Bye. If you need any more help in the future, feel free to ask me. Enjoy your day!");
        printHorizontalLine();
    }

    /**
     * Prints out a long horizontal line to act as separator in the chat
     */
    private static void printHorizontalLine() {
        System.out.println("____________________________________________________________");
    }

    /**
     * Prints out the task list along with its status (done or not done)
     *
     * @param tasks array that holds the task list
     */
    private static void printList(ArrayList<Task> tasks) {
        int listIndex = 1;
        System.out.println("Here are the tasks in your list:");
        for (Task task: tasks) {
            System.out.println(listIndex + ". " + task.toString());
            listIndex++;
        }
    }

    /**
     * Returns the index/number of the task to be marked/unmarked
     *
     * @param userInput the string input by user
     * @return (index + 1) of the task to be marked/unmarked
     */
    private static int extractTaskNumber(String userInput) {
        try {
            String[] parts = userInput.split(" ");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * processes the user input into parts using split and regex to make it easier to instantiate the respective tasks
     *
     * @param userInput input by the user
     * @param taskType can be todo, deadline, or event
     * @return a String array of the parts of the user input that will be used for the
     *
     */
    private static String[] extractInputDetails(String userInput, String taskType) {
        String[] preprocessArray;
        String[] resultArray = new String[]{};
        switch (taskType) {
        case "todo":
            preprocessArray = userInput.split("(?i)todo ", 2);
            if (preprocessArray.length < 2 || preprocessArray[1].trim().isEmpty()) {
                throw new NetherException("The description of a todo cannot be empty.");
            }
            resultArray = new String[]{preprocessArray[1]};
            break;

        case "deadline":
            preprocessArray = userInput.split("(?i)deadline ", 2);
            if (preprocessArray.length < 2 || preprocessArray[1].trim().isEmpty()) {
                throw new NetherException("The description of a deadline cannot be empty.");
            }
            String[] deadlineParts = preprocessArray[1].split("/by ", 2);
            if (deadlineParts.length < 2 || deadlineParts[0].trim().isEmpty() || deadlineParts[1].trim().isEmpty()) {
                throw new NetherException("The description or date/time of a deadline cannot be empty.");
            }
            resultArray = new String[] {deadlineParts[0], deadlineParts[1]};
            break;

        case "event":
            preprocessArray = userInput.split("(?i)event ", 2);
            if (preprocessArray.length < 2 || preprocessArray[1].trim().isEmpty()) {
                throw new NetherException("The description of an event cannot be empty.");
            }
            String[] eventParts = preprocessArray[1].split("/from |/to ", 3);
            if (eventParts.length < 3 || eventParts[0].trim().isEmpty() || eventParts[1].trim().isEmpty()
                    || eventParts[2].trim().isEmpty()) {
                throw new NetherException(
                        "The description, start time, or end time of an event cannot be empty.");
            }
            resultArray = new String[]{eventParts[0], eventParts[1], eventParts[2]};
            break;
        default:
            throw new NetherException("the command: '" + userInput + "' is not in our database");
        }
        return resultArray;
    }

}
