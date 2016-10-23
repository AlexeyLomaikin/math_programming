package transpot_exercise.solution;

import java.util.*;

/**
 * This class contains methods which do some calculations and
 * return OperationResult object with Object result of operation and List of String which explain all actions
 */
public class Solution {
    private Solution() {}

    public static final String A_POTENCIAL = "a";
    public static final String B_POTENCIAL = "b";
    public static final double E = 1e-10;

    public static class OperationResult<T> {
        private T result;
        private List<String> info;

        public OperationResult(T result, List<String> info) {
            this.result = result;
            this.info = info;
        }

        public T getResult() {
            return result;
        }

        public List<String> getInfo() {
            return info;
        }
    }

    public static class Point {
        private int x;
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }

        @Override
        public boolean equals(Object p) {
            if (p == null) return false;
            if (this == p) return true;
            if (p instanceof Point) {
                Point another = (Point)p;
                return this.x == another.x && this.y == another.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int res = 1;
            int k = 31;
            res = res * k + x;
            res = res * k + y;
            return res;
        }
    }

    /**
     *  add some zero shippings as E in current plan
     *  E > 0, E -> 0
     */
    private  static OperationResult<Set<Point>> resolveDegeneratePlan(double[][] curPlan) {
        StringBuilder logger = new StringBuilder();

        //Check plan degeneration
        int potencialsCount = curPlan.length + curPlan[0].length - 1;   // m + n - 1
        int basicCellsCount = 0;

        List<Point> zeroChippingCoordinates = new ArrayList<>();
        for (int i = 0; i < curPlan.length; i++) {
            for (int j = 0; j < curPlan[i].length; j++) {
                if (curPlan[i][j] >= 0 && curPlan[i][j] <= E) {
                    zeroChippingCoordinates.add(new Point(i, j));
                }else {
                    basicCellsCount++;
                }
            }
        }

        logValue(logger, potencialsCount, "m + n - 1");
        logValue(logger, basicCellsCount, "basicCellCount");

        Set<Point> eCoordinates = new HashSet<>();

        int cellsNeedToAddCount = potencialsCount - basicCellsCount;
        if (cellsNeedToAddCount == 0) {
            logger.append("План невырожденный\n");
            return new OperationResult<>(eCoordinates, Collections.singletonList(logger.toString()));
        }
        logger.append("План вырожденный\nДополним план до базисного, ");
        logger.append("пометив"  + cellsNeedToAddCount + " клеток как E:  E > 0, E-> 0\n");

        while (eCoordinates.size() < cellsNeedToAddCount) {
            int idxToAdd = (int)Math.round(Math.random() * (zeroChippingCoordinates.size() - 1));

            if (idxToAdd >= zeroChippingCoordinates.size())
                continue;

            Point zeroShipping = zeroChippingCoordinates.get(idxToAdd);

            if (eCoordinates.contains(zeroShipping))
                continue;

            eCoordinates.add(zeroShipping);
        }

        List<String> logs = Collections.singletonList(logger.toString());
        return new OperationResult<>(eCoordinates, logs);
    }

