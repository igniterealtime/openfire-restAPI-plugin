package org.jivesoftware.openfire.plugin.rest;

import org.jivesoftware.openfire.stats.i18nStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StatisticsFilter implements ContainerResponseFilter
{
    private static final Logger Log = LoggerFactory.getLogger(StatisticsFilter.class);

    private static final ConcurrentMap<Response.Status.Family, Long> ratePerFamily = new ConcurrentHashMap<>();

    private static ConcurrentMap<Response.Status.Family, Long> getStatsCollection() {
        return ratePerFamily;
    }
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
    {
        final Response.StatusType statusInfo = responseContext.getStatusInfo();
        if (statusInfo == null) {
            Log.warn("Cannot record statistics for a response that contains no status info. Response context object: {}", responseContext);
        } else {
            StatisticsFilter.getStatsCollection().merge(statusInfo.getFamily(), 1L, Long::sum);
        }
    }

    public static Collection<RestResponseFamilyStatistic> generateAllFamilyStatisticInstances() {
        final Collection<RestResponseFamilyStatistic> result = new HashSet<>();
        for (Response.Status.Family family : Response.Status.Family.values()) {
            result.add(new RestResponseFamilyStatistic(family));
        }
        return result;
    }

    public static class RestResponseFamilyStatistic extends i18nStatistic
    {
        public static final String GROUP = "restapi_responses";

        private final Response.Status.Family family;

        public RestResponseFamilyStatistic(@Nonnull final Response.Status.Family family)
        {
            super(GROUP + "." + family.toString().toLowerCase(), "restapi", Type.rate);
            this.family = family;
        }

        @Override
        public double sample()
        {
            final Long oldValue = StatisticsFilter.getStatsCollection().replace(family, 0L);
            return oldValue == null ? 0 : oldValue;
        }

        @Override
        public boolean isPartialSample()
        {
            return true;
        }

        public String getGroupName() {
            return GROUP;
        }

        public String getKeyName() {
            return family.toString().toLowerCase();
        }
    }
}
