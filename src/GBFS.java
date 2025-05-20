import java.util.*;

public class GBFS {
    // comparator GBFS: f(n) = h(n)
    private static final Comparator<State> gbfsComparator = Comparator.comparingInt(State::getHeuristic);

    public static void solve(State initialState) {
        long startTime = System.currentTimeMillis();
        int visitCount = 0;
        Set<State> visited = new HashSet<>();
        PriorityQueue<State> queue = new PriorityQueue<>(gbfsComparator);

        queue.add(initialState);

        while (!queue.isEmpty()) {
            State current = queue.poll();

            if (visited.contains(current)) continue;
            visited.add(current);
            visitCount++;

            if (current.isGoal()) {
                long endTime = System.currentTimeMillis();
                System.out.println("Execution time: " + (endTime - startTime) + " ms");
                System.out.println("Visited nodes: " + visitCount);
                current.printSolution();
                current.saveSolutionToFile(visitCount, endTime - startTime);
                return;
            }

            for (State next : current.getNextStates()) {
                if (!visited.contains(next)) {
                    next.setPrevState(current); // Set parent/prevState
                    queue.add(next);
                }
            }
        }

        System.out.println("No solution found.");
        long endTime = System.currentTimeMillis();
        initialState.saveNoSolutionToFile(visitCount, endTime - startTime);
    }

    public static SearchResult GUIsolve(State initialState, int heuristicType) {
        long startTime = System.currentTimeMillis();
        int visitCount = 0;
        Set<State> visited = new HashSet<>();
        Comparator<State> guiComparator = Comparator.comparingInt(s -> Heuristic.calculate(s, heuristicType));
        PriorityQueue<State> queue = new PriorityQueue<>(guiComparator);

        // Set heuristic untuk initialState
        initialState.setHeuristic(Heuristic.calculate(initialState, heuristicType));
        queue.add(initialState);

        while (!queue.isEmpty()) {
            State current = queue.poll();

            if (visited.contains(current)) continue;
            visited.add(current);
            visitCount++;

            if (current.isGoal()) {
                long endTime = System.currentTimeMillis();
                return new SearchResult(current.getPath(), endTime - startTime, visitCount);
            }

            for (State next : current.getNextStates()) {
                if (!visited.contains(next)) {
                    next.setPrevState(current);
                    next.setHeuristic(Heuristic.calculate(next, heuristicType)); // Set heuristic untuk next state
                    queue.add(next);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        List<State> noSolution = new ArrayList<>();
        noSolution.add(initialState);
        return new SearchResult(noSolution, endTime - startTime, visitCount);
    }
}