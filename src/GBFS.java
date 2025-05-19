import java.util.*;

public class GBFS {
    // Comparator for GBFS: f(n) = h(n), where h(n) is the heuristic
    private static final Comparator<State> gbfsComparator = (s1, s2) -> {
        int h1 = s1.getHeuristic();
        int h2 = s2.getHeuristic();
        return Integer.compare(h1, h2);
    };

    public static void solve(State initialState) {
        long startTime = System.currentTimeMillis();
        int visitCount = 0;
        PriorityQueue<State> openSet = new PriorityQueue<>(gbfsComparator);
        Set<State> closedSet = new HashSet<>();

        openSet.add(initialState);

        while (!openSet.isEmpty()) {
            State current = openSet.poll();
            visitCount++;

            if (current.isGoal()) {
                long endTime = System.currentTimeMillis();
                System.out.println("Execution time: " + (endTime - startTime) + " ms");
                System.out.println("Visited nodes: " + visitCount);
                current.printSolution();
                current.saveSolutionToFile(visitCount, endTime - startTime);
                return;
            }

            closedSet.add(current);

            for (State next : current.getNextStates()) {
                if (!closedSet.contains(next)) {
                    openSet.add(next);
                }
            }
        }

        System.out.println("No solution found.");
        long endTime = System.currentTimeMillis();
        initialState.saveNoSolutionToFile(visitCount, endTime - startTime);
    }
}
