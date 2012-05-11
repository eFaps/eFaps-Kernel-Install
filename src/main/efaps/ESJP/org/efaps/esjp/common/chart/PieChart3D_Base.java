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
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.PieDataset;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("49f47e6b-6e3a-46f8-81fb-27494ed5f2d0")
@EFapsRevision("$Rev$")
public abstract class PieChart3D_Base
    extends PieChart_Base
{

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @param _dataset dataset
     * @param _toolTipGen tooltip generator
     * @return JFreeChart
     * @throws EFapsException on error
     */
    @Override
    protected JFreeChart createChart(final Parameter _parameter,
                                     final PieDataset dataset,
                                     final StandardPieToolTipGenerator _ttg)
        throws EFapsException
    {

        final JFreeChart chart = ChartFactory.createPieChart3D(
                        getTitel(_parameter), // chart title
                        dataset, // data
                        isIncludeLegend(_parameter), // include legend
                        isIncludeTooltip(_parameter), // inlcude tooltips
                        false
                        );
        return chart;
    }

    /**
     * @param _parameter
     * @param _chart
     * @param _ttg
     */
    @Override
    protected void configureChart(final Parameter _parameter,
                                  final JFreeChart _chart,
                                  final StandardPieToolTipGenerator _ttg)
    {
        _chart.setBackgroundPaint(Color.white);

        final PiePlot3D plot = (PiePlot3D) _chart.getPlot();
        plot.setForegroundAlpha(0.6f);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
    }

}
