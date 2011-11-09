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

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("e2797c99-3fe2-4774-b3b8-30f8daced210")
@EFapsRevision("$Rev$")
public abstract class BarChart_Base
    extends AbstractChart
{

    @Override
    protected JFreeChart createChart(final Parameter _parameter)
        throws EFapsException
    {
        final CategoryDataset dataset = getDataSet(_parameter);
        final StandardCategoryToolTipGenerator ttg = new StandardCategoryToolTipGenerator();
        fillData(_parameter, dataset, ttg);
        final JFreeChart chart = createChart(_parameter, dataset, ttg);
        configureChart(_parameter, chart, ttg);
        return chart;
    }

    /**
     * @param _parameter
     * @param _chart
     * @param _ttg
     */
    protected void configureChart(final Parameter _parameter,
                                  final JFreeChart _chart,
                                  final StandardCategoryToolTipGenerator _ttg)
    {
        _chart.setBackgroundPaint(Color.white);
        final CategoryPlot plot = _chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
    }

    /**
     * @param _parameter
     * @param _dataset
     * @param _ttg
     */
    protected void fillData(final Parameter _parameter,
                            final CategoryDataset _dataset,
                            final StandardCategoryToolTipGenerator _ttg)
    {
        // row keys...
        final String series1 = "First";
        final String series2 = "Second";
        final String series3 = "Third";

        // column keys...
        final String category1 = "Category 1";
        final String category2 = "Category 2";
        final String category3 = "Category 3";
        final String category4 = "Category 4";
        final String category5 = "Category 5";

        // create the dataset...
        final DefaultCategoryDataset dataset = (DefaultCategoryDataset) _dataset;

        dataset.addValue(1.0, series1, category1);
        dataset.addValue(4.0, series1, category2);
        dataset.addValue(3.0, series1, category3);
        dataset.addValue(5.0, series1, category4);
        dataset.addValue(5.0, series1, category5);

        dataset.addValue(5.0, series2, category1);
        dataset.addValue(7.0, series2, category2);
        dataset.addValue(6.0, series2, category3);
        dataset.addValue(8.0, series2, category4);
        dataset.addValue(4.0, series2, category5);

        dataset.addValue(4.0, series3, category1);
        dataset.addValue(3.0, series3, category2);
        dataset.addValue(2.0, series3, category3);
        dataset.addValue(3.0, series3, category4);
        dataset.addValue(6.0, series3, category5);

    }

    /**
     * @param _parameter
     * @return
     */
    protected CategoryDataset getDataSet(final Parameter _parameter)
    {
        return new DefaultCategoryDataset();
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _dataset dataset
     * @param _toolTipGen tooltip generator
     * @return JFreeChart
     * @throws EFapsException on error
     */
    protected JFreeChart createChart(final Parameter _parameter,
                                     final CategoryDataset dataset,
                                     final StandardCategoryToolTipGenerator _ttg)
        throws EFapsException
    {

        final JFreeChart chart = ChartFactory.createBarChart(
                        getTitel(_parameter), // chart title
                        getDomainAxisLabel(_parameter), // domain axis label
                        getRangeAxisLabel(_parameter), // range axis label
                        dataset, // data
                        getOrientation(_parameter), // orientation
                        isIncludeLegend(_parameter), // include legend
                        isIncludeTooltip(_parameter), // tooltips?
                        false // URLs?
                        );
        return chart;
    }

    /**
     * Get the Label for XAxis of the chart. . Can be set by adding a property "XAxisLabel" to the event definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return Label for XAxis
     * @throws EFapsException on error
     */
    protected String getDomainAxisLabel(final Parameter _parameter)
        throws EFapsException
    {
        final String ret = getProperty(_parameter, "DomainAxisLabel");
        return DBProperties.getProperty(ret);
    }

    /**
     * Get the Label for YAxis of the chart. . Can be set by adding a property "YAxisLabel" to the event definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return Label for YAxis
     * @throws EFapsException on error
     */
    protected String getRangeAxisLabel(final Parameter _parameter)
        throws EFapsException
    {
        final String ret = getProperty(_parameter, "RangeAxisLabel");
        return DBProperties.getProperty(ret);
    }
}
