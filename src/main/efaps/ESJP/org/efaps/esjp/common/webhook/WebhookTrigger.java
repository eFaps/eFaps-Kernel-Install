package org.efaps.esjp.common.webhook;

import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("6cc773ca-14be-46d3-9f53-80e7e00e19d9")
@EFapsApplication("eFaps-Kernel")
public class WebhookTrigger
{

    private static final Logger LOG = LoggerFactory.getLogger(WebhookTrigger.class);

    public Return execute(final Parameter parameter)
        throws EFapsException
    {
        LOG.info("WebhookTrigger for {}", parameter);
        @SuppressWarnings("unchecked") final var eventType = (String) ((Map<Object, Object>) parameter
                        .get(ParameterValues.PROPERTIES)).get("Event");
        new Webhook().trigger(eventType, BaseDataDto.builder().withOid(parameter.getInstance().getOid()).build());
        return new Return();
    }
}
