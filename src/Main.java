import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("=====================================");
        System.out.println(" Rush Hour Solver [Jakarta Edition]");
        System.out.println("=====================================");

        String fileName = "";
        Path filePath = null;

        while (filePath == null || !Files.exists(filePath)) {
            System.out.println("\nEnter the filename (ex: test1.txt) located in the 'test' folder:");

            try {
                fileName = reader.readLine().trim();
            } catch (IOException e) {
                System.err.println("Failed to read input. Make sure the file is in valid format :)");
            }

            filePath = Paths.get("test", fileName);
            if (!Files.exists(filePath)) {
                System.err.println("File not found in the 'test' folder: " + fileName);
            }
        }

        Board board = new Board();
        board.readFromFile(filePath.toString()); 
        Map<Character, Piece> pieces = board.getPieceMap();
        pieces.put('P', board.getPrimaryPiece());
        System.out.println("\nInitial Board Configuration:");
        board.printBoard(); 

        State initialState = new State(pieces, board, 0, null, "Initial State");

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

                if (choice < 1 || choice > 4) {
                    System.err.println("Invalid input. Please select a valid option (1-4).");
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number between 1 and 4.");
            }
        }

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
