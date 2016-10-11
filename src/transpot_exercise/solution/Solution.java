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
    private static double E = 0.00001;

    public static void setE(double E) {
        Solution.E = E;
    }
    public static double getE() {
        return Solution.E;
    }


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

    private static class Point {
        private int x;
        private int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        int getX() {
            return x;
        }
        int getY() {
            return y;
        }

        @Override
        public boolean equals(Object p) {
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
     * @param eXCoordinates - coordinates of zero shipping in array which must be replaced with E: E > 0 && E -> 0
     * We need to solve the sytem of equlation: Ai + Bj = c[i, j] for X[i, j] > 0; A1 = 0
     * A1...Am, B1...Bn, i = 1...m, j = 1..m
    where m - offers size, n - needs size, Ai, Bi - potencials
     @return OperationResult wich contains logs and A, B potencials
     **/
    private static OperationResult<Map<String, double[]>> getPotencials(double[][] curPlan, double[][] cost,
                                                                     Point[] eXCoordinates) {
        ArrayList<String> infoList = new ArrayList<>();
        StringBuilder info = new StringBuilder();

        Set<Point> eCoordinates = null;
        if (eXCoordinates != null && eXCoordinates.length != 0) {
            eCoordinates = new HashSet<>();
            Collections.addAll(eCoordinates, eXCoordinates);
        };

        boolean isPlanDegenerate = eCoordinates != null;

        int offersSize = cost.length;
        int needsSize = cost[0].length;

        //init equalations: Ai + Bj = C[i,j] for X[i, j] > 0
        Map<Point, Integer> equalations = new HashMap<>();
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[i].length; j++) {
                if (curPlan[i][j] != 0 || (isPlanDegenerate && eCoordinates.contains(new Point(i, j)))) {
                    equalations.put(new Point(i, j), cost[i][j]);
                }
            }
        }

        int potencialCount = offersSize + needsSize;

        if ((potencialCount - 1) != equalations.size()) {
            throw new IllegalArgumentException("m + n - 1 != basicCellSize: incorrect eXCoordinates List");
        }

        int[] aPotencial = new int[offersSize];
        int[] bPotencial = new int[needsSize];

        //put x Idx as x and y Idx as y + offersSize
        Set<Integer> solvedIdxs = new HashSet<>();
        solvedIdxs.add(0);                                //A1 = 0
        aPotencial[0] = 0;

        info.append("Производим рассчет потенциалов:\n");
        info.append("A0 = 0\n");
        while(solvedIdxs.size() != potencialCount) {
            for (Point equlationVariables: equalations.keySet()) {
                if (solvedIdxs.size() == potencialCount) break;

                int x = equlationVariables.getX();
                int y = equlationVariables.getY();
                boolean isAxResolved = solvedIdxs.contains(x);
                boolean isByResolved = solvedIdxs.contains(y + offersSize);

                //if both variables unresolved - go to next equlation
                if (!isAxResolved && !isByResolved) {
                    continue;
                }

                //if both variables resolved - go to next equalation
                if (isAxResolved && isByResolved) {
                    continue;
                }

                int price = equalations.get(equlationVariables);

                info.append("A[" + x + "] + " );
                info.append("B[" + y + "] = ");
                info.append(price + "    ");

                if (isAxResolved) {
                    bPotencial[y] = price - aPotencial[x];
                    info.append("B[" + y + "] = ");
                    info.append(price + " - " + "A" + x);
                    info.append(" = " + bPotencial[y] + "\n");
                }else {
                    aPotencial[x] = price - bPotencial[y];
                    info.append("A[" + x + "] = ");
                    info.append(price + " - " + "B" + y);
                    info.append(" = " + aPotencial[x] + "\n");
                }
                solvedIdxs.add(isAxResolved ? y + offersSize : x);
            }
        }

        Map<String, int[]> res = new HashMap<>();
        res.put(A_POTENCIAL, aPotencial);
        res.put(B_POTENCIAL, bPotencial);
        return new OperationResult<>(res, infoList);
    }

    public static OperationResult<List<int[][]>> PotentialMethod(int[][] xPlan, int[][] cost) {
        ArrayList<String> infoList = new ArrayList<>();
        List<int[][]> allPlans = new ArrayList<>();
        boolean optimal = false;
        int[][] curPlan = ConvertUtils.copyOf(xPlan);

        while (!optimal) {
            StringBuilder logger = new StringBuilder();

            //Check plan degeneration
            int checkCount = cost.length + cost[0].length - 1;
            int basicCellCount = 0;
            //sorted set of cost for zero shipping: c[i][j] for x[i][j] == 0
            Set<Integer> zeroXCost = new TreeSet<>();
            Set<Point> zeroXs = new HashSet<>();

            for (int i = 0; i < curPlan.length; i++) {
                for (int j = 0; j < curPlan[i].length; j++) {
                    if (curPlan[i][j] != 0) {
                        basicCellCount++;
                    } else {
                        zeroXCost.add(cost[i][j]);
                        zeroXs.add(new Point(i, j));
                    }
                }
            }

            logValue(logger, checkCount, "m + n - 1");
            logValue(logger, basicCellCount, "basicCellCount");

            int diff = Math.abs(basicCellCount - checkCount);

            if (diff == 0) {
                logger.append("План невырожденный");
                OperationResult<Map<String, int[]>> result = Solution.getPotencials(curPlan, cost, null);
                for (String s: result.getInfo()) {
                    logger.append(s);
                }
                int[] aPotencials = result.getResult().get(A_POTENCIAL);
                int[] bPotencials = result.getResult().get(B_POTENCIAL);
            } else {
                logger.append("План вывырожденный");

                //coordinates of zero shipping in array sorted by their cost
                //first of them: (m + n - 1 - basicCellCount) will be marked as E
                List<Point> eCoordinates = new ArrayList<>();

                find:
                for (int price : zeroXCost) {
                    for (Point zeroX : zeroXs) {
                        int i = zeroX.getX();
                        int j = zeroX.getY();
                        if (cost[i][j] == price) {
                            if (eCoordinates.size() == diff) {
                                break find;
                            }
                            eCoordinates.add(new Point(i, j));
                            zeroXs.remove(new Point(i, j));
                        }
                    }
                }
                OperationResult<Map<String, int[]>> result =
                        Solution.getPotencials(curPlan, cost, eCoordinates.toArray(new Point[eCoordinates.size()]));
                for (String s: result.getInfo()) {
                    logger.append(s);
                }
                int[] aPotencials = result.getResult().get(A_POTENCIAL);
                int[] bPotencials = result.getResult().get(B_POTENCIAL);
                Point maxDiffFakeCost = null;
                int maxDiffPrice = 0;
                for (Point zeroX: zeroXs) {
                    int x = zeroX.getX();
                    int y = zeroX.getY();
                    int fakePrice = aPotencials[x] + bPotencials[y];
                    int diffPrice = fakePrice - cost[x][y];
                    if (diffPrice > maxDiffPrice) {
                        maxDiffPrice = diffPrice;
                        maxDiffFakeCost = new Point(x, y);
                    }
                }
                if (maxDiffFakeCost != null) {

                }
            }
        }
        return new OperationResult<>(allPlans, infoList);
    }

    private static OperationResult<boolean[][]> getCycle(Point startZeroShipping, int[][] curPlan) {
        int startX = startZeroShipping.getX();
        int prevX = startX - 1;
        int nextX = startX + 1;

        int startY = startZeroShipping.getY();
        int prevY = startY - 1;
        int nextY = startY + 1;

        int xLength = curPlan.length;
        int yLength = curPlan[0].length;

        boolean[][] choosenCells = new boolean[curPlan.length][curPlan[0].length];
        choosenCells[startX][startY] = true;

        List<String> logs = new ArrayList<>();
        boolean cycleMaked = false;

        if (prevX >= 0) {
            cycleMaked  = makeCycle(startZeroShipping, startZeroShipping, new Point(prevX, startY), curPlan, choosenCells);
        }

        if (cycleMaked)
            return new OperationResult<>(choosenCells, logs);

        if (nextX < xLength) {
            cycleMaked = makeCycle(startZeroShipping, startZeroShipping, new Point(nextX, startY), curPlan, choosenCells);
        }

        if (cycleMaked)
            return new OperationResult<>(choosenCells, logs);

        if (prevY >= 0) {
            cycleMaked = makeCycle(startZeroShipping, startZeroShipping, new Point(startX, prevY), curPlan, choosenCells);
        }

        if (cycleMaked)
            return new OperationResult<>(choosenCells, logs);

        if (nextY < yLength) {
            makeCycle(startZeroShipping, startZeroShipping, new Point(startX, nextY), curPlan, choosenCells);
        }

        return new OperationResult<>(choosenCells, logs);
    }

    public static void main(String[] args) {
    }

    private static boolean makeCycle(Point startZeroX, Point prevX, Point curX,  int[][] curPlan,
                                     boolean[][] choosenCells) {
        if (curX.equals(startZeroX))
            return true;

        int x = curX.getX();
        int x1 = x - 1;
        int x2 = x + 1;
        boolean isPrevUp = (x - 1 == prevX.getX());
        boolean isPrevDown = (x + 1 == prevX.getX());

        int y = curX.getY();
        int y1 = y - 1;
        int y2 = y + 1;
        boolean isPrevLeft = (y - 1 == prevX.getY());
        boolean isPrevRight = (y + 1 == prevX.getY());

        int xLength = curPlan.length;
        int yLength  = curPlan[0].length;
        int prevShippingX = prevX.getX();
        int prevShippingY = prevX.getY();

        boolean cycleMaked = false;

        if (y2 < yLength && y2 != prevShippingY) {
            if (!choosenCells[x][y2]) {
                if (isPrevLeft || isPrevRight) {
                    if (curPlan[x][y] > 0) {
                        choosenCells[x][y] = true;
                        cycleMaked = makeCycle(startZeroX, curX, new Point(x, y2), curPlan, choosenCells);
                    }else {
                        cycleMaked = false;
                    }
                }else {
                    cycleMaked = makeCycle(startZeroX, curX, new Point(x, y2), curPlan, choosenCells);
                }
            }
        }

        if (cycleMaked) {
            return true;
        }
        choosenCells[x][y] = false;

        if (y1 >= 0 && y1 != prevShippingY) {
            if (!choosenCells[x][y1]){
                if (isPrevLeft || isPrevRight) {
                    if (curPlan[x][y] > 0) {
                        choosenCells[x][y] = true;
                        cycleMaked = makeCycle(startZeroX, curX, new Point(x, y1), curPlan, choosenCells);
                    }else {
                        cycleMaked = false;
                    }
                }else {
                    cycleMaked = makeCycle(startZeroX, curX, new Point(x, y1), curPlan, choosenCells);
                }
            }
        }

        if (cycleMaked) {
            return true;
        }
        choosenCells[x][y] = false;

        if (x2 < xLength && x2 != prevShippingX) {
            if (!choosenCells[x2][y]) {
                if (isPrevUp || isPrevDown) {
                    if (curPlan[x][y] > 0) {
                        choosenCells[x][y] = true;
                        cycleMaked = makeCycle(startZeroX, curX, new Point(x2, y), curPlan, choosenCells);
                    }else {
                        cycleMaked = false;
                    }
                }else {
                    cycleMaked = makeCycle(startZeroX, curX, new Point(x2, y), curPlan, choosenCells);
                }
            }
        }

        if (cycleMaked) {
            return true;
        }
        choosenCells[x][y] = false;

        if (x1 >= 0 && x1 != prevShippingX) {
            if (!choosenCells[x1][y]) {
                if (isPrevUp || isPrevDown) {
                    if (curPlan[x][y] > 0) {
                        choosenCells[x][y] = true;
                        cycleMaked = makeCycle(startZeroX, curX, new Point(x1, y), curPlan, choosenCells);
                    }else {
                        cycleMaked = false;
                    }
                }else {
                    cycleMaked = makeCycle(startZeroX, curX, new Point(x1, y), curPlan, choosenCells);
                }
            }
        }

        if (cycleMaked) {
            return true;
        }
        choosenCells[x][y] = false;

        return false;
    }

    public static OperationResult<int[][]> MinMethod(int[] consumersNeeds, int[] providersOffers,
                                                     int[][] cost) {

        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("Метод мин. элемента\n\n");
        infoBuilder.append("Начальный опорный план:\n");
        int[] needs = Arrays.copyOf(consumersNeeds, consumersNeeds.length);
        int[] offers = Arrays.copyOf(providersOffers, providersOffers.length);
        int[][] xPlan = new int[providersOffers.length][consumersNeeds.length];

        //sort all prices ascending
        Set<Integer> sortedCost = new TreeSet<>();
        List<Point> zeroCostIdxs = new ArrayList<>();
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[i].length; j++) {
                if (cost[i][j] != 0) {
                    sortedCost.add(cost[i][j]);
                }else {
                    zeroCostIdxs.add(new Point(i, j));
                }
            }
        }

        //execute algorithm from min price to max price
        //also we check all equal prices
        for (Integer price: sortedCost) {
            List<Point> foundIdxs = new ArrayList<>();
            for (int i = 0; i < cost.length; i++) {
                for (int j = 0; j < cost[i].length; j++) {
                    if (price == cost[i][j]) {
                        foundIdxs.add(new Point(i, j));
                    }
                }
            }
            for (Point p: foundIdxs) {
                int iIdx = p.getX();
                int jIdx = p.getY();
                if (offers[iIdx] == 0 || needs[jIdx] == 0) continue;
                xPlan[iIdx][jIdx] = Math.min(offers[iIdx], needs[jIdx]);
                offers[iIdx] -= xPlan[iIdx][jIdx];
                needs[jIdx] -= xPlan[iIdx][jIdx];

                loggingCurValues(iIdx, jIdx, xPlan, cost, needs, offers, infoBuilder);
            }
        }

        //check zero prices (added for isolation)
        for (Point zeroCost: zeroCostIdxs) {
            int i = zeroCost.getX();
            int j = zeroCost.getY();
            if (offers[i] != 0 && needs[j] != 0) {
                if (cost[i][j] == 0) {
                    xPlan[i][j] = Math.min(offers[i], needs[j]);
                    offers[i] -= xPlan[i][j];
                    needs[j] -= xPlan[i][j];

                    loggingCurValues(i, j, xPlan, cost, needs, offers, infoBuilder);
                }
            }
        }

        int L = calcL(cost, xPlan);
        infoBuilder.append("\n L = ");
        infoBuilder.append(L);

        ArrayList<String> infoList = new ArrayList<>();
        infoList.add(infoBuilder.toString());
        return new OperationResult<>(xPlan, infoList);
    }

    public static OperationResult<int[][]> NorthWestMethod(int[] consumerNeeds, int[] providersOffers, int[][] cost) {
        StringBuilder info = new StringBuilder();
        info.append("Метод северо-западного угла\n\n");
        info.append("Начальный опорный план:\n");
        int[] needs = Arrays.copyOf(consumerNeeds, consumerNeeds.length);
        int[] offers = Arrays.copyOf(providersOffers, providersOffers.length);
        int[][] xPlan = new int[offers.length][needs.length];

        int i = 0, j = 0;
        while (j < needs.length && i < offers.length) {
            xPlan[i][j] = Math.min(needs[j], offers[i]);
            needs[j] -= xPlan[i][j];
            offers[i] -= xPlan[i][j];

            loggingCurValues(i, j, xPlan, null, needs, offers, info);

            if (needs[j] == 0) {
                j++;
            }
            if (offers[i] == 0) {
                i++;
            }
        }
        int L = calcL(cost, xPlan);
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
        int sumB = sumResultB.getResult();
        logs = sumResultB.getInfo();

        for (String s: logs) {
            info.append(s);
        }
        info.append("\n");

        if (sumA == sumB) {
            info.append("Задача замкнута");
        } else {
            info.append("\nПриведем задачу к замкнутому виду:\n");
            if (sumA > sumB) {
                needs.add(sumA - sumB);
                for (int i = 0; i < cost.size(); i++) {
                    cost.get(i).add(0);
                }
                int addedNeedIdx = needs.size() - 1;

                info.append("b" + addedNeedIdx);
                info.append(" = sumA - sumB = ");
                info.append(needs.get(addedNeedIdx));

                info.append("\nci,");
                info.append(addedNeedIdx + " = 0,");
                info.append("i = 0..." + (offers.size() - 1));
            } else {
                offers.add(sumB - sumA);
                cost.add(new ArrayList<Integer>());
                List<Integer> addedOffer = cost.get(cost.size() - 1);
                for (int i = 0; i < needs.size(); i++) {
                    addedOffer.add(0);
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


        List<String> infoList = new ArrayList<>();
        infoList.add(info.toString());
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

        ArrayList<String> infoList = new ArrayList<>();
        infoList.add(infoBuilder.toString());
        return new OperationResult<>(sum, infoList);
    }

    public static double calcL(double[][] cost, double[][] xPlan) {
        int L = 0;
        for (int i = 0; i < xPlan.length; i++) {
            for (int j = 0; j < xPlan[i].length; j++) {
                if (xPlan[i][j] != 0) {
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

