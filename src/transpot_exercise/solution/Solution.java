package transpot_exercise.solution;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.*;

/**
 * This class contains methods which do some calculations and
 * return OperationResult object with Object result of operation and String which explain all actions
 */
public class Solution {
    private static final String A_POTENCIAL = "a";
    private static final String B_POTENCIAL = "b";

    private Solution() {}

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

    //eXCoordinates - coordinates of zero shipping in array which must be replaced with E
    private static OperationResult<Map<String, int[]>> getPotencials(int[][] curPlan, int[][] cost,
                                                                     Point[] eXCoordinates) {
        Map<String, int[]> res = new HashMap<>();
        List<Integer> aPotencial = new ArrayList<>();
        List<Integer> bPotencial = new ArrayList<>();
        ArrayList<String> infoList = new ArrayList<>();

        List<Point> eCoordinates = null;
        if (eXCoordinates != null && eXCoordinates.length != 0) {
            eCoordinates = new ArrayList<>();
            Collections.addAll(eCoordinates, eXCoordinates);
        };

        boolean isPlanDegenerate = eCoordinates != null;



        res.put(A_POTENCIAL, ConvertUtils.singleListToPrimitiveArray(aPotencial));
        res.put(B_POTENCIAL, ConvertUtils.singleListToPrimitiveArray(bPotencial));
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

            for (int i = 0; i < curPlan.length; i++) {
                for (int j = 0; j < curPlan[i].length; j++) {
                    if (curPlan[i][j] != 0) {
                        basicCellCount++;
                    } else {
                        zeroXCost.add(cost[i][j]);
                    }
                }
            }

            logValue(logger, checkCount, "m + n - 1");
            logValue(logger, basicCellCount, "basicCellCount");

            int diff = Math.abs(basicCellCount - checkCount);

            if (diff == 0) {
                logger.append("План невырожденный");
            } else {
                logger.append("План вывырожденный");

                //coordinates of zero shipping in array sorted by their cost
                //first of them: (m + n - 1 - basicCellCount) will be marked as E
                List<Point> zeroXCoordinates = new ArrayList<>();

                find:
                for (int price: zeroXCost) {
                    for (int i = 0; i < curPlan.length; i++)
                        for (int j = 0; j < curPlan[i].length ; j++) {
                            if (curPlan[i][j] == 0 && cost[i][j] == price) {
                                if (zeroXCoordinates.size() == diff) {
                                    break find;
                                }
                                zeroXCoordinates.add(new Point(i, j));
                            }
                        }
                }
            }
        }

        return new OperationResult<>(allPlans, infoList);
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
        for (int i = 0; i < cost.length; i++) {
            for (int price : cost[i]) {
                if (price != 0) {
                    sortedCost.add(price);
                }
            }
        }

        //execute algorithm from min price to max price
        //also we check all equal prices
        for (Integer price: sortedCost) {
            List<Integer> foundIdxI = new ArrayList<>();
            List<Integer> foundIdxJ = new ArrayList<>();
            for (int i = 0; i < cost.length; i++) {
                for (int j = 0; j < cost[i].length; j++) {
                    if (price == cost[i][j]) {
                        foundIdxI.add(i);
                        foundIdxJ.add(j);
                    }
                }
            }
            for (int k = 0; k < foundIdxI.size(); k++) {
                int iIdx = foundIdxI.get(k);
                int jIdx = foundIdxJ.get(k);
                if (offers[iIdx] == 0 || needs[jIdx] == 0) continue;
                xPlan[iIdx][jIdx] = Math.min(offers[iIdx], needs[jIdx]);
                offers[iIdx] -= xPlan[iIdx][jIdx];
                needs[jIdx] -= xPlan[iIdx][jIdx];

                loggingCurValues(iIdx, jIdx, xPlan, cost, needs, offers, infoBuilder);
            }
        }

        //check zero prices (added for isolation)
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[i].length; j++) {
                if (offers[i] != 0 && needs[j] != 0) {
                    if (cost[i][j] == 0) {
                        xPlan[i][j] = Math.min(offers[i], needs[j]);
                        offers[i] -= xPlan[i][j];
                        needs[j] -= xPlan[i][j];

                        loggingCurValues(i, j, xPlan, cost, needs, offers, infoBuilder);
                    }
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

    public static OperationResult<Void> makeIsolation(List<List<Integer>> cost,
                                                      List<Integer> needs, List<Integer> offers) {
        StringBuilder info = new StringBuilder();

        info.append("sumA = ");
        OperationResult<Integer> sumResultA = Solution.sum(offers);
        int sumA = sumResultA.getResult();
        for (String s: sumResultA.getInfo()) {
            info.append(s);
        }
        info.append("\n");

        info.append("sumB = ");
        OperationResult<Integer> sumResultB = Solution.sum(needs);
        int sumB = sumResultB.getResult();
        for (String s: sumResultB.getInfo()) {
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

    public static OperationResult<Integer> sum(List<Integer> list) {
        StringBuilder infoBuilder = new StringBuilder();
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            int num = list.get(i);
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

    private static int calcL(int[][] cost, int[][] xPlan) {
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

    private static void loggingCurValues(int i, int j, int[][]xPlan, int[][]cost, int[] needs, int[] offers,
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

    private static void logValue(StringBuilder logger, int value, String title) {
        logger.append(title);
        logger.append(" = ");
        logger.append(value);
        logger.append("\n");
    }
}
