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

package org.efaps.esjp.common.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Utility class used to create empty files in a user depended temporarily
 * file-folder architecture.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b3bae05a-8db6-4a89-9f84-37564945049d")
@EFapsRevision("$Rev$")
public abstract class FileUtil_Base
{
    /**
     * Name of the temp folder.
     */
    public static final String TMPFOLDERNAME = "eFapsUserDepTemp";

    /**
     * Method to get a file with given name and ending.
     * Spaces will be replaced by underscores.
     * @param _name     name for the file
     * @param _ending   ending for the file
     * @return file
     * @throws EFapsException on error
     */
    public File getFile(final String _name,
                        final String _ending)
        throws EFapsException
    {
        return getFile(_name + "." + _ending);
    }

    /**
     * Method to get a file with given name and ending.
     * @param _name     name for the file
     * @return file
     * @throws EFapsException on error
     */
    public File getFile(final String _name)
        throws EFapsException
    {
        File ret = null;
        try {
            final File temp = File.createTempFile("eFaps", ".tmp");
            final File tmpfld = temp.getParentFile();
            temp.delete();
            final File storeFolder = new File(tmpfld, FileUtil_Base.TMPFOLDERNAME);
            final NumberFormat formater = NumberFormat.getInstance();
            formater.setMinimumIntegerDigits(8);
            formater.setGroupingUsed(false);
            final File userFolder = new File(storeFolder, formater.format(Context.getThreadContext().getPersonId()));
            if (!userFolder.exists()) {
                userFolder.mkdirs();
            }
            final String name = StringUtils.stripAccents(_name);
            ret = new File(userFolder,  name.replaceAll("[^a-zA-Z0-9.-]", "_"));
        } catch (final IOException e) {
            throw new EFapsException(FileUtil_Base.class, "IOException", e);
        }
        return ret;
    }

    public File combinePdfs(final List<File> _files,
                            final String _fileName,
                            final boolean _paginate)
        throws EFapsException
    {
        File ret = null;
        if (_files.size() == 1) {
            ret = _files.get(0);
        } else {
            try {
                final List<InputStream> pdfs = new ArrayList<InputStream>();
                for (final File file : _files) {
                    pdfs.add(new FileInputStream(file));
                }
                ret = getFile(_fileName, "pdf");
                final OutputStream outputStream = new FileOutputStream(ret);
                final Document document = new Document();
                try {
                    final List<PdfReader> readers = new ArrayList<PdfReader>();
                    int totalPages = 0;
                    final Iterator<InputStream> iteratorPDFs = pdfs.iterator();

                    // Create Readers for the pdfs.
                    while (iteratorPDFs.hasNext()) {
                        final InputStream pdf = iteratorPDFs.next();
                        final PdfReader pdfReader = new PdfReader(pdf);
                        readers.add(pdfReader);
                        totalPages += pdfReader.getNumberOfPages();
                    }
                    // Create a writer for the outputstream
                    final PdfWriter writer = PdfWriter.getInstance(document, outputStream);

                    document.open();
                    final BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
                                    BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    final PdfContentByte cb = writer.getDirectContent(); // Holds the
                                                                   // PDF
                    // data

                    PdfImportedPage page;
                    int currentPageNumber = 0;
                    int pageOfCurrentReaderPDF = 0;
                    final Iterator<PdfReader> iteratorPDFReader = readers.iterator();

                    // Loop through the PDF files and add to the output.
                    while (iteratorPDFReader.hasNext()) {
                        final PdfReader pdfReader = iteratorPDFReader.next();

                        // Create a new page in the target for each source page.
                        while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                            document.newPage();
                            pageOfCurrentReaderPDF++;
                            currentPageNumber++;
                            page = writer.getImportedPage(pdfReader,
                                            pageOfCurrentReaderPDF);
                            cb.addTemplate(page, 0, 0);

                            // Code for pagination.
                            if (_paginate) {
                                cb.beginText();
                                cb.setFontAndSize(bf, 9);
                                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, ""
                                                + currentPageNumber + " of " + totalPages, 520,
                                                5, 0);
                                cb.endText();
                            }
                        }
                        pageOfCurrentReaderPDF = 0;
                    }
                    outputStream.flush();
                    document.close();
                    outputStream.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    if (document.isOpen()) {
                        document.close();
                    }
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (final IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

            } catch (final FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ret;
    }
}
