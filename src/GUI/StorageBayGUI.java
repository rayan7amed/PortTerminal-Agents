package GUI;

import simulation.Simulation;
import utils.StorageBay;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class StorageBayGUI extends JFrame {

    private JPanel gridPanel;
    private static JPanel[][] stackPanels;
    private static JLabel[][] countLabels;
    private static int[][] stackCounts;

    public StorageBayGUI() {
        setTitle("Storage Bay GUI");
        setSize(800, 800);
//        setDefaultCloseOperation(EXIT_ON_CLOSE);

        gridPanel = new JPanel(new GridLayout(10, 10));
        stackPanels = new JPanel[10][10];
        countLabels = new JLabel[10][10];
        stackCounts = new int[10][10];

        // Create stack representations with count labels
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                JPanel stackPanel = createStackPanel();
                gridPanel.add(stackPanel);

                stackPanels[i][j] = stackPanel;
                countLabels[i][j] = new JLabel("", SwingConstants.CENTER);
                stackPanel.add(countLabels[i][j], BorderLayout.CENTER);
            }
        }

        add(gridPanel);

        setupCounts();
        // Start a timer to update the counts every second
//        Timer timer = new Timer(1000, e -> updateCounts());
//        timer.start();
    }

    private JPanel createStackPanel() {
        JPanel stackPanel = new JPanel(new BorderLayout());
        stackPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        stackPanel.setPreferredSize(new Dimension(50, 50)); // Adjust size as needed
        return stackPanel;
    }

    public static void updateCounts(int[] position) {
        ChangeColorOfStack(position, Color.RED);
        Simulation.wait(1);
        stackCounts[position[0]][position[1]] = StorageBay.terminalLayout[position[0]][position[1]].size();
        countLabels[position[0]][position[1]].setText(Integer.toString(stackCounts[position[0]][position[1]]));
        Simulation.wait(1);
        ChangeColorOfStack(position,Color.WHITE);
        Simulation.wait(1);
    }
    private void setupCounts(){
        for(int i = 0; i < 10; ++i)
            for(int j = 0; j < 10; ++j)
            {
                stackCounts[i][j] = StorageBay.terminalLayout[i][j].size();
                countLabels[i][j].setText(Integer.toString(stackCounts[i][j]));
                int[] pos = new int[2];
                pos[0] = i;
                pos[1] = j;
                ChangeColorOfStack(pos,Color.WHITE);
            }
    }

    public static void ChangeColorOfStack(int[] position, Color color) {
        stackPanels[position[0]][position[1]].setBackground(color);
    }
}