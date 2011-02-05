/*
 * Copyright 2003 - 2011 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("2061bb7c-65cf-4db4-b6ba-cb58ad6f8586")
@EFapsRevision("$Rev$")
public abstract class LineChart_Base
    extends AbstractChart
{

    @Override
    protected JFreeChart createChart(final Parameter _parameter)
    {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        final CustomXYToolTipGenerator ttg = new CustomXYToolTipGenerator();
        fillData(_parameter, dataset, ttg);
        return createChart(_parameter, dataset, ttg);
    }

    /**
     * @param _dataset
     * @return
     */
    protected JFreeChart createChart(final Parameter _parameter,
                                     final XYDataset _dataset,
                                     final XYToolTipGenerator _toolTipGen)
    {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                        "Line Chart Demo 6", // chart title
                        "X", // x axis label
                        "Y", // y axis label
                        _dataset, // data
                        PlotOrientation.VERTICAL,
                        true, // include legend
                        true, // tooltips
                        false // urls
                        );

        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final StandardXYItemRenderer renderer = new StandardXYItemRenderer(
                        StandardXYItemRenderer.LINES + StandardXYItemRenderer.SHAPES, _toolTipGen);

        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return chart;
    }

    /**
     * @param _ttg
     * @param _dataset
     * @return
     */
    protected void fillData(final Parameter _parameter,
                            final XYSeriesCollection _dataset,
                            final CustomXYToolTipGenerator _ttg)
    {
        final XYSeries series1 = new XYSeries("First");
        final List<String> toolTips = new ArrayList<String>();
        series1.add(1.0, 1.0);
        toolTips.add("1D - 5.22");
        series1.add(2.0, 4.0);
        toolTips.add("1D - 5.22");
        series1.add(3.0, 3.0);
        toolTips.add("1D - 5.22");
        series1.add(4.0, 5.0);
        toolTips.add("1D - 5.22");
        series1.add(5.0, 5.0);
        toolTips.add("1D - 5.22");
        series1.add(6.0, 7.0);
        toolTips.add("1D - 5.22");
        series1.add(7.0, 7.0);
        toolTips.add("1D - 5.22");
        series1.add(8.0, 8.0);
        toolTips.add("1D - 5.22");

        _dataset.addSeries(series1);
        _ttg.addToolTipSeries(toolTips);

        final XYSeries series2 = new XYSeries("Second");
        series2.add(1.0, 5.0);
        series2.add(2.0, 7.0);
        series2.add(3.0, 6.0);
        series2.add(4.0, 8.0);
        series2.add(5.0, 4.0);
        series2.add(6.0, 4.0);
        series2.add(7.0, 2.0);
        series2.add(8.0, 1.0);
        _dataset.addSeries(series2);
        final XYSeries series3 = new XYSeries("Third");
        series3.add(3.0, 4.0);
        series3.add(4.0, 3.0);
        series3.add(5.0, 2.0);
        series3.add(6.0, 3.0);
        series3.add(7.0, 6.0);
        series3.add(8.0, 3.0);
        series3.add(9.0, 4.0);
        series3.add(10.0, 3.0);
        _dataset.addSeries(series3);

    }
}
