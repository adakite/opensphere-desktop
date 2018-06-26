package io.opensphere.mantle.plugin.queryregion;

import java.util.Collection;

import io.opensphere.core.model.time.TimeSpan;

/**
 * A region that data is requested.
 */
public interface QueryRegion extends TypedGeometicRegion
{
    /**
     * Get the times that this query region is valid.
     *
     * @return The valid times.
     */
    Collection<? extends TimeSpan> getValidTimes();
}
