import java.util.*;

public class AStar {
    private static final Comparator<State> aStarComparator = (s1, s2) -> {
        int f1 = s1.getCost() + s1.getHeuristic();
        int f2 = s2.getCost() + s2.getHeuristic();
        return Integer.compare(f1, f2);
    };

    public static void solve(State startState) {
        long startTime = System.currentTimeMillis();
        int visitCount = 0;
        PriorityQueue<State> openSet = new PriorityQueue<>(aStarComparator);
        Map<State, Integer> bestFScore = new HashMap<>();

        openSet.add(startState);
        bestFScore.put(startState, startState.getCost() + startState.getHeuristic());

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

            for (State next : current.getNextStates()) {
                int g = next.getCost();
                int f = g + next.getHeuristic();

                if (!bestFScore.containsKey(next) || f < bestFScore.get(next)) {
                    bestFScore.put(next, f);
                    openSet.add(next);
                }
            }
        }

        System.out.println("No solution found.");
        long endTime = System.currentTimeMillis();
        startState.saveNoSolutionToFile(visitCount, endTime - startTime);
    }

    public static SearchResult GUIsolve(State initialState, int heuristicType) {
        long startTime = System.currentTimeMillis();
        int visitCount = 0;
        // Comparator tetap sama, pakai cost + heuristic
        PriorityQueue<State> openSet = new PriorityQueue<>(aStarComparator);
        Map<State, Integer> bestFScore = new HashMap<>();

        // Set heuristic sesuai pilihan user
        initialState.setHeuristic(Heuristic.calculate(initialState, heuristicType));
        openSet.add(initialState);
        bestFScore.put(initialState, initialState.getCost() + initialState.getHeuristic());

        while (!openSet.isEmpty()) {
            State current = openSet.poll();
            visitCount++;

            if (current.isGoal()) {
                long endTime = System.currentTimeMillis();
                return new SearchResult(current.getPath(), endTime - startTime, visitCount);
            }

            for (State next : current.getNextStates()) {
                // Set heuristic untuk next state sesuai pilihan user
                next.setHeuristic(Heuristic.calculate(next, heuristicType));
                int g = next.getCost();
                int f = g + next.getHeuristic();

                // Set parent/prevState agar path bisa ditelusuri di GUI
                next.setPrevState(current);

                if (!bestFScore.containsKey(next) || f < bestFScore.get(next)) {
                    bestFScore.put(next, f);
                    openSet.add(next);
                }
            }
        }
        long endTime = System.currentTimeMillis();
        List<State> noSolution = new ArrayList<>();
        noSolution.add(initialState);
        return new SearchResult(noSolution, endTime - startTime, visitCount);
    }
}
