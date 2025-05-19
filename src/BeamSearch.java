import java.util.*;

public class BeamSearch {
    private static int beamWidth = 100; // Number of best states to retain

    public static void solve(State initialState) {
        long startTime = System.currentTimeMillis();
        Set<String> visited = new HashSet<>();
        int nodeCount = 0;
        List<State> currentBeam = new ArrayList<>();
        currentBeam.add(initialState);

        int iterations = 0;
        int maxIterations = 10000; // Maximum iterations to avoid infinite loop

        while (!currentBeam.isEmpty() && iterations < maxIterations) {
            iterations++;
            List<State> nextBeam = new ArrayList<>();
            for (State current : currentBeam) {
                if (current.isGoal()) {
                    long endTime = System.currentTimeMillis();
                    System.out.println("Execution time: " + (endTime - startTime) + " ms");
                    System.err.println("Visited nodes: " + nodeCount);
                    current.printSolution();
                    current.saveSolutionToFile(nodeCount, endTime - startTime);
                    return;
                }
                String boardKey = getBoardKey(current.getBoard().getBoard());
                visited.add(boardKey);
                List<State> successors = current.getNextStates();
                for (State successor : successors) {
                    String successorKey = getBoardKey(successor.getBoard().getBoard());
                    if (!visited.contains(successorKey)) {
                        nextBeam.add(successor);
                        nodeCount++;
                    }
                }
            }
            if (nextBeam.isEmpty()) {
                break;
            }

            // Sort next beam based on combined cost and heuristic (f = g + h)
            Collections.sort(nextBeam, (s1, s2) -> {
                int f1 = s1.getCost() + s1.getHeuristic();
                int f2 = s2.getCost() + s2.getHeuristic();
                return Integer.compare(f1, f2);
            });

            currentBeam = nextBeam.size() <= beamWidth ?
                    nextBeam :
                    nextBeam.subList(0, beamWidth);
        }

        System.out.println("No solution found after " + iterations + " iterations.");
        long endTime = System.currentTimeMillis();
        initialState.saveNoSolutionToFile(nodeCount, endTime - startTime);
    }

    // Function to get a unique string representation of the board
    private static String getBoardKey(char[][] board) {
        StringBuilder key = new StringBuilder();
        for (char[] row : board) {
            key.append(String.valueOf(row));
        }
        return key.toString();
    }
}
