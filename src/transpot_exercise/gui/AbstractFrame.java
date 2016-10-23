package transpot_exercise.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class AbstractFrame extends JFrame{
    private JFrame prevFrame;
    protected boolean okPressed;

    public AbstractFrame(JFrame prevFrame) {
        this.prevFrame = prevFrame;
        setTitle("Траспортная задача");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    protected void init() {
        Box mainPanel = Box.createVerticalBox();

        mainPanel.add(createInfoLabel());
        mainPanel.add(createInputPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createButtonPanel());

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    public void exit() {
        dispose();
        System.exit(0);
    }

    private Component createButtonPanel() {
        Box buttonPanel = Box.createHorizontalBox();

        final JButton ok = new JButton("далее");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                onOkPressed();
                okPressed = false;
            }
        });
        ok.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okPressed = true;
                    onOkPressed();
                    okPressed = false;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        JButton cancel = new JButton("назад");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnToPrevFrame();
            }
        });
        cancel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    returnToPrevFrame();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        JButton exit = new JButton("выйти");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        exit.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    exit();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });


        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(ok);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(exit);

        return buttonPanel;
    }

    private void returnToPrevFrame() {
        dispose();
        if (prevFrame != null) {
            prevFrame.setVisible(true);
            prevFrame.toFront();
        }
    }

    protected abstract void onOkPressed();
    protected abstract Component createInfoLabel();
    protected abstract Component createInputPanel();
}
