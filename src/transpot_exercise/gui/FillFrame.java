package transpot_exercise.gui;

import transpot_exercise.validation.FieldHelper;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class FillFrame extends AbstractFrame {
    private int providersSize;
    private int consumerSize;
    private ArrayList<TextCel> fields = new ArrayList<>();
    private JTextField firstInvalidField;

    public FillFrame(JFrame prevFrame, int providersSize, int consumersSize) {
        super(prevFrame);
        this.providersSize = providersSize;
        this.consumerSize = consumersSize;
        super.init();
    }

    @Override
    protected Component createInfoLabel() {
        return new JLabel("Заполните таблицу стоимости и ресурсов");
    }

    private TextCel createField(boolean isDisabled, boolean isInfoField, String toolTip) {
        Border initialBorder = isInfoField ? BorderFactory.createLineBorder(Color.BLUE) :
                                             BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        final TextCel field = new TextCel(5, initialBorder);

        field.setEnabled(!isDisabled);

        if (!isDisabled) {
            fields.add(field);
            field.setToolTipText(toolTip);
        }else {
            field.setText(toolTip);
        }

        field.setHorizontalAlignment(JTextField.CENTER);

        addHandlersToField(field);

        return field;
    }

    private void addHandlersToField(final TextCel field) {

        field.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_KP_LEFT || key == KeyEvent.VK_LEFT) {
                    int x = fields.indexOf(field) - 1;
                    setFocusOnField(x);
                }else if (key == KeyEvent.VK_KP_RIGHT || key == KeyEvent.VK_RIGHT) {
                    int x = fields.indexOf(field) + 1;
                    setFocusOnField(x);
                }else if (key == KeyEvent.VK_KP_DOWN || key == KeyEvent.VK_DOWN) {
                    int x = fields.indexOf(field) + consumerSize + 1;
                    setFocusOnField(x);
                }else if(key == KeyEvent.VK_KP_UP || key == KeyEvent.VK_UP) {
                    int x = fields.indexOf(field) - consumerSize - 1;
                    setFocusOnField(x);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                backLightSelectedCell(true, (TextCel) e.getSource());
                validateCell(field);
            }

            @Override
            public void focusLost(FocusEvent e) {
                backLightSelectedCell(false, (TextCel) e.getSource());
                validateCell(field);
            }
        });
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onFieldChange(field);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onFieldChange(field);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onFieldChange(field);
            }
        });
    }

    private void onFieldChange(TextCel field){
        validateCell(field);
    }

    private void setFocusOnField(int idx) {
        if (idx >=0 && idx < fields.size()) {
            TextCel field = fields.get(idx);
            if (field.isEnabled()) {
                field.requestFocus();
            }
        }
    }

    @Override
    protected Component createInputPanel() {
        final Box table = Box.createVerticalBox();
        for (int i = 0; i < providersSize + 1; i++) {
            Box row = Box.createHorizontalBox();
            for (int j = 0; j < consumerSize + 1; j++) {
                boolean isDisabledCell = (i == 0 && j == 0);
                boolean isInfoField = (i == 0 || j == 0);
                String toolTip = "";

                if (isInfoField) {
                    toolTip = !isDisabledCell ? (j == 0) ? ("a" + (i-1)): ("b" + (j-1)): "ai/bj" ;
                }else {
                    toolTip = "c" + (i-1) + ", " + (j-1);
                }
                JTextField field = createField(isDisabledCell, isInfoField, toolTip);
                row.add(field);
            }
            table.add(row);
        }

        final JScrollPane scroll = new JScrollPane(table);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!(evt.getNewValue() instanceof JComponent)) {
                    return;
                }
                JComponent focused = (JComponent)evt.getNewValue();
                if (scroll.isAncestorOf(focused)) {
                    int x1 = focused.getX() + focused.getParent().getX() + focused.getWidth();
                    int x2 = focused.getX() + focused.getParent().getX() - focused.getWidth();
                    int y1 = focused.getY() + focused.getParent().getY();
                    int y2 = focused.getY() + focused.getParent().getY();
                    Rectangle r1 = new Rectangle(x1, y1, focused.getWidth(), focused.getHeight());
                    Rectangle r2 = new Rectangle(x2, y2, focused.getWidth(), focused.getHeight());
                    Rectangle viewRectangle = scroll.getViewport().getViewRect();
                    boolean isOutOfView = !r1.intersects(viewRectangle)  || !r2.intersects(viewRectangle);
                    if (isOutOfView) {
                        scroll.getVerticalScrollBar().setValue(focused.getParent().getY() + focused.getY());
                        scroll.getHorizontalScrollBar().setValue(focused.getParent().getX() + focused.getX());
                    }
                }
            }
        });
        scroll.setPreferredSize(new Dimension(600, 400));
        return scroll;
    }

    @Override
    protected void onOkPressed() {
        if (!validateCells()) {
            if (firstInvalidField != null) {
                firstInvalidField.requestFocus();
                firstInvalidField = null;
            }
        }else {
            showSolutionFrame();
        }
    }

    private void backLightSelectedCell(boolean isSelected, TextCel field) {
        if (isSelected) {
            field.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        } else {
            field.resetBorder();
        }
    }

    private void showSolutionFrame() {
        this.setVisible(false);
        SolutionFrame solFrame = new SolutionFrame(this, getData(), consumerSize, providersSize);
        solFrame.setVisible(true);
    }

    private ArrayList<String> getData() {
        ArrayList<String> data = new ArrayList<>();
        for (TextCel field: fields) {
            data.add(field.getText());
        }
        return data;
    }

    private void backLightIncorrectCell(boolean correct, TextCel field) {
        if (!correct) {
            field.setBorder(BorderFactory.createLineBorder(Color.RED));
        }else {
            backLightSelectedCell(field.isFocusOwner(), field);
        }
    }

    private boolean validateCell(TextCel field) {
        boolean isEmpty = FieldHelper.isEmpty(field.getText());
        boolean isDisabled = !field.isEnabled();
        boolean isFieldValid = isDisabled ||
                FieldHelper.isNaturalNumber(field.getText()) || FieldHelper.isZero(field.getText());
        backLightIncorrectCell(isEmpty && !okPressed || isFieldValid, field);
        return isFieldValid;
    }

    private boolean validateCells() {
        boolean isValid = true;
        for (TextCel field: fields) {
            if (!validateCell(field)) {
                if (firstInvalidField == null) {
                    firstInvalidField = field;
                }
                isValid = false;
            }
        }
        return isValid;
    }
}
