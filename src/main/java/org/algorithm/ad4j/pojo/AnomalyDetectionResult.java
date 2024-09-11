package org.algorithm.ad4j.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anomaly detection result pojo
 */
public class AnomalyDetectionResult implements Serializable {

    /**
     * indicator info
     */
    private IndicatorInfo indicatorInfo;

    /** indicator evaluate result. key=AnomalyType, value=List<IndicatorEvaluateInfo> */
    public Map<Integer, ArrayList<IndicatorEvaluateInfo>> indicatorEvaluateMap = new ConcurrentHashMap<>();

    public AnomalyDetectionResult(IndicatorInfo indicatorInfo) {
        this.indicatorInfo = indicatorInfo;
    }

    public AnomalyDetectionResult() {
    }

    public void addIndicatorEvaluateInfo(Integer anomalyType, IndicatorEvaluateInfo evaluateInfo){
        if (evaluateInfo == null) {
            return;
        }
        this.indicatorEvaluateMap.compute(anomalyType, (k,v) -> v == null ? new ArrayList<>() : v).add(evaluateInfo);
    }

    public ArrayList<IndicatorEvaluateInfo> getIndicatorEvaluateInfo(Integer anomalyType){
        return this.indicatorEvaluateMap.get(anomalyType);
    }

    public Map<Integer, ArrayList<IndicatorEvaluateInfo>> getIndicatorEvaluateMap() {
        return indicatorEvaluateMap;
    }

    public void setIndicatorEvaluateMap(Map<Integer, ArrayList<IndicatorEvaluateInfo>> indicatorEvaluateMap) {
        this.indicatorEvaluateMap = indicatorEvaluateMap;
    }

    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    public void setIndicatorInfo(IndicatorInfo indicatorInfo) {
        this.indicatorInfo = indicatorInfo;
    }

    public boolean hasAnomaly(){
        return indicatorEvaluateMap != null && indicatorEvaluateMap.size() == 0;
    }
}
