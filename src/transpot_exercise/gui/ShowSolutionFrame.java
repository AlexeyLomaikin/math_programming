package transpot_exercise.gui;

import transpot_exercise.solution.PotencialMethodIteration;
import transpot_exercise.solution.Solution;

import javax.swing.*;
import java.awt.*;
import transpot_exercise.solution.Solution.Point;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by AlexL on 23.10.2016.
 */
public class ShowSolutionFrame extends SolutionFrame {
    private int iterationIdx;
    private Solution.OperationResult<List<PotencialMethodIteration>> result;
    private List<PotencialMethodIteration> iterations;

    public ShowSolutionFrame(JFrame prevFrame,
                             Solution.OperationResult<List<PotencialMethodIteration>> result,
                             int iterationIdx) {
        super(prevFrame);
        this.result = result;
        this.iterations = result.getResult();
        this.iterationIdx = iterationIdx;
        this.info = Collections.singletonList(result.getInfo().get(iterationIdx));
        super.init();
        setResizable(true);
        ok.setEnabled(true);
    }

    @Override
    protected Container createButtonPanel() {
        Container buttonPanel = super.createButtonPanel();
        if (iterationIdx == iterations.size() - 1) {
            for (int i = 0; i < 4; i++) {
                buttonPanel.remove(0);
            }
        }
        return buttonPanel;
    }

    @Override
    protected Component createResultTable(double[][] data) {
        final Box table = Box.createVerticalBox();

        PotencialMethodIteration curIteration = iterations.get(iterationIdx);
        Map<String, double[]> potencials = curIteration.getPotencials();
        double[][] cost = curIteration.getCost();
        double[][] curPlan = curIteration.getCurPlan();
        Set<Point> eCoordinates = curIteration.getECoordinates();
        List<Point> cycledCells = curIteration.getCycledCells();
        Map<Point, Double> fakeCost = curIteration.getFakeCosts();

        int rows = potencials.get(Solution.A_POTENCIAL).length + 1;
        int cols = potencials.get(Solution.B_POTENCIAL).length + 1;
        for (int i = 0; i < rows; i++) {
            Box row = Box.createHorizontalBox();
            for (int j = 0; j < cols; j++) {
                final JTextField field;
                if (i == 0 || j == 0) {
                    boolean isDisabledCell = (i == 0 && j == 0);
                    final String toolTip = !isDisabledCell ? (j == 0) ? ("a" + (i-1)): ("b" + (j-1)): "Ai/Bj" ;

                    field = createField(isDisabledCell,toolTip);
                    if (!isDisabledCell) {
                        String text;
                        if (i == 0) {
                            text = String.valueOf(potencials.get(Solution.B_POTENCIAL)[j - 1]);
                        }else {
                            text = String.valueOf(potencials.get(Solution.A_POTENCIAL)[i -1]);
                        }
                        field.setText(text);
                        field.setEditable(false);
                    }
                }else {
                    field = createField(true, "");
                    Point curCell = new Point(i - 1, j -1);

                    String shippingString = "";
                    if (!eCoordinates.contains(curCell)) {
                        if (curPlan[i-1][j-1] > Solution.E)
                            shippingString = String.valueOf(curPlan[i-1][j-1]);
                        else
                            shippingString = "";
                    }else {
                        shippingString = "E";
                    }

                    if (cycledCells != null && cycledCells.contains(curCell)) {
                        int cycledCellIdx = cycledCells.indexOf(curCell);
                        if (cycledCellIdx % 2 == 0)
                            field.setBackground(Color.RED);
                        else
                            field.setBackground(Color.BLUE);
                    }
                    String text = cost[i - 1][j - 1] + ")  " + shippingString;
                    if (fakeCost.containsKey(curCell)) {
                        text += "  (" + fakeCost.get(curCell);
                    }
                    field.setText(text);
                    field.setToolTipText("x" + (i-1) + ", " + (j-1));
                }
                row.add(field);
            }
            table.add(row);
        }

        return new JScrollPane(table);
    }

    @Override
    protected void addSomeSpecificComponents(Box mainPanel) {

    }

    @Override
    protected TextCel createField(boolean isDisabled, String tooltip) {
        TextCel field = super.createField(isDisabled, tooltip);
        field.setFont(new Font("SansSerif", Font.ITALIC, 16));
        return field;
    }

    @Override
    protected void showResult(boolean isFinalResult) {
        ShowSolutionFrame lastFrame = new ShowSolutionFrame(this, result, iterations.size() - 1);
        this.setVisible(false);
        lastFrame.setVisible(true);
    }

    @Override
    protected void onOkPressed() {
        ShowSolutionFrame nextFrame = new ShowSolutionFrame(this, result, iterationIdx + 1);
        nextFrame.setVisible(true);
        this.setVisible(false);
    }
}
