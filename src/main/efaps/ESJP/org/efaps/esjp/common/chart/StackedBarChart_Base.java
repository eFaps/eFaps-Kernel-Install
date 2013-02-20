/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev: 7235 $
 * Last Changed:    $Date: 2011-11-08 21:21:25 -0500 (mar, 08 nov 2011) $
 * Last Changed By: $Author: jan@moxter.net $
 */


package org.efaps.esjp.common.chart;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: BarChart3D_Base.java 7235 2011-11-09 02:21:25Z jan@moxter.net $
 */
@EFapsUUID("91a15870-dbbf-4adf-90b7-803b5d6c2b0f")
@EFapsRevision("$Rev: 7235 $")
public abstract class StackedBarChart_Base
    extends BarChart
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
                                     final CategoryDataset dataset,
                                     final StandardCategoryToolTipGenerator _ttg)
        throws EFapsException
    {
        final JFreeChart chart = ChartFactory.createStackedBarChart(
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


    @Override
    protected void fillData(final Parameter _parameter,
                            final CategoryDataset _dataset,
                            final StandardCategoryToolTipGenerator _ttg)
    {
        final DefaultCategoryDataset result = (DefaultCategoryDataset) _dataset;

        result.addValue(20.3, "Product 1 (US)", "Jan 04");
        result.addValue(27.2, "Product 1 (US)", "Feb 04");
        result.addValue(19.7, "Product 1 (US)", "Mar 04");
        result.addValue(-19.4, "Product 1 (Europe)", "Jan 04");
        result.addValue(10.9, "Product 1 (Europe)", "Feb 04");
        result.addValue(18.4, "Product 1 (Europe)", "Mar 04");
        result.addValue(16.5, "Product 1 (Asia)", "Jan 04");
        result.addValue(-15.9, "Product 1 (Asia)", "Feb 04");
        result.addValue(16.1, "Product 1 (Asia)", "Mar 04");
        result.addValue(13.2, "Product 1 (Middle East)", "Jan 04");
        result.addValue(14.4, "Product 1 (Middle East)", "Feb 04");
        result.addValue(-13.7, "Product 1 (Middle East)", "Mar 04");

        /*result.addValue(23.3, "Product 2 (US)", "Jan 04");
        result.addValue(16.2, "Product 2 (US)", "Feb 04");
        result.addValue(28.7, "Product 2 (US)", "Mar 04");
        result.addValue(12.7, "Product 2 (Europe)", "Jan 04");
        result.addValue(17.9, "Product 2 (Europe)", "Feb 04");
        result.addValue(12.6, "Product 2 (Europe)", "Mar 04");
        result.addValue(15.4, "Product 2 (Asia)", "Jan 04");
        result.addValue(21.0, "Product 2 (Asia)", "Feb 04");
        result.addValue(11.1, "Product 2 (Asia)", "Mar 04");
        result.addValue(23.8, "Product 2 (Middle East)", "Jan 04");
        result.addValue(23.4, "Product 2 (Middle East)", "Feb 04");
        result.addValue(19.3, "Product 2 (Middle East)", "Mar 04");

        result.addValue(11.9, "Product 3 (US)", "Jan 04");
        result.addValue(31.0, "Product 3 (US)", "Feb 04");
        result.addValue(22.7, "Product 3 (US)", "Mar 04");
        result.addValue(15.3, "Product 3 (Europe)", "Jan 04");
        result.addValue(14.4, "Product 3 (Europe)", "Feb 04");
        result.addValue(25.3, "Product 3 (Europe)", "Mar 04");
        result.addValue(23.9, "Product 3 (Asia)", "Jan 04");
        result.addValue(19.0, "Product 3 (Asia)", "Feb 04");
        result.addValue(10.1, "Product 3 (Asia)", "Mar 04");
        result.addValue(13.2, "Product 3 (Middle East)", "Jan 04");
        result.addValue(15.5, "Product 3 (Middle East)", "Feb 04");
        result.addValue(10.1, "Product 3 (Middle East)", "Mar 04");*/
    }
}
