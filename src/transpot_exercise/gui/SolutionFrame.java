package transpot_exercise.gui;

import transpot_exercise.solution.ConvertUtils;
import transpot_exercise.solution.PotencialMethodIteration;
import transpot_exercise.solution.Solution.OperationResult;
import transpot_exercise.solution.Solution;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;

public class SolutionFrame extends JFrame {
    private JFrame prevFrame;
    private double[][] cost;
    private double[] consumersNeeds;
    private double[] providerOffers;
    protected JButton ok;
    private JComboBox<String> methodsList;
    protected List<String> info = new ArrayList<>();

    private static final String EMPTY = "";
    private static final String NORTH_WEST_METHOD = "Метод северо-западного угла";
    private static final String MIN_METHOD = "Метод наименьшего элемента";

    public SolutionFrame(JFrame prevFrame, ArrayList<String> data, int consumersSize, int providersSize) {
        this(prevFrame);
        initData(data, consumersSize, providersSize);
        init();
    }

    protected SolutionFrame(JFrame prevFrame) {
        super("Транспортная задача");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.prevFrame = prevFrame;
    }

    protected Component createInfoLabel() {
        JLabel infoLabel = new JLabel("Выберите метод получения начального опорного плана");
        infoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        return infoLabel;
    }

    protected void init() {

        Box mainPanel = Box.createVerticalBox();

        JTextArea solInfo = new JTextArea();
        for (String s: info) {
            solInfo.append(s);
        }
        solInfo.setEditable(false);

        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(createResultTable(cost));
        mainPanel.add(solInfo);
        mainPanel.add(createButtonPanel());
        addSomeSpecificComponents(mainPanel);
        setContentPane(mainPanel);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    protected void addSomeSpecificComponents(Box mainPanel) {
        mainPanel.add(createInfoLabel());
        mainPanel.add(createInputPanel());
    }

    protected Component createResultTable(double[][] data) {
        final Box table = Box.createVerticalBox();
        int rows = providerOffers.length + 1;
        int cols = consumersNeeds.length + 1;
        for (int i = 0; i < rows; i++) {
            Box row = Box.createHorizontalBox();
            for (int j = 0; j < cols; j++) {
                final JTextField field;
                if (i == 0 || j == 0) {
                    boolean isDisabledCell = (i == 0 && j == 0);
                    final String toolTip = !isDisabledCell ? (j == 0) ? ("a" + (i-1)): ("b" + (j-1)): "ai/bj" ;

                    field = createField(isDisabledCell,toolTip);
                    if (!isDisabledCell) {
                        String text;
                        if (i == 0) {
                            text = String.valueOf(consumersNeeds[j - 1]);
                        }else {
                            text = String.valueOf(providerOffers[i -1]);
                        }
                        field.setText(text);
                        field.setEditable(false);
                    }
                }else {
                    field = createField(true, "");
                    String text = cost[i - 1][j - 1] + ")    " + (data != cost ? data[i - 1][j - 1] : "");
                    field.setText(text);
                    field.setToolTipText("c" + (i-1) + ", " + (j-1));
                }
                row.add(field);
            }
            table.add(row);
        }

        return new JScrollPane(table);
    }



    private void initData(List<String> data, int consumersSize, int providersSize)  {
        int i;

        //create cost, offers and needs lists
        List<List<Double>> costList = new ArrayList<>();
        for (i = 0; i < providersSize; i++) {
            costList.add(new ArrayList<Double>());
        }

        List<Double> consumers = new ArrayList<>();
        List<Double> providers = new ArrayList<>();

        //init needs list
        for (i = 0; i < consumersSize; i++) {
            consumers.add(Double.parseDouble(data.get(i)));
        }

        //init needs list and cost list
        int counter = 0;
        int cols = consumersSize + 1;
        for (i = consumersSize; i < data.size(); i++) {
            counter++;
            if (counter % cols == 1) {
                providers.add(Double.parseDouble(data.get(i)));
            }
            else {
                boolean endOfRow = counter % cols == 0;
                int provider = (endOfRow) ? (counter / cols - 1): (counter / cols);
                costList.get(provider).add(Double.parseDouble(data.get(i)));
            }
        }

        //all lists can be changed after Isolation
        this.info = Solution.makeIsolation(costList, consumers, providers).getInfo();
        this.consumersNeeds = ConvertUtils.singleListToPrimitiveArray(consumers);
        this.providerOffers = ConvertUtils.singleListToPrimitiveArray(providers);
        this.cost = ConvertUtils.doubleListToPrimitiveArray(costList);
    }

    protected TextCel createField(boolean isDisabled, String toolTip) {
        final TextCel field = new TextCel(5);
        if (!isDisabled) {
            field.setToolTipText(toolTip);
        }else {
            field.setText(toolTip);
        }

        field.setEnabled(!isDisabled);

        field.setHorizontalAlignment(JTextField.CENTER);

        return field;
    }

    protected void returnToPrevFrame() {
        dispose();
        prevFrame.setVisible(true);
        prevFrame.requestFocus();
    }

    protected Container createButtonPanel() {
        Box buttonPanel = Box.createHorizontalBox();

        JButton showAnswer = new JButton("показать ответ");
        showAnswer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showResult(true);
            }
        });
        showAnswer.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    showResult(true);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        JButton ok = new JButton("далее");
        this.ok = ok;
        ok.setEnabled(false);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOkPressed();
            }
        });
        ok.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onOkPressed();
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
        buttonPanel.add(showAnswer);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(ok);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(exit);
        return buttonPanel;
    }

    protected void onOkPressed() {
        OperationResult<double[][]> getFirstPlanResult = null;
        String selectedMethod = (String)methodsList.getSelectedItem();
        switch (selectedMethod) {
            case MIN_METHOD:
                getFirstPlanResult = Solution.MinMethod(consumersNeeds, providerOffers, cost);
                break;
            case NORTH_WEST_METHOD:
                getFirstPlanResult = Solution.NorthWestMethod(consumersNeeds, providerOffers, cost);
                break;
        }
        double[][] firstPlan = getFirstPlanResult.getResult();

        OperationResult<List<PotencialMethodIteration>> methodResult = Solution.potentialMethod(firstPlan, cost);
        ShowSolutionFrame nextFrame = new ShowSolutionFrame(this, methodResult, 0);
        nextFrame.setVisible(true);
        this.setVisible(false);
    }

    protected void showResult(boolean isFinalResult) {
        OperationResult<double[][]> getFirstPlanResult = Solution.MinMethod(consumersNeeds,
                providerOffers, cost);
        double[][] firstPlan = getFirstPlanResult.getResult();
        OperationResult<List<PotencialMethodIteration>> methodResult = Solution.potentialMethod(firstPlan, cost);
        List<PotencialMethodIteration> iterations = methodResult.getResult();
        int iterationIdx = isFinalResult ? iterations.size() - 1 : 0;
        ShowSolutionFrame answerFrame = new ShowSolutionFrame(SolutionFrame.this,  methodResult, iterationIdx);
        answerFrame.setVisible(true);
        SolutionFrame.this.setVisible(false);
    }

    private void reinitFrame(double[][] data) {
        Box mainPanel = Box.createVerticalBox();

        JTextArea solInfo = new JTextArea();
        for (String s: this.info) {
            solInfo.append(s);
        }
        solInfo.setEditable(false);

        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(createResultTable(data));
        mainPanel.add(solInfo);
        mainPanel.add(createButtonPanel());
        ok.setEnabled(true);

        setContentPane(mainPanel);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    protected Component createInputPanel() {
        Box inputPanel = Box.createHorizontalBox();

        methodsList = new JComboBox<>(new String[]{EMPTY, NORTH_WEST_METHOD, MIN_METHOD});
        methodsList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (methodsList.getSelectedItem().equals("")) {
                    ok.setEnabled(false);
                }else {
                    ok.setEnabled(true);
                }
            }
        });

        inputPanel.add(methodsList);
        return inputPanel;
    }

    private void clearResultInfo() {
        this.info.clear();
    }
}
