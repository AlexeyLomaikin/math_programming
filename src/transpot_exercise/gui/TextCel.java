package transpot_exercise.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class TextCel extends JTextField {
    private Border initialBorder = BorderFactory.createLineBorder(Color.BLACK);

    public TextCel(int columns, Border initialBorder) {
        this(columns);
        this.initialBorder = initialBorder;
        super.setBorder(initialBorder);
    }

    public TextCel(int columns) {
        super(columns);
        setFont(new Font("SansSerif", Font.ITALIC, 20));
    }

    public void resetBorder() {
        if (initialBorder != null) {
            super.setBorder(initialBorder);
        }
    }
}