    //init equations to calc potencials
    private static Map<Point, Double> initEquations(double[][] cost, double[][] curPlan, Set<Point> eCoordinates) {
        Map<Point, Double> equations = new HashMap<>();
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[i].length; j++) {
                if (curPlan[i][j] > E || eCoordinates.contains(new Point(i, j))) {
                    equations.put(new Point(i, j), cost[i][j]);
                }
            }
        }
        return equations;
    }

    /**
     * @param eCoordinates - coordinates of zero shipping wich included in basic cells
     * @return OperationResult wich contains logs and A, B potencials
     **/
    private static OperationResult<Map<String, double[]>> getPotencials(double[][] curPlan, double[][] cost,
                                                                        Set<Point> eCoordinates) {
        StringBuilder logger = new StringBuilder();
        String startString = "\nПроизводим рассчет потенциалов:\nA0 = 0\n";
        logger.append(startString);

        int offersSize = cost.length;
        int needsSize = cost[0].length;
        int potencialCount = offersSize + needsSize;

        //init equations: Ai + Bj = C[i,j] for X[i, j] > 0
        Map<Point, Double> equations = initEquations(cost, curPlan, eCoordinates);

        double[] aPotencial = new double[offersSize];
        double[] bPotencial = new double[needsSize];

        //put aPotencial Idx as x and bPotencial Idx as y + offersSize
        Set<Integer> solvedIdxs = new HashSet<>();
        solvedIdxs.add(0);                                //A1 = 0

        int iteration = 0;
        while(solvedIdxs.size() != potencialCount) {
            for (Point equationVariables: equations.keySet()) {
                if (solvedIdxs.size() == potencialCount)
                    break;

                //check for infinity loop
                //reinit eCells, equations and restart "for" cycle
                if (iteration > potencialCount * equations.size()) {
                    iteration = 0;

                    logger = new StringBuilder();
                    logger.append(startString);

                    eCoordinates.clear();
                    eCoordinates.addAll(Solution.resolveDegeneratePlan(curPlan).getResult());
                    equations = initEquations(cost, curPlan, eCoordinates);

                    solvedIdxs.clear();
                    solvedIdxs.add(0);
                    break;
                }
                iteration++;

                int x = equationVariables.getX();
                int y = equationVariables.getY();
                boolean isAxResolved = solvedIdxs.contains(x);
                boolean isByResolved = solvedIdxs.contains(y + offersSize);

                //if both variables unresolved - go to next equlation
                if (!isAxResolved && !isByResolved)
                    continue;

                //if both variables resolved - go to next equalation
                if (isAxResolved && isByResolved)
                    continue;

                double price = equations.get(equationVariables);

                logger.append("A[" + x + "] + " );
                logger.append("B[" + y + "] = ");
                logger.append(price + "    ");

                if (isAxResolved) {
                    bPotencial[y] = price - aPotencial[x];
                    logger.append("B[" + y + "] = ");
                    logger.append(price + " - " + "A" + x);
                    logger.append(" = " + bPotencial[y] + "\n");
                }else {
                    aPotencial[x] = price - bPotencial[y];
                    logger.append("A[" + x + "] = ");
                    logger.append(price + " - " + "B" + y);
                    logger.append(" = " + aPotencial[x] + "\n");
                }
                solvedIdxs.add(isAxResolved ? y + offersSize : x);
            }
        }

        Map<String, double[]> res = new HashMap<>();
        res.put(A_POTENCIAL, aPotencial);
        res.put(B_POTENCIAL, bPotencial);
        return new OperationResult<>(res, Collections.singletonList(logger.toString()));
    }

    private static Set<Point> findNotOptimalCells(double[][] curPlan, Set<Point> eCoordinates,
                                                  Map<String, double[]> potencials, double[][] cost) {
        Set<Point> notOptimalCells = new HashSet<>();
        Map<Point, Double> fakeCost = Solution.getFakeCost(curPlan, eCoordinates, cost, potencials);

        for (Point zeroShipping: fakeCost.keySet()) {
            int x = zeroShipping.getX();
            int y = zeroShipping.getY();
            double fakePrice = fakeCost.get(zeroShipping);
            double diffPrice = fakePrice - cost[x][y];
            if (diffPrice > 0.0) {
                notOptimalCells.add(zeroShipping);
            }
        }
        return notOptimalCells;
    }

    public static Map<Point, Double> getFakeCost(double[][] curPlan, Set<Point> eCoordinates,
                                                 double[][] cost, Map<String, double[]> potencials) {
        Map<Point, Double> fakeCost = new HashMap<>();
        double[] aPotencials = potencials.get(A_POTENCIAL);
        double[] bPotencials = potencials.get(B_POTENCIAL);

        for (int x = 0; x < cost.length; x++) {
            for (int y = 0; y < cost[x].length; y++) {
                Point curCell = new Point(x, y);
                if (curPlan[x][y] <= E && !eCoordinates.contains(curCell)) {
                    double fakePrice = aPotencials[x] + bPotencials[y];
                    fakeCost.put(curCell, fakePrice);
                }
            }
        }
        return fakeCost;
    }

    //try to found cycle for some not optimal cell
    private static OperationResult<List<Point>> findCycleForSomeNotOptimalCell(double[][] curPlan,
                                                                               Set<Point> eCoordinates,
                                                                               Set<Point> notOptimalShippings) {
        boolean found = false;
        List<Point> cycledCells = Collections.emptyList();

        while (!found) {
            Iterator<Point> iterator = notOptimalShippings.iterator();
            if (!iterator.hasNext())
                break;

            Point notOptimalShiping = iterator.next();
            iterator.remove();

            //make cycle for not optimal cell
            cycledCells = new ArrayList<>(Solution.getCycle(notOptimalShiping, curPlan, eCoordinates));
            if (!cycledCells.isEmpty()) {
                found = true;
            } else {
                cycledCells = Collections.emptyList();
            }
        }
        return new OperationResult<>(cycledCells, Collections.<String>emptyList());
    }

    private static double findMinNegativeShippingValue(double[][] curPlan, List<Point> cycledCells) {
        if (cycledCells.size() <= 1)
            throw new IllegalArgumentException("Incorrect cycle: cycledCells size can't be <= 1");

        Point firstNegativeCell = cycledCells.get(1);
        double minNegShippingValue = curPlan[firstNegativeCell.getX()][firstNegativeCell.getY()];
        for (int i = 0; i < cycledCells.size(); i++) {
            Point shipping = cycledCells.get(i);
            int x = shipping.getX();
            int y = shipping.getY();

            if (i % 2 != 0 && curPlan[x][y] < minNegShippingValue) {
                minNegShippingValue = curPlan[x][y];
            }
        }
        return minNegShippingValue;
    }

    /**
     *
     * @return Point - cell to remove from basis after plan recalculation
     * or null if such cell not found
     */
    private static Point findCellToRemoveFromBasis(double[][] curPlan, double[][] cost,
                                                   List<Point> cycledCells, double minNegShippingValue) {
        Point removedShipping = null;
        double maxCost = 0.0;
        for (int i = 0; i < cycledCells.size(); i++) {
            Point cycledShipping = cycledCells.get(i);
            int x = cycledShipping.getX();
            int y = cycledShipping.getY();
            if (i % 2 != 0 && curPlan[x][y] == minNegShippingValue && cost[x][y] >= maxCost) {
                maxCost = cost[x][y];
                removedShipping = new Point(x, y);
            }
        }
        return removedShipping;
    }

    private static OperationResult<Void> recalculatePlanOfShipping(double[][] curPlan, double[][] cost,
                                                                   Set<Point> eCoordinates, List<Point> cycledCells) {
        StringBuilder logger = new StringBuilder();
        logger.append("\nПроизводим пересчет перевозок: \n");

        //find min of all "negative" shippings
        double minNegShippingValue = Solution.findMinNegativeShippingValue(curPlan, cycledCells);
        logger.append("Минимум из \"отрицательных перевозок\":  " + minNegShippingValue + "\n");

        //find "negative" shipping with min value and max cost
        Point removedShipping = Solution.findCellToRemoveFromBasis(curPlan, cost,
                cycledCells, minNegShippingValue);

        for (int i = 0; i < cycledCells.size(); i++) {
            Point cycledShipping = cycledCells.get(i);
            int x = cycledShipping.getX();
            int y = cycledShipping.getY();

            logger.append("x" + x + "," + y + " = ");
            if (i % 2 == 0) {
                logger.append(curPlan[x][y] + " + " + minNegShippingValue);

                if (eCoordinates.contains(cycledShipping) && minNegShippingValue > E) {
                    eCoordinates.remove(cycledShipping);
                }
                curPlan[x][y] += minNegShippingValue;

                logger.append(" = " + curPlan[x][y] + "\n");
            }else {
                //if we have many shippings with equal min values ...
                // we must exclude only one of them
                if (!eCoordinates.contains(cycledShipping)) {
                    logger.append(curPlan[x][y] + " - " + minNegShippingValue);

                    if (curPlan[x][y] == minNegShippingValue && !cycledShipping.equals(removedShipping)) {
                        eCoordinates.add(cycledShipping);
                    }
                    curPlan[x][y] = Math.abs(curPlan[x][y] - minNegShippingValue);

                    logger.append(" = " + curPlan[x][y] + "\n");
                }else {
                    logger.append("E - E = 0.0");
                    if (cycledShipping.equals(removedShipping))
                        eCoordinates.remove(cycledShipping);
                }
            }
        }
        return new OperationResult<>(null, Collections.singletonList(logger.toString()));
    }

    public static OperationResult<List<PotencialMethodIteration>> potentialMethod(double[][] xPlan, double[][] cost) {
        List<String> infoList = new ArrayList<>();
        StringBuilder logger = new StringBuilder();
        double[][] curPlan = ConvertUtils.copyOf(xPlan);

        //check plan's degeneration
        OperationResult<Set<Point>> resolveResult = Solution.resolveDegeneratePlan(curPlan);
        Set<Point> eCoordinates = resolveResult.getResult();
        for (String info: resolveResult.getInfo()) {
            logger.append(info);
        }

        List<PotencialMethodIteration> iterations = new ArrayList<>();
        boolean optimal = false;

        while (!optimal) {
            PotencialMethodIteration iteration = new PotencialMethodIteration();
            iteration.setCurPlan(ConvertUtils.copyOf(curPlan));
            iteration.setCost(cost);

            double L = calcL(cost, curPlan);
            logValue(logger, L, "L");

            //calculate potencials
            OperationResult<Map<String, double[]>> getPotencialsResult = Solution.getPotencials(curPlan,
                    cost, eCoordinates);
            Map<String, double[]> potencials = getPotencialsResult.getResult();
            for (String s: getPotencialsResult.getInfo()) {
                logger.append(s);
            }

            //find not optimal cells
            Set<Point> notOptimalShipings = Solution.findNotOptimalCells(curPlan,eCoordinates,
                    potencials, cost);

            //plan isn't optimal
            if (notOptimalShipings.size() > 0) {
                //try found cycle for some not optimal cell
                //if not found - reinit eCoordinates and go to next interation
                OperationResult<List<Point>> findResult = Solution.findCycleForSomeNotOptimalCell(curPlan,
                        eCoordinates, notOptimalShipings);
                List<Point> cycledCells = findResult.getResult();
                if (cycledCells.isEmpty()) {
                    logger = new StringBuilder();
                    resolveResult = Solution.resolveDegeneratePlan(curPlan);
                    eCoordinates = resolveResult.getResult();
                    logger.append(resolveResult.getInfo());
                    continue;
                }

                //logging choosen not optimal cell
                Point startCycleCell = cycledCells.get(0);
                int startCycleX = startCycleCell.getX();
                int starCycleY = startCycleCell.getY();
                Map<Point, Double> fakeCost = Solution.getFakeCost(curPlan, eCoordinates, cost, potencials);
                double costDiff = fakeCost.get(startCycleCell) - cost[startCycleX][starCycleY];
                logger.append("\nПлан неоптимален.\n");
                logger.append("\nИз неоптимальных клеток, для которых fakeCi,j > Ci,j, выбираем: \n");
                logger.append("x" + startCycleX + "," + starCycleY + ": ");
                logger.append("fC" + startCycleX + "," + starCycleY + " - ");
                logger.append("C" + startCycleX + "," + starCycleY + " = " + costDiff + "\n");

                iteration.setECoordinates(new HashSet<Point>(eCoordinates));
                iteration.setPotencials(potencials);
                iteration.setFakeCosts(fakeCost);
                iteration.setCycledCells(cycledCells);

                //recalculate plan of shippings
                OperationResult<Void> recalcResult = Solution.recalculatePlanOfShipping(curPlan, cost, eCoordinates,
                        cycledCells);
                for (String info: recalcResult.getInfo())
                    logger.append(info);
            }else {
                iteration.setECoordinates(new HashSet<Point>(eCoordinates));
                iteration.setPotencials(potencials);
                iteration.setFakeCosts(Solution.getFakeCost(curPlan, eCoordinates, cost, iteration.getPotencials()));
                logger.append("\nВсе Ci,j >= fCi,j: План оптимален\n");
                optimal = true;
            }
            iterations.add(iteration);
            infoList.add(logger.toString());
            logger = new StringBuilder();
        }

        double L = calcL(cost, curPlan);
        return new OperationResult<>(iterations, infoList);
    }

    private static Set<Point> getCycle(Point startZeroShipping, double[][] curPlan,
                                       Set<Point> eCoordinates) {
        int startX = startZeroShipping.getX();
        int prevX = startX - 1;
        int nextX = startX + 1;

        int startY = startZeroShipping.getY();
        int prevY = startY - 1;
        int nextY = startY + 1;

        int xLength = curPlan.length;
        int yLength = curPlan[0].length;

        Set<Point> choosenCells = new LinkedHashSet<>();
        choosenCells.add(new Point(startX, startY));

        List<String> logs = new ArrayList<>();
        boolean cycleMaked = false;

        if (prevX >= 0) {
            cycleMaked  = makeCycle(startZeroShipping, new Point(prevX, startY), curPlan, eCoordinates, choosenCells);
        }

        if (cycleMaked)
            return choosenCells;

        if (nextX < xLength) {
            cycleMaked = makeCycle(startZeroShipping, new Point(nextX, startY), curPlan, eCoordinates, choosenCells);
        }

        if (cycleMaked)
            return choosenCells;

        if (prevY >= 0) {
            cycleMaked = makeCycle(startZeroShipping, new Point(startX, prevY), curPlan, eCoordinates, choosenCells);
        }

        if (cycleMaked)
            return choosenCells;

        if (nextY < yLength) {
            cycleMaked = makeCycle(startZeroShipping, new Point(startX, nextY), curPlan, eCoordinates, choosenCells);
        }

        if (!cycleMaked)
            choosenCells = Collections.emptySet();

        return choosenCells;
    }

    private static boolean makeCycle(Point prevShipping, Point curShipping, double[][] curPlan,
                                     Set<Point> eCoordinates, Set<Point> choosenCells) {
        Point startZeroShipping = choosenCells.iterator().next();
        if (curShipping.equals(startZeroShipping)) {
            return true;
        }

        int x = curShipping.getX();
        int x1 = x - 1;
        int x2 = x + 1;
        boolean isPrevUp = (x - 1 == prevShipping.getX());
        boolean isPrevDown = (x + 1 == prevShipping.getX());

        int y = curShipping.getY();
        int y1 = y - 1;
        int y2 = y + 1;
        boolean isPrevLeft = (y - 1 == prevShipping.getY());
        boolean isPrevRight = (y + 1 == prevShipping.getY());

        boolean isBasicCurCell = curPlan[x][y] > E || eCoordinates.contains(curShipping);

        int xLength = curPlan.length;
        int yLength  = curPlan[0].length;
        int prevShippingX = prevShipping.getX();
        int prevShippingY = prevShipping.getY();

        boolean cycleMaked = false;

        if (y2 < yLength && y2 != prevShippingY) {
            Point nextCell = new Point(x, y2);

            if (!choosenCells.contains(nextCell) || nextCell.equals(startZeroShipping)) {
                if (isPrevUp || isPrevDown) {
                    if (isBasicCurCell) {
                        choosenCells.add(curShipping);
                        cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                    }else {
                        cycleMaked = false;
                    }
                }else {
                    cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                }
            }
        }
        if (cycleMaked) {
            return true;
        }
        choosenCells.remove(curShipping);

        if (y1 >= 0 && y1 != prevShippingY) {
            Point nextCell = new Point(x, y1);

            if (!choosenCells.contains(nextCell) || nextCell.equals(startZeroShipping)){
                if (isPrevUp || isPrevDown) {
                    if (isBasicCurCell) {
                        choosenCells.add(curShipping);
                        cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                    }else {
                        cycleMaked = false;
                    }
                }else {
                    cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                }
            }
        }

        if (cycleMaked) {
            return true;
        }
        choosenCells.remove(curShipping);

        if (x2 < xLength && x2 != prevShippingX) {
            Point nextCell = new Point(x2, y);

            if (!choosenCells.contains(nextCell) || nextCell.equals(startZeroShipping)) {
                if (isPrevLeft || isPrevRight) {
                    if (isBasicCurCell) {
                        choosenCells.add(curShipping);
                        cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                    }else {
                        cycleMaked = false;
                    }
                }else {
                    cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                }
            }
        }

        if (cycleMaked) {
            return true;
        }
        choosenCells.remove(curShipping);

        if (x1 >= 0 && x1 != prevShippingX) {
            Point nextCell = new Point(x1, y);

            if (!choosenCells.contains(nextCell) || nextCell.equals(startZeroShipping)) {
                if (isPrevLeft || isPrevRight) {
                    if (isBasicCurCell) {
                        choosenCells.add(curShipping);
                        cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                    } else {
                        cycleMaked = false;
                    }
                } else {
                    cycleMaked = makeCycle(curShipping, nextCell, curPlan, eCoordinates, choosenCells);
                }
            }
        }

        if (cycleMaked) {
            return true;
        }
        choosenCells.remove(curShipping);

        return false;
    }

    public static OperationResult<double[][]> MinMethod(double[] consumersNeeds, double[] providersOffers,
                                                        double[][] cost) {

        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("Метод мин. элемента\n\n");
        infoBuilder.append("Начальный опорный план:\n");
        double[] needs = Arrays.copyOf(consumersNeeds, consumersNeeds.length);
        double[] offers = Arrays.copyOf(providersOffers, providersOffers.length);
        double[][] xPlan = new double[providersOffers.length][consumersNeeds.length];

        //for not zero cost: sort all prices ascending
        Set<Double> sortedCost = new TreeSet<>();
        List<Point> zeroCostIdxs = new ArrayList<>();
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[i].length; j++) {
                if (cost[i][j] > 0.0) {
                    sortedCost.add(cost[i][j]);
                }else {
                    zeroCostIdxs.add(new Point(i, j));
                }
            }
        }

        //execute algorithm from min price to max price
        //also we check all equal prices
        for (Double price: sortedCost) {
            List<Point> foundIdxs = new ArrayList<>();
            for (int i = 0; i < cost.length; i++) {
                for (int j = 0; j < cost[i].length; j++) {
                    if (Math.abs(price - cost[i][j]) >= 0 && Math.abs(price - cost[i][j]) <= E) {
                        foundIdxs.add(new Point(i, j));
                    }
                }
            }
            for (Point p: foundIdxs) {
                int iIdx = p.getX();
                int jIdx = p.getY();
                if (offers[iIdx]  <= E || needs[jIdx] <= E) continue;
                xPlan[iIdx][jIdx] = Math.min(offers[iIdx], needs[jIdx]);
                offers[iIdx] = Math.abs(offers[iIdx] - xPlan[iIdx][jIdx]);
                needs[jIdx] = Math.abs(needs[jIdx] - xPlan[iIdx][jIdx]);

                loggingCurValues(iIdx, jIdx, xPlan, cost, needs, offers, infoBuilder);
            }
        }

        //check zero prices (added for isolation)
        for (Point zeroCost: zeroCostIdxs) {
            int i = zeroCost.getX();
            int j = zeroCost.getY();
            if (offers[i] > E && needs[j] > E) {
                xPlan[i][j] = Math.min(offers[i], needs[j]);
                offers[i] = Math.abs(offers[i] - xPlan[i][j]);
                needs[j] = Math.abs(needs[j] - xPlan[i][j]);

                loggingCurValues(i, j, xPlan, cost, needs, offers, infoBuilder);
            }
        }

        double L = calcL(cost, xPlan);
        infoBuilder.append("\n L = ");
        infoBuilder.append(L);

        ArrayList<String> infoList = new ArrayList<>();
        infoList.add(infoBuilder.toString());
        return new OperationResult<>(xPlan, infoList);
    }

    public static OperationResult<double[][]> NorthWestMethod(double[] consumerNeeds, double[] providersOffers,
                                                              double[][] cost) {
        StringBuilder info = new StringBuilder();
        info.append("Метод северо-западного угла\n\n");
        info.append("Начальный опорный план:\n");
        double[] needs = Arrays.copyOf(consumerNeeds, consumerNeeds.length);
        double[] offers = Arrays.copyOf(providersOffers, providersOffers.length);
        double[][] xPlan = new double[offers.length][needs.length];

        int i = 0, j = 0;
        while (j < needs.length && i < offers.length) {
            xPlan[i][j] = Math.min(needs[j], offers[i]);
            needs[j] = Math.abs(needs[j] - xPlan[i][j]);
            offers[i] = Math.abs(offers[i] - xPlan[i][j]);
            loggingCurValues(i, j, xPlan, null, needs, offers, info);

            if (needs[j] <= E) {
                j++;
            }
            if (offers[i] <= E) {
                i++;
            }
        }
        double L = calcL(cost, xPlan);
        info.append("\n L = ");
        info.append(L);

        List<String> infoList = new ArrayList<>();
        infoList.add(info.toString());
        return new OperationResult<>(xPlan, infoList);
    }

    public static OperationResult<Void> makeIsolation(List<List<Double>> cost,
                                                      List<Double> needs, List<Double> offers) {
        StringBuilder info = new StringBuilder();

        info.append("sumA = ");

        OperationResult<Double> sumResultA = Solution.sum(offers);
        double sumA = sumResultA.getResult();
        List<String> logs = sumResultA.getInfo();

        for (String s: logs) {
            info.append(s);
        }
        info.append("\n");

        info.append("sumB = ");

        OperationResult<Double> sumResultB = Solution.sum(needs);
        double sumB = sumResultB.getResult();
        logs = sumResultB.getInfo();

        for (String s: logs) {
            info.append(s);
        }
        info.append("\n");

        if (Math.abs(sumA - sumB) >= 0 && Math.abs(sumA - sumB) <= E) {
            info.append("Задача замкнута");
        } else {
            info.append("\nПриведем задачу к замкнутому виду:\n");
            if (sumA > sumB) {
                needs.add(Math.abs(sumA - sumB));
                for (int i = 0; i < cost.size(); i++) {
                    cost.get(i).add(0.0);
                }
                int addedNeedIdx = needs.size() - 1;

                info.append("b" + addedNeedIdx);
                info.append(" = sumA - sumB = ");
                info.append(needs.get(addedNeedIdx));

                info.append("\nci,");
                info.append(addedNeedIdx + " = 0,");
                info.append("i = 0..." + (offers.size() - 1));
            } else {
                offers.add(Math.abs(sumB - sumA));
                cost.add(new ArrayList<Double>());
                List<Double> addedOffer = cost.get(cost.size() - 1);
                for (int i = 0; i < needs.size(); i++) {
                    addedOffer.add(0.0);
                }

                int addedOfferIdx = offers.size() - 1;
                info.append("a" + addedOfferIdx);
                info.append(" = sumB - sumA = ");
                info.append(offers.get(addedOfferIdx));

                info.append("\nc" + addedOfferIdx);
                info.append(",j = 0, j = 0...");
                info.append(needs.size() - 1);
            }
        }


        List<String> infoList = Collections.singletonList(info.toString());
        return new OperationResult<>(null, infoList);
    }

    public static OperationResult<Double> sum(List<Double> list) {
        StringBuilder infoBuilder = new StringBuilder();
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            double num = list.get(i);
            sum += num;

            infoBuilder.append(num);
            if (i != list.size() - 1) {
                infoBuilder.append(" + ");
            }
        }
        infoBuilder.append(" = ");
        infoBuilder.append(sum);

        List<String> infoList = Collections.singletonList(infoBuilder.toString());
        return new OperationResult<>(sum, infoList);
    }

    public static double calcL(double[][] cost, double[][] xPlan) {
        double L = 0;
        for (int i = 0; i < xPlan.length; i++) {
            for (int j = 0; j < xPlan[i].length; j++) {
                if (xPlan[i][j] > E) {
                    L += cost[i][j] * xPlan[i][j];
                }
            }
        }
        return L;
    }

    private static void loggingCurValues(int i, int j, double[][]xPlan, double[][]cost, double[] needs, double[] offers,
                                         StringBuilder infoBuilder) {
        if (infoBuilder == null) return;
        if (cost != null) {
            infoBuilder.append("c[" + i + "][" + j + "] = ");
            infoBuilder.append(cost[i][j]);
            infoBuilder.append("    ");
        }
        if (xPlan != null) {
            infoBuilder.append("x[" + i + "][" + j + "] = ");
            infoBuilder.append(xPlan[i][j]);
            infoBuilder.append("    ");
        }
        if (needs != null) {
            infoBuilder.append("b" + j + " = ");
            infoBuilder.append(needs[j] + "    ");
        }
        if (offers != null) {
            infoBuilder.append("a" + i + " = ");
            infoBuilder.append(offers[i]);
            infoBuilder.append("\n");
        }
    }

    private static void logValue(StringBuilder logger, double value, String title) {
        logger.append(title);
        logger.append(" = ");
        logger.append(value);
        logger.append("\n");
    }
}

