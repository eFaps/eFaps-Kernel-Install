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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.common.chart;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.field.FieldChart;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.util.EFapsException;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotOrientation;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("87db5f83-fe92-4b41-a6fd-a23655e37bd8")
@EFapsRevision("$Rev$")
public abstract class AbstractChart_Base
    implements EventExecution
{

    /**
     * Create the Chart.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return Return containing the chart and a snipplet
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final JFreeChart chart = createChart(_parameter);
        final File file = new FileUtil().getFile(getFileName(_parameter), getEnding(_parameter));
        final FieldChart field = (FieldChart) _parameter.get(ParameterValues.UIOBJECT);
        ret.put(ReturnValues.VALUES, file);
        try {
            final ChartRenderingInfo renderInf = getChartRenderingInfo();
            ChartUtilities.saveChartAsPNG(file, chart, getWidth(_parameter), getHeigth(_parameter), renderInf);
            final String map = ChartUtilities.getImageMap(field.getName() + "eFapsChartMap", renderInf);
            ret.put(ReturnValues.SNIPLETT, map);
        } catch (final IOException e) {
            throw new EFapsException(AbstractChart.class, "IOException", e);
        }
        return ret;
    }

    /**
     * Get the Height for the chart. Default value is 500. Can be set by adding a property "Height" to the event
     * definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the Height for the chart
     * @throws EFapsException on error
     */
    protected int getHeigth(final Parameter _parameter)
        throws EFapsException
    {
        Integer ret = 500;
        final String heightStr = getProperty(_parameter, "Height");
        if (heightStr != null && !heightStr.isEmpty()) {
            ret = Integer.parseInt(heightStr);
        }
        return ret;
    }

    /**
     * Get the Width for the chart. Default value is 500. Can be set by adding a property "Width" to the event
     * definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the Width for the chart
     * @throws EFapsException on error
     */
    protected Integer getWidth(final Parameter _parameter)
        throws EFapsException
    {
        Integer ret = 500;
        final String widthStr = getProperty(_parameter, "Width");
        if (widthStr != null && !widthStr.isEmpty()) {
            ret = Integer.parseInt(widthStr);
        }
        return ret;
    }

    /**
     * @return the Rendering info
     */
    protected ChartRenderingInfo getChartRenderingInfo()
    {
        return new ChartRenderingInfo(new StandardEntityCollection());
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return the filename of the temporary file
     */
    protected String getFileName(final Parameter _parameter)
    {
        return Long.toString(new Date().getTime());
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return "png"
     */
    protected String getEnding(final Parameter _parameter)
    {
        return "png";
    }

    /**
     * @param _parameter Parameter as passed by the eFaps API
     * @return JFreeCHart
     * @throws EFapsException on error
     */
    protected abstract JFreeChart createChart(final Parameter _parameter)
        throws EFapsException;

    /**
     * Get the Title for the chart. Default value is the Label from the related Field. Can be set by adding a property
     * "Title" to the event definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return the Width for the chart
     * @throws EFapsException on error
     */
    protected String getTitel(final Parameter _parameter)
        throws EFapsException
    {
        String ret = getProperty(_parameter, "Title");
        if (ret.isEmpty()) {
            final FieldChart field = (FieldChart) _parameter.get(ParameterValues.UIOBJECT);
            ret = field.getLabel();
        }
        return DBProperties.getProperty(ret);
    }

    /**
     * Include the Tooltip. Default true. Can be set by adding a property "Tooltip" to the event definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return inlcude tooltip
     * @throws EFapsException on error
     */
    protected boolean isIncludeTooltip(final Parameter _parameter)
        throws EFapsException
    {
        boolean ret = true;
        final String tooltipStr = getProperty(_parameter, "Tooltip");
        if (tooltipStr != null && !tooltipStr.isEmpty()) {
            ret = "TRUE".equalsIgnoreCase(tooltipStr);
        }
        return ret;
    }

    /**
     * Include the Legend. Default true. Can be set by adding a property "Legend" to the event definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @return include Legend
     * @throws EFapsException on error
     */
    protected boolean isIncludeLegend(final Parameter _parameter)
        throws EFapsException
    {
        boolean ret = true;
        final String legendStr = getProperty(_parameter, "Legend");
        if (legendStr != null && !legendStr.isEmpty()) {
            ret = "TRUE".equalsIgnoreCase(legendStr);
        }
        return ret;
    }

    protected PlotOrientation getOrientation(final Parameter _parameter)
        throws EFapsException
    {
        PlotOrientation ret = PlotOrientation.VERTICAL;
        final String orientStr = getProperty(_parameter, "Orientation");
        if (orientStr != null && !orientStr.isEmpty()) {
            if ("VERTICAL".equalsIgnoreCase(orientStr)) {
                ret = PlotOrientation.VERTICAL;
            } else {
                ret = PlotOrientation.HORIZONTAL;
            }
        }
        return ret;
    }

    /**
     * Get a property from the event definition.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _key Key for the Property
     * @return String value. Empty String if not found.
     * @throws EFapsException on error
     */
    protected String getProperty(final Parameter _parameter,
                                 final String _key)
        throws EFapsException
    {
        String ret = "";
        final Map<?, ?> props = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        if (props != null && props.containsKey(_key)) {
            ret = (String) props.get(_key);
        }
        return ret;
    }
}
