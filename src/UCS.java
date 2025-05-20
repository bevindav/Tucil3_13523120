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

    public static SearchResult GUIuniformCostSearch(State initialState) {
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
                return new SearchResult(current.getPath(), endTime - startTime, visitCount);
            }

            explored.add(current);

            for (State next : current.getNextStates()) {
                next.setPrevState(current); // Penting: set parent/prevState!
                if (!explored.contains(next) && !frontier.contains(next)) {
                    frontier.add(next);
                } else if (frontier.contains(next)) {
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
        long endTime = System.currentTimeMillis();
        java.util.List<State> noSolution = new java.util.ArrayList<>();
        noSolution.add(initialState);
        return new SearchResult(noSolution, endTime - startTime, visitCount);
    }
}
