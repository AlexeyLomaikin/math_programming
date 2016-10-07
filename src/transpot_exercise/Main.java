package transpot_exercise;

import transpot_exercise.gui.HelloFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new HelloFrame();
                frame.setVisible(true);
            }
        });
    }
}
