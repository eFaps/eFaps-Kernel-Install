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

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
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
        throws EFapsException
    {
        final XYDataset dataset = getDataSet(_parameter);
        final CustomXYToolTipGenerator ttg = new CustomXYToolTipGenerator();
        fillData(_parameter, dataset, ttg);
        final JFreeChart chart = createChart(_parameter, dataset, ttg);
         configureChart(_parameter, chart, ttg);
        return chart;
    }

    protected XYDataset getDataSet(final Parameter _parameter)
        throws EFapsException
    {
        return new XYSeriesCollection();
    }

    /**
     * @param _dataset
     * @return
     */
    protected JFreeChart createChart(final Parameter _parameter,
                                     final XYDataset _dataset,
                                     final XYToolTipGenerator _toolTipGen)
        throws EFapsException
    {

        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                        getTitel(_parameter), // chart title
                        getXAxisLabel(_parameter), // x axis label
                        getYAxisLabel(_parameter), // y axis label
                        _dataset, // data
                        PlotOrientation.VERTICAL,
                        true, // include legend
                        true, // tooltips
                        false // urls
                        );
        return chart;
    }

    protected String getXAxisLabel(final Parameter _parameter)
        throws EFapsException
    {
        final String ret = getProperty(_parameter, "XAxisLabel");
        return DBProperties.getProperty(ret);
    }

    protected String getYAxisLabel(final Parameter _parameter)
        throws EFapsException
    {
        final String ret = getProperty(_parameter, "YAxisLabel");
        return DBProperties.getProperty(ret);
    }

    protected void configureChart(final Parameter _parameter,
                                  final JFreeChart _chart,
                                  final XYToolTipGenerator _toolTipGen)
    {
        _chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot = _chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final StandardXYItemRenderer renderer = new StandardXYItemRenderer(
                        StandardXYItemRenderer.LINES + StandardXYItemRenderer.SHAPES, _toolTipGen);

        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }

    /**
     * @param _ttg
     * @param _dataset
     * @return
     */
    protected void fillData(final Parameter _parameter,
                            final XYDataset _dataset,
                            final CustomXYToolTipGenerator _ttg)
        throws EFapsException
    {
        final XYSeriesCollection datset = (XYSeriesCollection) _dataset;
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

        datset.addSeries(series1);
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
        datset.addSeries(series2);
        final XYSeries series3 = new XYSeries("Third");
        series3.add(3.0, 4.0);
        series3.add(4.0, 3.0);
        series3.add(5.0, 2.0);
        series3.add(6.0, 3.0);
        series3.add(7.0, 6.0);
        series3.add(8.0, 3.0);
        series3.add(9.0, 4.0);
        series3.add(10.0, 3.0);
        datset.addSeries(series3);
    }
}
