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

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractChart_Base.java 6121 2011-02-05 03:42:38Z
 *          jan@moxter.net $
 */
@EFapsUUID("87db5f83-fe92-4b41-a6fd-a23655e37bd8")
@EFapsRevision("$Rev$")
public abstract class AbstractChart_Base
    implements EventExecution
{

    /**
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @param _parameter
     * @return
     */
    private int getHeigth(final Parameter _parameter)
        throws EFapsException
    {
        Integer ret = 500;
        final String heightStr = getProperty(_parameter, "Height");
        if (heightStr != null && !heightStr.isEmpty()) {
            ret = Integer.parseInt(heightStr);
        }
        return ret;
    }

    protected ChartRenderingInfo getChartRenderingInfo()
    {
        return new ChartRenderingInfo(new StandardEntityCollection());
    }

    protected String getFileName(final Parameter _parameter)
    {
        return Long.toString(new Date().getTime());
    }

    protected String getEnding(final Parameter _parameter)
    {
        return "png";
    }

    protected abstract JFreeChart createChart(final Parameter _parameter)
        throws EFapsException;

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
