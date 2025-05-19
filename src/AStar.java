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
}
