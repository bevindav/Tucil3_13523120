import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Input filename (now expecting it to be inside the 'test' folder)
        System.out.println("=====================================");
        System.out.println(" Rush Hour Solver [Jakarta Edition]");
        System.out.println("=====================================");

        String fileName = "";
        Path filePath = null;

        // Loop to ensure a valid file is provided
        while (filePath == null || !Files.exists(filePath)) {
            System.out.println("\nEnter the filename (ex: test1.txt) located in the 'test' folder:");

            try {
                fileName = reader.readLine().trim();
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
                return;
            }

            // Ensure the file exists in the 'test' folder
            filePath = Paths.get("test", fileName);
            if (!Files.exists(filePath)) {
                System.err.println("File not found in the 'test' folder: " + fileName);
            }
        }

        // Initialize the board and load the configuration from the file
        Board board = new Board();
        board.readFromFile(filePath.toString());  // Load the board from the provided file
        Map<Character, Piece> pieces = board.getPieceMap();
        pieces.put('P', board.getPrimaryPiece());  // Add the primary piece (red car)
        System.out.println("\nInitial Board Configuration:");
        board.printBoard();  // Print the initial board

        // Create the initial state
        State initialState = new State(pieces, board, 0, null, "Initial State");

        // Algorithm selection with input validation inside a loop
        int choice = -1;
        while (choice < 1 || choice > 4) {
            System.out.println("\nSelect the search algorithm:");
            System.out.println("1. UCS (Uniform Cost Search)");
            System.out.println("2. GBFS (Greedy Best First Search)");
            System.out.println("3. A* (A Star Search)");
            System.out.println("4. Beam Search");

            try {
                String input = reader.readLine().trim();
                choice = Integer.parseInt(input);

                // Ensure valid choice
                if (choice < 1 || choice > 4) {
                    System.err.println("Invalid input. Please select a valid option (1-4).");
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number between 1 and 4.");
            }
        }

        // Perform the search based on the user's choice
        switch (choice) {
            case 1:
                System.out.println("\nUsing UCS Algorithm");
                UCS.uniformCostSearch(initialState);
                break;
            case 2:
                System.out.println("\nUsing GBFS Algorithm");
                GBFS.solve(initialState);
                break;
            case 3:
                System.out.println("\nUsing A* Algorithm");
                AStar.solve(initialState);
                break;
            case 4:
                System.out.println("\nUsing Beam Search Algorithm");
                BeamSearch.solve(initialState);
                break;
            default:
                System.err.println("Invalid choice.");
                return;
        }
    }
}
