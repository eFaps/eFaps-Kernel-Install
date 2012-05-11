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

package org.efaps.esjp.common.history;

import java.io.StringWriter;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class HistoryXML
{

    /**
     * The doc for this xml.
     */
    private Document xmlDoc;

    /**
     * @param _instance Instance
     * @param _event    event name
     * @throws EFapsException on error
     */
    public HistoryXML(final Instance _instance,
                      final String _event)
        throws EFapsException
    {
        try {
            // Create a XML Document
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder;

            docBuilder = dbFactory.newDocumentBuilder();
            this.xmlDoc = docBuilder.newDocument();

            // Create the root element
            final Element root = this.xmlDoc.createElement("history");
            this.xmlDoc.appendChild(root);
            root.setAttribute("oid", _instance.getOid());
            root.setAttribute("event", _event);
            root.setAttribute("date", new DateTime().toString(ISODateTimeFormat.dateTime()));
        } catch (final ParserConfigurationException e) {
            throw new EFapsException("error", e);
        }
    }

    /**
     * @param _attrValues attribute values to be added to the doc
     */
    public void addAttributes(final Set<AttrValue> _attrValues)
    {
        final Node root = this.xmlDoc.getFirstChild();
        final Element attributes = this.xmlDoc.createElement("attributes");
        root.appendChild(attributes);
        for (final AttrValue attrValue : _attrValues) {
            final Element attribute = this.xmlDoc.createElement("attribute");
            attribute.setAttribute("name", attrValue.getName());
            attribute.setAttribute("label", attrValue.getLabel());
            if (attrValue.getLinkOid() != null) {
                attribute.setAttribute("linkoid", attrValue.getLinkOid());
            }
            attribute.setTextContent(attrValue.getValue());
            attributes.appendChild(attribute);
        }
    }

    /**
     * @return the String representation of this xml
     */
    public String getXML()
    {
        final DOMImplementationLS domImplementation = (DOMImplementationLS) this.xmlDoc.getImplementation();
        final LSSerializer lsSerializer = domImplementation.createLSSerializer();
        final DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
        final String ret;
        if (domConfiguration.canSetParameter("format-pretty-print", Boolean.TRUE)) {
            lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            final LSOutput lsOutput = domImplementation.createLSOutput();
            lsOutput.setEncoding("UTF-8");
            final StringWriter stringWriter = new StringWriter();
            lsOutput.setCharacterStream(stringWriter);
            lsSerializer.write(this.xmlDoc, lsOutput);
            return stringWriter.toString();
        } else {
            ret = lsSerializer.writeToString(this.xmlDoc);
        }
        return ret;
    }

    /**
     * Attribute Value.
     */
    public static class AttrValue
    {
        /**
         * Label of this attributevalue.
         */
        private final String label;

        /**
         * Name of this attributevalue.
         */
        private final String name;

        /**
         * Oid of the linked object of this attributevalue.
         */
        private String linkOid;

        /**
         * The value.
         */
        private String value;

        /**
         * Select used for PrintQuery.
         */
        private String select;

        /**
         * @param _label    label
         * @param _name     name
         * @param _select   select
         * @param _linkOid  oid of the linked object
         */
        public AttrValue(final String _label,
                         final String _name,
                         final String _select)
        {
            this.label = _label;
            this.name = _name;
            this.select = _select;
        }

        /**
         * @param _label    label
         * @param _name     name
         * @param _linkOid  oid of the linked object
         */
        public AttrValue(final String _label,
                         final String _name,
                         final String _value,
                         final String _linkOid)
        {
            this.label = _label;
            this.name = _name;
            this.linkOid = _linkOid;
            this.value = _value;
        }

        /**
         * Getter method for the instance variable {@link #label}.
         *
         * @return value of instance variable {@link #label}
         */
        protected String getLabel()
        {
            return this.label;
        }

        /**
         * Getter method for the instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         */
        protected String getName()
        {
            return this.name;
        }

        /**
         * Getter method for the instance variable {@link #linkOid}.
         *
         * @return value of instance variable {@link #linkOid}
         */
        protected String getLinkOid()
        {
            return this.linkOid;
        }

        /**
         * Setter method for instance variable {@link #linkOid}.
         *
         * @param _linkOid value for instance variable {@link #linkOid}
         */

        protected void setLinkOid(final String _linkOid)
        {
            this.linkOid = _linkOid;
        }

        /**
         * Getter method for the instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        protected String getValue()
        {
            return this.value;
        }

        /**
         * Setter method for instance variable {@link #value}.
         *
         * @param _value value for instance variable {@link #value}
         */
        protected void setValue(final String _value)
        {
            this.value = _value;
        }


        /**
         * Getter method for the instance variable {@link #select}.
         *
         * @return value of instance variable {@link #select}
         */
        protected String getSelect()
        {
            return this.select;
        }


        /**
         * Setter method for instance variable {@link #select}.
         *
         * @param _select value for instance variable {@link #select}
         */

        protected void setSelect(final String _select)
        {
            this.select = _select;
        }


        @Override
        public String toString()
        {
            return this.label;
        }
    }
}
