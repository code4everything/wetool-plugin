package org.code4everything.wetool.plugin.support.util;

import cn.hutool.core.util.ReflectUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2020/6/30
 */
@Slf4j
@UtilityClass
public class FxCharts {

    /**
     * 获取图表对象
     *
     * @param dataList 数据列表
     * @param targetType 图表类型
     * @param dataMapper 类型映射
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>, S> T getChart(List<Pair<String, List<S>>> dataList
            , Class<T> targetType, Function<S, XYChart.Data<String, Y>> dataMapper) {
        List<XYChart.Series<String, Y>> list = dataList.stream().map(pair -> getSeries(pair.getKey(), pair.getValue()
                , dataMapper)).collect(Collectors.toList());
        return getChart(FXCollections.observableList(list), targetType);
    }

    /**
     * 获取图表对象
     *
     * @param dataMap 数据列表
     * @param targetType 图表类型
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>> T getChart(Map<String, Map<String, Y>> dataMap,
                                                                              Class<T> targetType) {
        List<XYChart.Series<String, Y>> list = dataMap.entrySet().stream().map(entry -> getSeries(entry.getKey(),
                entry.getValue())).collect(Collectors.toList());
        return getChart(FXCollections.observableList(list), targetType);
    }

    /**
     * 获取图表对象
     *
     * @param name series label
     * @param dataList 数据列表
     * @param targetType 图表类型
     * @param dataMapper 类型映射
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>, S> T getSingleSeriesChart(String name,
                                                                                             List<S> dataList,
                                                                                             Class<T> targetType,
                                                                                             Function<S,
                                                                                                     XYChart.Data<String, Y>> dataMapper) {
        return getChart(getSeries(name, dataList, dataMapper), targetType);
    }

    /**
     * 获取图表对象
     *
     * @param name series label
     * @param dataMap 数据列表
     * @param targetType 图表类型
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>> T getSingleSeriesChart(String name,
                                                                                          Map<String, Y> dataMap,
                                                                                          Class<T> targetType) {
        return getChart(getSeries(name, dataMap), targetType);
    }

    /**
     * 获取图表对象
     *
     * @param series 数据列表
     * @param targetType 图表类型
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>> T getChart(XYChart.Series<String, Y> series,
                                                                              Class<T> targetType) {
        return getChart(FXCollections.singletonObservableList(series), targetType);
    }

    /**
     * 获取图表对象
     *
     * @param series 数据列表
     * @param targetType 图表类型
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>> T getChart(ObservableList<XYChart.Series<String,
            Y>> series, Class<T> targetType) {
        T chart = getChart(targetType);
        chart.setData(series);
        return chart;
    }

    /**
     * 获取图表对象
     *
     * @param targetType 图表类型
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>> T getChart(Class<T> targetType) {
        return getChart(targetType, Side.BOTTOM, Side.LEFT);
    }

    /**
     * 获取图表对象
     *
     * @param targetType 图表类型
     * @param categorySide X轴位置
     * @param numberSide Y轴位置
     *
     * @return 图表对象
     */
    public static <Y extends Number, T extends XYChart<String, Y>> T getChart(Class<T> targetType, Side categorySide,
                                                                              Side numberSide) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setSide(categorySide);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setSide(numberSide);

        return ReflectUtil.newInstance(targetType, xAxis, yAxis);
    }

    /**
     * 获取series对象
     *
     * @param name series label
     * @param dataList 数据列表
     * @param dataMapper 类型映射
     *
     * @return series对象
     */
    public static <S, Y> XYChart.Series<String, Y> getSeries(String name, List<S> dataList, Function<S,
            XYChart.Data<String, Y>> dataMapper) {
        XYChart.Series<String, Y> series = new XYChart.Series<>();
        series.setName(name);
        dataList.forEach(e -> series.getData().add(dataMapper.apply(e)));
        return series;
    }

    /**
     * 获取series对象
     *
     * @param name series label
     * @param dataMap 数据列表
     *
     * @return series对象
     */
    public static <Y> XYChart.Series<String, Y> getSeries(String name, Map<String, Y> dataMap) {
        XYChart.Series<String, Y> series = new XYChart.Series<>();
        series.setName(name);
        dataMap.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        return series;
    }
}
