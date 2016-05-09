/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */
package org.efaps.esjp.admin.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.efaps.admin.index.IAnalyzerProvider;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The Class AnalyzerProvider_Base.
 *
 * @author The eFaps Team
 */
public abstract class AnalyzerProvider_Base
    implements IAnalyzerProvider
{

    @Override
    public Analyzer getAnalyzer()
        throws EFapsException
    {
        return getAnalyzer(null, Context.getThreadContext().getLanguage());
    }

    /**
     * Gets the analyzer.
     *
     * @param _companyId the _company id
     * @param _language the _language
     * @return the analyzer
     */
    public Analyzer getAnalyzer(final Long _companyId,
                                final String _language)
    {
        StandardAnalyzer ret;
        switch (_language) {
            case "de":
                ret = new StandardAnalyzer(GermanAnalyzer.getDefaultStopSet());
                break;
            case "es":
                ret = new StandardAnalyzer(SpanishAnalyzer.getDefaultStopSet());
                break;
            case "en":
            default:
                ret = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet());
                break;
        }
        return ret;
    }
}
