import java.util.*;

public class BeamSearch {
    private static int beamWidth = 100;

    public static void solve(State initialState) {
        long startTime = System.currentTimeMillis();
        Set<String> visited = new HashSet<>();
        int nodeCount = 0;
        List<State> currentBeam = new ArrayList<>();
        currentBeam.add(initialState);

        int iterations = 0;
        int maxIterations = 10000; 

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

    private static String getBoardKey(char[][] board) {
        StringBuilder key = new StringBuilder();
        for (char[] row : board) {
            key.append(String.valueOf(row));
        }
        return key.toString();
    }

    public static SearchResult GUIsolve(State initialState, int beamWidth) {
        long startTime = System.currentTimeMillis();
        Set<String> visited = new HashSet<>();
        int nodeCount = 0;
        List<State> currentBeam = new ArrayList<>();
        currentBeam.add(initialState);

        int iterations = 0;
        int maxIterations = 10000;

        while (!currentBeam.isEmpty() && iterations < maxIterations) {
            iterations++;
            List<State> nextBeam = new ArrayList<>();
            for (State current : currentBeam) {
                if (current.isGoal()) {
                    long endTime = System.currentTimeMillis();
                    return new SearchResult(current.getPath(), endTime - startTime, nodeCount);
                }
                String boardKey = getBoardKey(current.getBoard().getBoard());
                visited.add(boardKey);
                List<State> successors = current.getNextStates();
                for (State successor : successors) {
                    String successorKey = getBoardKey(successor.getBoard().getBoard());
                    if (!visited.contains(successorKey)) {
                        successor.setPrevState(current); // Penting: set parent/prevState!
                        nextBeam.add(successor);
                        nodeCount++;
                    }
                }
            }
            if (nextBeam.isEmpty()) {
                break;
            }

            Collections.sort(nextBeam, (s1, s2) -> {
                int f1 = s1.getCost() + s1.getHeuristic();
                int f2 = s2.getCost() + s2.getHeuristic();
                return Integer.compare(f1, f2);
            });

            currentBeam = nextBeam.size() <= beamWidth ?
                    nextBeam :
                    nextBeam.subList(0, beamWidth);
        }

        long endTime = System.currentTimeMillis();
        List<State> noSolution = new ArrayList<>();
        noSolution.add(initialState);
        return new SearchResult(noSolution, endTime - startTime, nodeCount);
    }
}
