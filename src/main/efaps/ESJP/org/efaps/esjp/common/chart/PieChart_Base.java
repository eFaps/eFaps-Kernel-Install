/*
 * Copyright 2003 - 2012 The eFaps Team
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
import java.awt.Font;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("714d4a56-0d24-4c83-9328-1f2286878f4f")
@EFapsRevision("$Rev$")
public abstract class PieChart_Base
    extends AbstractChart
{

    @Override
    protected JFreeChart createChart(final Parameter _parameter)
        throws EFapsException
    {
        final PieDataset dataset = getDataSet(_parameter);
        final StandardPieToolTipGenerator ttg = new StandardPieToolTipGenerator();
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
                                  final StandardPieToolTipGenerator _ttg)
    {
        _chart.setBackgroundPaint(Color.white);

        final PiePlot plot = (PiePlot) _chart.getPlot();
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _dataset dataset
     * @param _toolTipGen tooltip generator
     * @return JFreeChart
     * @throws EFapsException on error
     */
    protected JFreeChart createChart(final Parameter _parameter,
                                     final PieDataset dataset,
                                     final StandardPieToolTipGenerator _ttg)
        throws EFapsException
    {

        final JFreeChart chart = ChartFactory.createPieChart(
                        getTitel(_parameter), // chart title
                        dataset, // data
                        isIncludeLegend(_parameter), // include legend
                        isIncludeTooltip(_parameter), // inlcude tooltips
                        false
                        );
        return chart;
    }

    /**
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    protected PieDataset getDataSet(final Parameter _parameter)
    {
        return new DefaultPieDataset();
    }

    /**
     * @param _parameter
     * @param _dataset
     * @param _ttg
     */
    protected void fillData(final Parameter _parameter,
                            final PieDataset _dataset,
                            final StandardPieToolTipGenerator _ttg)
    {
        final DefaultPieDataset dataset = (DefaultPieDataset) _dataset;
        dataset.setValue("One", new Double(43.2));
        dataset.setValue("Two", new Double(10.0));
        dataset.setValue("Three", new Double(27.5));
        dataset.setValue("Four", new Double(17.5));
        dataset.setValue("Five", new Double(11.0));
        dataset.setValue("Six", new Double(19.4));
    }
}
