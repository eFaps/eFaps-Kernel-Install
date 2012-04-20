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

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("3a5edb8e-2aba-41e6-a1c2-951963977e35")
@EFapsRevision("$Rev$")
public abstract class BarChart3D_Base
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
        final JFreeChart chart = ChartFactory.createBarChart3D(
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
}