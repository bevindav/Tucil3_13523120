import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.event.*;

public class App extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 700;
    
    private Board board;
    private State initialState;
    private String selectedAlgorithm = "UCS";  
    private List<State> solutionPath;
    private int currentStep = 0;
    private boolean isAnimating = false;
    
    private GamePanel boardPanel;
    private JButton loadButton;
    private JComboBox<String> algorithmComboBox;
    private JButton solveButton;
    private JButton saveButton;
    private JButton playPauseButton;
    private JButton resetButton;
    private JTextArea logArea;
    private JSlider speedSlider;
    private Timer animationTimer;
    private JLabel statusLabel;
    private JLabel stepCountLabel;
    private JLabel execTimeLabel;
    private JLabel visitedNodesLabel;
    private JProgressBar solutionProgress;
    
    private final Color ROAD_COLOR = new Color(50, 50, 50);
    private final Color EXIT_COLOR = new Color(0, 200, 0);
    private final Color PRIMARY_CAR_COLOR = new Color(220, 50, 50);
    private final Color BOARD_BACKGROUND = new Color(150, 150, 150);
    private final Map<Character, Color> colorMap = new HashMap<>();

    public interface SolverCallback {
        void onStep(State currentState);
        void onSolutionFound(State solution, long executionTime, int visitedNodes);
        void onNoSolution(long executionTime, int visitedNodes);
    }
    public App() {
        setTitle("Rush Hour Solver [Jakarta Edition]");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            log("Could not set Nimbus look and feel");
        }
        initUI();
    }

    private void initUI() {
        boardPanel = new GamePanel();
        boardPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT - 200));
        boardPanel.setBackground(BOARD_BACKGROUND);
        
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boardContainer.add(boardPanel, BorderLayout.CENTER);

        loadButton = createButton("Load Traffic", "Load a traffic puzzle from file");
        loadButton.addActionListener(e -> loadTraffic());

        String[] algorithms = {"UCS", "GBFS", "A*", "Beam Search"};
        algorithmComboBox = new JComboBox<>(algorithms);
        algorithmComboBox.setToolTipText("Select search algorithm");
        algorithmComboBox.addActionListener(e -> selectedAlgorithm = (String) algorithmComboBox.getSelectedItem());

        solveButton = createButton("Solve", "Solve the puzzle with selected algorithm");
        solveButton.addActionListener(e -> solvePuzzle());

        saveButton = createButton("Save Solution", "Save solution to file");
        saveButton.addActionListener(e -> saveSolution());

        playPauseButton = createButton("Play", "Play/Pause animation");
        playPauseButton.setEnabled(false);
        playPauseButton.addActionListener(e -> toggleAnimation());

        execTimeLabel = new JLabel("Execution time: - ms");
        execTimeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        visitedNodesLabel = new JLabel("Visited nodes: -");
        visitedNodesLabel.setFont(new Font("Arial", Font.PLAIN, 13));   

        resetButton = createButton("Reset", "Reset to initial state");
        resetButton.setEnabled(false);
        resetButton.addActionListener(e -> resetAnimation());

        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setToolTipText("Animation speed");
        speedSlider.setPreferredSize(new Dimension(150, 40));
        speedSlider.addChangeListener(e -> updateAnimationSpeed());

        stepCountLabel = new JLabel("Step: 0/0");
        stepCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        solutionProgress = new JProgressBar(0, 100);
        solutionProgress.setStringPainted(true);
        solutionProgress.setString("No solution");
        solutionProgress.setPreferredSize(new Dimension(150, 20));

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setForeground(new Color(0, 100, 200));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topControls.add(loadButton);
        topControls.add(new JLabel("Algorithm:"));
        topControls.add(algorithmComboBox);
        topControls.add(solveButton);
        topControls.add(saveButton);
        
        JPanel animationControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        animationControls.add(playPauseButton);
        animationControls.add(resetButton);
        animationControls.add(new JLabel("Speed:"));
        animationControls.add(speedSlider);
        animationControls.add(stepCountLabel);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(solutionProgress);

        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(execTimeLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(visitedNodesLabel);
        
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(topControls);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(animationControls);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(statusPanel);
        controlPanel.add(Box.createVerticalStrut(5));
        
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 5, 10),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            )
        ));

        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(240, 240, 240));
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        logScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Log Output"));

        add(boardContainer, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        add(logScroll, BorderLayout.SOUTH);
        
        animationTimer = new Timer(200, e -> stepAnimation());
    }
    
    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }
    
    private void toggleAnimation() {
        if (solutionPath == null || solutionPath.isEmpty()) {
            return;
        }

        if (isAnimating) {
            animationTimer.stop();
            playPauseButton.setText("Play");
            isAnimating = false;
        } else {
            if (currentStep >= solutionPath.size()) {
                currentStep = 0;
            }
            animationTimer.start();
            playPauseButton.setText("Pause");
            isAnimating = true;
        }
    }
    
    private void resetAnimation() {
        if (solutionPath == null || solutionPath.isEmpty()) {
            return;
        }
        
        animationTimer.stop();
        isAnimating = false;
        currentStep = 0;
        
        if (solutionPath.size() > 0) {
            board = solutionPath.get(0).getBoard().copy();
            updateStepDisplay();
            boardPanel.repaint();
        }
        
        playPauseButton.setText("Play");
    }
    
    private void updateAnimationSpeed() {
        if (animationTimer != null) {
            int delay = 1000 / speedSlider.getValue();
            animationTimer.setDelay(delay);
        }
    }
    
    private void stepAnimation() {
        if (solutionPath == null || currentStep >= solutionPath.size()) {
            animationTimer.stop();
            playPauseButton.setText("Play");
            isAnimating = false;
            return;
        }

        State currentState = solutionPath.get(currentStep);
        board = currentState.getBoard().copy();
        log("Move: " + currentState.getMove());
        updateStepDisplay();
        boardPanel.repaint();
        currentStep++;
        updateAnimationProgress();
    }

    private void updateAnimationProgress() {
        if (solutionPath != null && solutionPath.size() > 1) {
            int percentage = (int) Math.round((currentStep * 100.0) / Math.max(1, solutionPath.size() - 1));
            percentage = Math.max(0, Math.min(100, percentage));
            solutionProgress.setValue(percentage);
            solutionProgress.setString(percentage + "% Complete");
        }
    }
    
    private void updateStepDisplay() {
        if (solutionPath != null) {
            stepCountLabel.setText("Step: " + currentStep + "/" + (solutionPath.size() - 1));
            if (solutionPath.size() > 1) {
                int percentage = (currentStep * 100) / (solutionPath.size() - 1);
                solutionProgress.setValue(percentage);
                solutionProgress.setString(percentage + "% Complete");
            }
        }
    }

    private void loadTraffic() {
        JFileChooser fileChooser = new JFileChooser("test");
        fileChooser.setDialogTitle("Load Traffic Puzzle");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                board = new Board();
                board.readFromFile(file.getPath());
                Map<Character, Piece> pieces = board.getPieceMap();
                pieces.put('P', board.getPrimaryPiece());
                initialState = new State(pieces, board, 0, null, "Initial State");
                log("Loaded board from: " + file.getName());
                status("Board loaded successfully!");
                solutionPath = null;
                currentStep = 0;
                solutionProgress.setValue(0);
                solutionProgress.setString("No solution");
                stepCountLabel.setText("Step: 0/0");
                playPauseButton.setEnabled(false);
                resetButton.setEnabled(false);
                refreshColorMap();
                boardPanel.repaint();
            } catch (Exception e) {
                showError("Error loading board: " + e.getMessage());
            }
        }
    }
    
    private void refreshColorMap() {
        colorMap.clear();
        colorMap.put('P', PRIMARY_CAR_COLOR);
        colorMap.put('K', EXIT_COLOR);
        Random r = new Random(42);
        if (board != null) {
            for (Piece piece : board.getNonPieceList()) {
                char c = piece.getPieceChar();
                if (!colorMap.containsKey(c)) {
                    int hue = (int)(c * 20) % 360;
                    float saturation = 0.7f + r.nextFloat() * 0.3f;
                    float brightness = 0.7f + r.nextFloat() * 0.3f;
                    colorMap.put(c, Color.getHSBColor(hue/360f, saturation, brightness));
                }
            }
        }
    }

    private void solvePuzzle() {
        if (initialState == null) {
            showError("Please load a board first!");
            return;
        }

        solveButton.setEnabled(false);
        status("Solving with " + selectedAlgorithm + "... Please wait");
        solutionProgress.setIndeterminate(true);
        solutionProgress.setString("Solving...");
        SwingWorker<Void, State> worker = new SwingWorker<Void, State>() {
            SearchResult result = null;

            @Override
            protected Void doInBackground() {
                switch (selectedAlgorithm) {
                    case "UCS":
                        result = UCS.GUIuniformCostSearch(initialState);
                        execTimeLabel.setText("Execution time: " + result.executionTime + " ms");
                        visitedNodesLabel.setText("Visited nodes: " + result.visitedNodes);
                        break;
                    case "GBFS":
                        result = GBFS.GUIsolve(initialState);
                        execTimeLabel.setText("Execution time: " + result.executionTime + " ms");
                        visitedNodesLabel.setText("Visited nodes: " + result.visitedNodes);
                        break;
                    case "A*":
                        result = AStar.GUIsolve(initialState);
                        execTimeLabel.setText("Execution time: " + result.executionTime + " ms");
                        visitedNodesLabel.setText("Visited nodes: " + result.visitedNodes);
                        break;
                    case "Beam Search":
                    //2
                        result = BeamSearch.GUIsolve(initialState, 2);
                        execTimeLabel.setText("Execution time: " + result.executionTime + " ms");
                        visitedNodesLabel.setText("Visited nodes: " + result.visitedNodes);
                        break;
                }
                return null;
            }

            @Override
            protected void done() {
                solutionProgress.setIndeterminate(false);
                if (result != null) {
                    solutionPath = result.path;
                    currentStep = 0;
                    log("Execution time: " + result.executionTime + " ms");
                    log("Visited nodes: " + result.visitedNodes);
                    log("Steps in solution: " + (solutionPath.size() - 1));
                    if (solutionPath != null && solutionPath.size() > 1) {
                        solutionProgress.setValue(0);
                        solutionProgress.setString("0% (Ready to play)");
                        stepCountLabel.setText("Step: 0/" + (solutionPath.size() - 1));
                        status("Solution found! Press Play to start animation.");
                        playPauseButton.setEnabled(true);
                        resetButton.setEnabled(true);
                    } else {
                        solutionProgress.setValue(0);
                        solutionProgress.setString("No solution found");
                        status("No solution found!");
                    }
                } else {
                    solutionProgress.setValue(0);
                    solutionProgress.setString("No solution found");
                    status("No solution found!");
                }
                solveButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void saveSolution() {
        if (solutionPath == null || solutionPath.isEmpty()) {
            showError("No solution to save.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser("test");
        fileChooser.setDialogTitle("Save Solution");
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getPath().endsWith(".txt") ? file.getPath() : file.getPath() + ".txt";
            try (PrintWriter writer = new PrintWriter(path)) {
                writer.println("Rush Hour Solution - " + new java.util.Date());
                writer.println("Algorithm used: " + selectedAlgorithm);
                writer.println("Total steps: " + (solutionPath.size() - 1));
                writer.println("-------------------------------------");
                writer.println();
                for (int i = 0; i < solutionPath.size(); i++) {
                    State s = solutionPath.get(i);
                    writer.println("Step " + i + ": " + s.getMove());
                    writer.println(s.toStringWithoutColor());
                    writer.println();
                }
                log("Solution saved to " + path);
                status("Solution saved successfully!");
            } catch (IOException e) {
                showError("Failed to save solution: " + e.getMessage());
            }
        }
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        status("Error: " + msg);
    }

    private void status(String msg) {
        statusLabel.setText("Status: " + msg);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set look and feel");
        }
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
    
    private class GamePanel extends JPanel {
        private static final int CELL_PADDING = 4;
        private static final int CELL_CORNER_RADIUS = 15;
        private final int ROAD_MARK_COUNT = 5;
        private final int ROAD_MARK_WIDTH = 20;
        private final int ROAD_MARK_HEIGHT = 5;
        
        public GamePanel() {
            setBackground(ROAD_COLOR);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null) {
                drawEmptyBoard(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int rows = board.getRows();
            int cols = board.getCols();
            int cellWidth = getWidth() / cols;
            int cellHeight = getHeight() / rows;
            int cellSize = Math.min(cellWidth, cellHeight);
            int xOffset = (getWidth() - (cellSize * cols)) / 2;
            int yOffset = (getHeight() - (cellSize * rows)) / 2;
            g2.setColor(ROAD_COLOR);
            g2.fillRect(xOffset, yOffset, cellSize * cols, cellSize * rows);
            g2.setColor(Color.YELLOW);
            for (int row = 1; row < rows; row++) {
                int y = yOffset + row * cellSize - ROAD_MARK_HEIGHT / 2;
                for (int i = 0; i < ROAD_MARK_COUNT; i++) {
                    int x = xOffset + (i * 2 * cellSize / ROAD_MARK_COUNT);
                    g2.fillRect(x, y, ROAD_MARK_WIDTH, ROAD_MARK_HEIGHT);
                }
            }
            for (int col = 1; col < cols; col++) {
                int x = xOffset + col * cellSize - ROAD_MARK_WIDTH / 2;
                for (int i = 0; i < ROAD_MARK_COUNT; i++) {
                    int y = yOffset + (i * 2 * cellSize / ROAD_MARK_COUNT);
                    g2.fillRect(x, y, ROAD_MARK_WIDTH, ROAD_MARK_HEIGHT);
                }
            }
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    drawCell(g2, row, col, cellSize, xOffset, yOffset);
                }
            }
            int exitX = board.getExitX();
            int exitY = board.getExitY();
            if (exitX >= 0 && exitY >= 0) {
                drawExit(g2, exitY, exitX, cellSize, xOffset, yOffset);
            } else if (exitX == -1) {
                drawExit(g2, exitY, -1, cellSize, xOffset, yOffset);
            } else if (exitY == -1) {
                drawExit(g2, -1, exitX, cellSize, xOffset, yOffset);
            } else if (exitX >= cols) {
                drawExit(g2, exitY, cols, cellSize, xOffset, yOffset);
            } else if (exitY >= rows) {
                drawExit(g2, rows, exitX, cellSize, xOffset, yOffset);
            }
            drawGameInfo(g2);
        }
        
        private void drawEmptyBoard(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BOARD_BACKGROUND);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            String message = "Load a traffic puzzle to start";
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2;
            g2.drawString(message, x, y);
        }
        
        private void drawCell(Graphics2D g2, int row, int col, int cellSize, int xOffset, int yOffset) {
            char c = board.getChar(row, col);
            if (c == '.') return;
            int x = col * cellSize + xOffset;
            int y = row * cellSize + yOffset;
            Color pieceColor = colorMap.getOrDefault(c, Color.LIGHT_GRAY);
            g2.setColor(pieceColor);
            g2.fillRoundRect(
                x + CELL_PADDING, 
                y + CELL_PADDING, 
                cellSize - 2 * CELL_PADDING, 
                cellSize - 2 * CELL_PADDING, 
                CELL_CORNER_RADIUS, 
                CELL_CORNER_RADIUS
            );
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(
                x + CELL_PADDING, 
                y + CELL_PADDING, 
                cellSize - 2 * CELL_PADDING, 
                cellSize / 4, 
                CELL_CORNER_RADIUS, 
                CELL_CORNER_RADIUS
            );
            if (c == 'P') {
                drawCarDetails(g2, x, y, cellSize);
            }
            g2.setColor(getContrastColor(pieceColor));
            g2.setFont(new Font("Arial", Font.BOLD, cellSize / 3));
            FontMetrics fm = g2.getFontMetrics();
            String text = String.valueOf(c);
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            g2.drawString(text, 
                x + (cellSize - textWidth) / 2, 
                y + (cellSize + textHeight / 3) / 2
            );
        }
        
        private void drawCarDetails(Graphics2D g2, int x, int y, int cellSize) {
            g2.setColor(new Color(200, 230, 255, 180));
            int windowSize = cellSize / 4;
            g2.fillRoundRect(
                x + cellSize / 3, 
                y + cellSize / 4, 
                windowSize, 
                windowSize, 
                5, 5
            );
            g2.setColor(Color.YELLOW);
            int lightSize = cellSize / 10;
            g2.fillOval(
                x + cellSize - cellSize / 4, 
                y + cellSize / 3, 
                lightSize, 
                lightSize
            );
        }
        
        private void drawExit(Graphics2D g2, int row, int col, int cellSize, int xOffset, int yOffset) {
            int x, y;
            int width = cellSize / 2;
            int height = cellSize / 2;
            if (col == -1) {
                x = xOffset - width / 2;
                y = yOffset + row * cellSize + cellSize / 4;
            } else if (col >= board.getCols()) {
                x = xOffset + board.getCols() * cellSize - width / 2;
                y = yOffset + row * cellSize + cellSize / 4;
            } else if (row == -1) {
                x = xOffset + col * cellSize + cellSize / 4;
                y = yOffset - height / 2;
            } else if (row >= board.getRows()) {
                x = xOffset + col * cellSize + cellSize / 4;
                y = yOffset + board.getRows() * cellSize - height / 2;
            } else {
                x = xOffset + col * cellSize + cellSize / 4;
                y = yOffset + row * cellSize + cellSize / 4;
            }
            g2.setColor(EXIT_COLOR);
            g2.fillRoundRect(x, y, width, height, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, width, height, 10, 10);
            g2.setFont(new Font("Arial", Font.BOLD, cellSize / 6));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String exitText = "EXIT";
            int textWidth = fm.stringWidth(exitText);
            g2.drawString(exitText, x + (width - textWidth) / 2, y + height / 2 + fm.getHeight() / 4);
        }
        
        private void drawGameInfo(Graphics2D g2) {
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(Color.WHITE);
            g2.drawString("Rush Hour Solver", 10, 20);
            if (solutionPath != null && currentStep > 0 && currentStep < solutionPath.size()) {
                String move = solutionPath.get(currentStep).getMove();
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.setColor(Color.YELLOW);
                g2.drawString(move, 10, 50);
            }
        }
        
        private Color getContrastColor(Color bg) {
            double brightness = (bg.getRed() * 299 + bg.getGreen() * 587 + bg.getBlue() * 114) / 1000.0;
            return brightness > 128 ? Color.BLACK : Color.WHITE;
        }
    }
}