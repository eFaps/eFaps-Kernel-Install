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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.io.FileUtils;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("31750f1c-96ae-4593-aee0-d687074605a9")
@EFapsApplication("eFaps-Kernel")
public class TmpFileCleanup
    implements Job
{

    private static final Logger LOG = LoggerFactory.getLogger(TmpFileCleanup.class);

    @Override
    public void execute(final JobExecutionContext context)
        throws JobExecutionException
    {
        final var tmpFolder = AppConfigHandler.get().getTempFolder();
        if (tmpFolder != null && tmpFolder.exists()) {
            try {
                final var fileIterator = FileUtils.iterateFiles(tmpFolder, null, true);
                while (fileIterator.hasNext()) {
                    final var file = fileIterator.next();
                    final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    LOG.info("Checking file: {} - creationTime: {}", attr.creationTime());
                    final var fileCreationTime = attr.creationTime().toInstant();
                    final Duration duration = Duration.between(fileCreationTime, Instant.now());
                    if (duration.toHours() != 0) {
                        file.delete();
                        LOG.info("    - deleted");
                    } else {
                        LOG.info("    - skipped");
                    }
                }
            } catch (final IOException e) {
                LOG.error("Catched", e);
            }
        }
    }
}
