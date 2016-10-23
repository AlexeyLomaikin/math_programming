package transpot_exercise.solution;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PotencialMethodIteration{
    private double[][] curPlan;
    private double[][] cost;
    private Set<Solution.Point> eCoordinates;
    private Map<String, double[]> potencials;
    private Map<Solution.Point, Double> fakeCosts;
    private List<Solution.Point> cycledCells;

    PotencialMethodIteration() {
    }

    void setCurPlan(double[][] curPlan) {
        this.curPlan = curPlan;
    }
    void setCost(double[][] cost) {this.cost = cost;}
    void setECoordinates(Set<Solution.Point> eCoordinates) {
        this.eCoordinates = eCoordinates;
    }
    void setPotencials(Map<String, double[]> potencials) {
        this.potencials = potencials;
    }
    void setFakeCosts(Map<Solution.Point, Double> fakeCosts) {
        this.fakeCosts = fakeCosts;
    }
    void setCycledCells(List<Solution.Point> cycledCells) {
        this.cycledCells = cycledCells;
    }

    public double[][] getCurPlan() {
        return curPlan;
    }
    public double[][] getCost() {
        return cost;
    }
    public Set<Solution.Point> getECoordinates() {
        return eCoordinates;
    }
    public Map<String, double[]> getPotencials() {
        return potencials;
    }
    public Map<Solution.Point, Double> getFakeCosts() {
        return fakeCosts;
    }
    public List<Solution.Point> getCycledCells() {
        return cycledCells;
    }
}
