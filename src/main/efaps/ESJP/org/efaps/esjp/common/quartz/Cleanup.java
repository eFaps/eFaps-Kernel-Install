package org.efaps.esjp.common.quartz;

import org.efaps.admin.common.Quartz;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("3b5ac999-29dd-4979-b72a-36aa925ae9b2")
@EFapsApplication("eFaps-Kernel")
public class Cleanup
{

    private static final Logger LOG = LoggerFactory.getLogger(Cleanup.class);

    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        LOG.info("Recieved request for cleaning and re-initializing of Quartz by: {}",
                        Context.getThreadContext().getPerson().getName());
        Quartz.reset();
        Quartz.initialize();
        return new Return();
    }
}
