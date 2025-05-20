public class SearchResult {
    public final java.util.List<State> path;
    public final long executionTime;
    public final int visitedNodes;

    public SearchResult(java.util.List<State> path, long executionTime, int visitedNodes) {
        this.path = path;
        this.executionTime = executionTime;
        this.visitedNodes = visitedNodes;
    }
}