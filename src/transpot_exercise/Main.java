package transpot_exercise;

import transpot_exercise.gui.HelloFrame;

import javax.swing.*;

/**
 * Created by AlexL on 24.09.2016.
 */
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
