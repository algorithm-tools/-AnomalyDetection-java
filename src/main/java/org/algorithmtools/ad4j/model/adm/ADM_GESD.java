package org.algorithmtools.ad4j.model.adm;

import org.algorithmtools.ad4j.config.ADMConfigs;
import org.algorithmtools.ad4j.enumtype.AnomalyDictType;
import org.algorithmtools.ad4j.pojo.*;
import org.algorithmtools.ad4j.utils.CollectionUtil;
import org.algorithmtools.ad4j.utils.IndicatorCalculateUtil;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Anomaly detection model: Outlier detection based on GESD
 * @author mym
 */
public class ADM_GESD extends AbstractADM {

    private double alpha;

    public ADM_GESD() {
        super(AnomalyDictType.MODEL_ADM_GESD, AnomalyDictType.TYPE_OUTLIERS_VALUE);
    }

    @Override
    public void init(AnomalyDetectionContext context) {
        this.alpha = Double.parseDouble((String) context.getConfig(ADMConfigs.ADM_GESD_ALPHA));
    }

    @Override
    public IndicatorEvaluateInfo evaluate(List<IndicatorSeries> indicatorSeries, AnomalyDetectionLog log) {
        List<Integer> gesdIndexList = gesd(indicatorSeries, indicatorSeries.size() / 2, alpha);

        // build evaluate info
        IndicatorEvaluateInfo result = buildDefaultEvaluateInfo();
        if (CollectionUtil.isNotEmpty(gesdIndexList)) {
            result.setHasAnomaly(true);
            result.setNormalRangeMin(0d);
            result.setNormalRangeMax(0d);

            DescriptiveStatistics statistics = IndicatorCalculateUtil.initStatistic(null, indicatorSeries, null);
            final double mean = statistics.getMean();
            result.setAnomalySeriesList(gesdIndexList.stream()
                    .map(v -> new AnomalyIndicatorSeries(indicatorSeries.get(v).getValue() > mean ? AnomalyDictType.INFLUENCE_POSITIVE : AnomalyDictType.INFLUENCE_NEGATIVE, indicatorSeries.get(v)))
                    .collect(Collectors.toList())
            );
        }

        return result;
    }

    @Override
    public boolean checkCompatibility(List<IndicatorSeries> indicatorSeries, AnomalyDetectionLog log) {
        // TODO 检查是否是正态分布
        return true;
    }

    protected List<Integer> gesd(List<IndicatorSeries> dataList, int maxOutliers, double alpha) {
        List<Integer> outlierIndexes = new ArrayList<>();
        List<IndicatorSeries> originalData = new ArrayList<>(dataList);

        DescriptiveStatistics stats = null;
        int n = dataList.size();
        int r = maxOutliers;
        for (int i = 1; i <= r; i++) { // i = 1,2,3...r
            // init statistic
            stats = IndicatorCalculateUtil.initStatistic(stats, originalData, outlierIndexes);
            // calculate mean and std
            double mean = stats.getMean();
            double stdDev = stats.getStandardDeviation();

            // calculate Max(Ri)
            double maxRi = -1;
            int maxRiIndex = -1;
            for (int j = 0; j < originalData.size(); j++) {
                if (outlierIndexes.contains(j)) {
                    continue;
                }

                double deviation = Math.abs(originalData.get(j).getValue() - mean);
                if (maxRi < deviation) {
                    maxRi = deviation;
                    maxRiIndex = j;
                }
            }

            // Lambda i
            double lambdaI = calculateLambdaI(n, i, alpha);

            // judge anomaly: where(Ri > LambdaI)
            if ((maxRi / stdDev) > lambdaI) {
                outlierIndexes.add(maxRiIndex);
            } else {
                break;
            }
        }

        return outlierIndexes;
    }

    // calculate lambda (critical-value)
    public double calculateLambdaI(int n, int i, double alpha) {
        TDistribution tDistribution = new TDistribution(n - i - 1);
        double p = 1 - alpha / (2 * (n - i + 1)); // both sid t-distribution
        double tValue = tDistribution.inverseCumulativeProbability(p); // Probability to T
        return (n - i) * tValue / Math.sqrt((n - i - 1 + Math.pow(tValue, 2)) * (n - i + 1));
    }

}
