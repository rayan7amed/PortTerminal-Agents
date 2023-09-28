package GUI;

import javax.swing.*;

import agents.ManagerAgent;
import utils.*;
import utils.Container;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ManagerGUI extends JFrame {

    private JTextField arg1Field;
    private JTextField arg2Field;
    private JTextField arg3Field;
    private JButton createButton;

    public ManagerGUI() {
        setTitle("Manager GUI");
        setSize(500, 500);

        arg1Field = new JTextField(15);
        arg2Field = new JTextField(15);
        arg3Field = new JTextField(10);
        createButton = new JButton("Add Container");

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String arg1 = arg1Field.getText();
                String arg2 = arg2Field.getText();
                LocalDate arg3 = LocalDate.parse(arg3Field.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Create an object using the provided arguments
                Container obj = new Container(arg1, arg2, arg3);

                // Display a message with the created object's details
                JOptionPane.showMessageDialog(ManagerGUI.this,
                        "Object created:\n" + obj.toString(),
                        "Object Created", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ManagerAgent.closeGui(); // Handle window-closing event
            }
        });

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Code:"));
        panel.add(arg1Field);
        panel.add(new JLabel("Next Destination:"));
        panel.add(arg2Field);
        panel.add(new JLabel("Departure Date (yyyy-MM-dd):"));
        panel.add(arg3Field);
        panel.add(createButton);

        add(panel);
    }
}
