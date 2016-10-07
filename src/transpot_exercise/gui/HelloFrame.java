package transpot_exercise.gui;

import transpot_exercise.validation.FieldHelper;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class HelloFrame extends JFrame{
    private static final String CONSUMER_FIELD = "consumber_field";
    private static final String PROVIDER_FIELD = "provider_field";

    HashMap<String, JTextField> fields = new HashMap<>();
    private JButton executeButton;
    private Border defaultBorder;

    public HelloFrame() {
        setTitle("Транспортная задача");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.init();
    }

    private void init() {
        Box mainPanel = Box.createVerticalBox();

        mainPanel.add(getInputPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(getButtonsPanel());
        setContentPane(mainPanel);

        setPreferredSize(new Dimension(350, 130));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void openFillFrame() {
        int providersSize = Integer.parseInt(fields.get(PROVIDER_FIELD).getText());
        int consumerSize = Integer.parseInt(fields.get(CONSUMER_FIELD).getText());
        FillFrame fillFrame = new FillFrame(this, providersSize, consumerSize);
        fillFrame.setVisible(true);
    }

    private Component getButtonsPanel() {
        Box buttonPanel = Box.createHorizontalBox();

        JButton execute = new JButton("Заполнить таблицу");
        this.executeButton = execute;
        execute.setEnabled(false);
        execute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFillFrame();
            }
        });
        execute.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openFillFrame();
               }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        JButton exit = new JButton("выйти");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });
        exit.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                    System.exit(0);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(execute);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(exit);

        return buttonPanel;
    }

    private Component getInputPanel() {
        Box inputPanel = Box.createVerticalBox();

        Box providerPanel = Box.createHorizontalBox();
        JLabel providerLabel = new JLabel("Число поставщиков");
        JTextField providerTextBox = new JTextField(15);
        providerPanel.add(providerLabel);
        providerPanel.add(Box.createHorizontalStrut(6));
        providerPanel.add(providerTextBox);

        Box consumerPanel = Box.createHorizontalBox();
        JLabel consumerLabel = new JLabel("Число потребителей");
        JTextField consumerTextBox = new JTextField(15);
        consumerPanel.add(consumerLabel);
        consumerPanel.add(Box.createHorizontalStrut(6));
        consumerPanel.add(consumerTextBox);

        fields.put(PROVIDER_FIELD, providerTextBox);
        fields.put(CONSUMER_FIELD, consumerTextBox);
        this.defaultBorder = consumerTextBox.getBorder();
        addHandlersToTextBox(providerTextBox);
        addHandlersToTextBox(consumerTextBox);

        providerLabel.setPreferredSize(consumerLabel.getPreferredSize());

        inputPanel.add(providerPanel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(consumerPanel);
        return inputPanel;
    }

    private void addHandlersToTextBox(JTextField textBox) {
        textBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                validateInput();
            }

            @Override
            public void focusLost(FocusEvent e) {
                validateInput();
            }
        });
        textBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }
        });
        textBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (executeButton.isEnabled()) {
                        openFillFrame();
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private void validateInput() {
        boolean isValid = true;
        for (JTextField field: fields.values()) {
            boolean fieldEmpty = FieldHelper.isEmpty(field.getText());
            boolean fieldValid = FieldHelper.isNaturalNumber(field.getText());
            backlightField(fieldEmpty || fieldValid, field);
            if (!fieldValid) {
                isValid = false;
            }
        }
        executeButton.setEnabled(isValid);
    }

    private void backlightField(boolean correct, JTextField field) {
        if (!correct) {
            field.setBorder(BorderFactory.createLineBorder(Color.red));
        }
        else {
            field.setBorder(defaultBorder);
        }
    }
}
