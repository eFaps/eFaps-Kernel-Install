/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.esjp.common.file;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSmartCopy;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used to create empty files in a user depended temporarily
 * file-folder architecture. In the standard implementation this folder is
 * synchronized to be accessed by a servlet serving the actual file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b3bae05a-8db6-4a89-9f84-37564945049d")
@EFapsApplication("eFaps-Kernel")
public abstract class FileUtil_Base
    extends AbstractCommon
{

    /**
     * Name of the folder inside the "official" temporary folder.
     */
    public static final String TMPFOLDERNAME = "eFapsUserDepTemp";

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Method to get a file with given name and ending. Spaces will be replaced
     * by underscores.
     *
     * @param _name name for the file
     * @param _ending ending for the file
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
     *
     * @param _name name for the file
     * @return file
     * @throws EFapsException on error
     */
    public File getFile(final String _name)
        throws EFapsException
    {
        File ret = null;
        try {
            File tmpfld = AppConfigHandler.get().getTempFolder();
            if (tmpfld == null) {
                final File temp = File.createTempFile("eFaps", ".tmp");
                tmpfld = temp.getParentFile();
                temp.delete();
            }
            final File storeFolder = new File(tmpfld, FileUtil_Base.TMPFOLDERNAME);
            final NumberFormat formater = NumberFormat.getInstance();
            formater.setMinimumIntegerDigits(8);
            formater.setGroupingUsed(false);
            final File userFolder = new File(storeFolder, formater.format(Context.getThreadContext().getPersonId()));
            if (!userFolder.exists()) {
                userFolder.mkdirs();
            }
            final String name = StringUtils.stripAccents(_name);
            ret = new File(userFolder, name.replaceAll("[^a-zA-Z0-9.-]", "_"));
        } catch (final IOException e) {
            throw new EFapsException(FileUtil_Base.class, "IOException", e);
        }
        return ret;
    }

    /**
     * @param _files pdfs to be combined into one file
     * @param _fileName name of the file to be generated
     * @param _paginate paginat or not
     * @return file
     * @throws EFapsException on error
     */
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
                final List<InputStream> pdfs = new ArrayList<>();
                for (final File file : _files) {
                    pdfs.add(new FileInputStream(file));
                }
                ret = getFile(_fileName, "pdf");
                final OutputStream outputStream = new FileOutputStream(ret);
                final Document document = new Document();
                try {
                    final List<PdfReader> readers = new ArrayList<>();
                    int totalPages = 0;
                    final Iterator<InputStream> iteratorPDFs = pdfs.iterator();

                    // Create Readers for the pdfs.
                    while (iteratorPDFs.hasNext()) {
                        final InputStream pdf = iteratorPDFs.next();
                        final PdfReader pdfReader = new PdfReader(pdf);
                        readers.add(pdfReader);
                        totalPages += pdfReader.getNumberOfPages();
                    }
                    final PdfSmartCopy copy = new PdfSmartCopy(document, outputStream);
                    final Iterator<PdfReader> iteratorPDFReader = readers.iterator();
                    document.open();
                    while (iteratorPDFReader.hasNext()) {
                        final PdfReader pdfReader = iteratorPDFReader.next();
                        for (int i = 0; i < pdfReader.getNumberOfPages(); i++) {
                            final PdfImportedPage importedPage = copy.getImportedPage(pdfReader, i + 1);
                            copy.addPage(importedPage);

                            if (_paginate) {
                                LOG.debug("Missing page ", totalPages);
                            }
                        }
                    }
                    outputStream.flush();
                    document.close();
                    outputStream.close();
                    // CHECKSTYLE:OFF
                } catch (final Exception e) {
                    // CHECKSTYLE:ON
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
                LOG.error("FileNotFoundException", e);
            }
        }
        return ret;
    }

    /**
     * Convert.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _file the file
     * @param _fileName the file name
     * @return the file
     * @throws EFapsException on error
     */
    public File convertPdf(final Parameter _parameter,
                           final File _file,
                           final String _fileName)
        throws EFapsException
    {
        File ret = _file;
        if (containsProperty(_parameter, "PageSize")) {
            ret = resizePdf(_parameter, ret, _fileName, getProperty(_parameter, "PageSize"));
        }
        if (containsProperty(_parameter, "NUpPow")) {
            ret = nUpPdf(_parameter, ret, _fileName);
        }
        return ret;
    }

    /**
     * Resize.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _file the file
     * @param _fileName the file name
     * @param _pageSize the page size
     * @return the file
     * @throws EFapsException on error
     */
    public File resizePdf(final Parameter _parameter,
                          final File _file,
                          final String _fileName,
                          final String _pageSize)
        throws EFapsException
    {
        final Document document = new Document();

        final File ret = getFile(_fileName, "pdf");
        try {
            final File destFile = new File(_file.getPath() + ".tmp");
            FileUtils.copyFile(_file, destFile);
            final OutputStream outputStream = new FileOutputStream(ret);
            final PdfReader pdfReader = new PdfReader(new FileInputStream(destFile));

            // Create a writer for the outputstream
            final PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfImportedPage page;
            final PdfContentByte cb = writer.getDirectContent();
            int pageOfCurrentReaderPDF = 0;
            // Create a new page in the target for each source page.
            while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                document.newPage();
                pageOfCurrentReaderPDF++;
                page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                document.setPageSize(page.getWidth() <= page.getHeight() ? PageSize.getRectangle(_pageSize)
                                : PageSize.getRectangle(_pageSize).rotate());
                final float widthFactor = document.getPageSize().getWidth() / page.getWidth();
                final float heightFactor = document.getPageSize().getHeight() / page.getHeight();
                final float factor = Math.min(widthFactor, heightFactor);
                final float offsetX = (document.getPageSize().getWidth() - page.getWidth() * factor) / 2;
                final float offsetY = (document.getPageSize().getHeight() - page.getHeight() * factor) / 2;
                cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);
            }
            pageOfCurrentReaderPDF = 0;
            outputStream.flush();
            document.close();
            outputStream.close();
        } catch (final FileNotFoundException e) {
            LOG.error("FileNotFoundException", e);
        } catch (final IOException e) {
            LOG.error("IOException", e);
        } catch (final DocumentException e) {
            LOG.error("DocumentException", e);
        }
        return ret;
    }

    /**
     * N up.
     *
     * @param _parameter Parameter as passed by the eFaps API
     * @param _file the file
     * @param _fileName the file name
     * @return the file
     * @throws EFapsException on error
     */
    public File nUpPdf(final Parameter _parameter,
                       final File _file,
                       final String _fileName)
        throws EFapsException
    {
        final File ret = getFile(_fileName, "pdf");
        try {
            final int pow = Integer.parseInt(getProperty(_parameter, "NUpPow", "1"));
            final boolean duplicate = "true".equalsIgnoreCase(getProperty(_parameter, "NUpDuplicate", "true"));
            final File destFile = new File(_file.getPath() + ".tmp");
            FileUtils.copyFile(_file, destFile);
            final OutputStream outputStream = new FileOutputStream(ret);
            final PdfReader pdfReader = new PdfReader(new FileInputStream(destFile));

            final Rectangle pageSize = pdfReader.getPageSize(1);

            final Rectangle newSize = pow % 2 == 0 ? new Rectangle(pageSize.getWidth(), pageSize.getHeight())
                            : new Rectangle(pageSize.getHeight(), pageSize.getWidth());

            Rectangle unitSize = new Rectangle(pageSize.getWidth(), pageSize.getHeight());

            for (int i = 0; i < pow; i++) {
                unitSize = new Rectangle(unitSize.getHeight() / 2, unitSize.getWidth());
            }

            final int n = (int) Math.pow(2, pow);
            final int r = (int) Math.pow(2, pow / 2);
            final int c = n / r;

            final Document document = new Document(newSize, 0, 0, 0, 0);

            // Create a writer for the outputstream
            final PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfImportedPage page;
            final PdfContentByte cb = writer.getDirectContent();
            // Create a new page in the target for each source page.
            Rectangle currentSize;
            float offsetX;
            float offsetY;
            float factor;

            final int total = pdfReader.getNumberOfPages();
            for (int i = 0; i < total;) {
                if (i % n == 0) {
                    document.newPage();
                }
                currentSize = pdfReader.getPageSize(++i);

                factor = Math.min(unitSize.getWidth() / currentSize.getWidth(), unitSize.getHeight() / currentSize
                                .getHeight());
                offsetX = unitSize.getWidth() * (i % n % c) + (unitSize.getWidth() - currentSize.getWidth() * factor)
                                / 2f;
                offsetY = newSize.getHeight() - (unitSize.getHeight() * (i % n % c) + 1) + (unitSize.getHeight()
                                - currentSize.getHeight() * factor) / 2f;

                page = writer.getImportedPage(pdfReader, i);

                cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);

                if (duplicate) {
                    for (int y = i + 1; y <= pow + 1; y++) {
                        factor = Math.min(unitSize.getWidth() / currentSize.getWidth(), unitSize.getHeight()
                                        / currentSize.getHeight());
                        offsetX = unitSize.getWidth() * (y % n % c) + (unitSize.getWidth() - currentSize.getWidth()
                                        * factor) / 2f;
                        offsetY = newSize.getHeight() - unitSize.getHeight() * (y % n / c + 1) + (unitSize.getHeight()
                                        - currentSize.getHeight() * factor) / 2f;
                        cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);
                    }
                }
            }
            outputStream.flush();
            document.close();
            outputStream.close();
        } catch (final FileNotFoundException e) {
            LOG.error("FileNotFoundException", e);
        } catch (final IOException e) {
            LOG.error("IOException", e);
        } catch (final DocumentException e) {
            LOG.error("DocumentException", e);
        }
        return ret;
    }

}
