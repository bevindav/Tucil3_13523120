import java.util.*;

public class UCS {

    public static void uniformCostSearch(State initialState) {
        long startTime = System.currentTimeMillis();
        int visitCount = 0;
        PriorityQueue<State> frontier = new PriorityQueue<>();
        Set<State> explored = new HashSet<>();

        frontier.add(initialState);

        while (!frontier.isEmpty()) {
            State current = frontier.poll();
            visitCount++;

            if (current.isGoal()) {
                long endTime = System.currentTimeMillis();
                System.out.println("Execution time: " + (endTime - startTime) + " ms");
                System.out.println("Visited nodes: " + visitCount);
                current.printSolution();
                current.saveSolutionToFile(visitCount, endTime - startTime);
                return;
            }

            explored.add(current);

            for (State next : current.getNextStates()) {
                if (!explored.contains(next) && !frontier.contains(next)) {
                    frontier.add(next);
                } else if (frontier.contains(next)) {
                    // If next is in frontier with higher cost, update
                    for (State stateInFrontier : frontier) {
                        if (stateInFrontier.equals(next) && stateInFrontier.getCost() > next.getCost()) {
                            frontier.remove(stateInFrontier);
                            frontier.add(next);
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("No solution found.");
        long endTime = System.currentTimeMillis();
        initialState.saveNoSolutionToFile(visitCount, endTime - startTime);
    }
}
