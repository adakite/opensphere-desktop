package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.mantle.plugin.queryregion.ExclusionRegion;

/**
 * The default implementation of an exclusion region.
 */
public class DefaultExclusionRegion extends AbstractTypedGeometricRegion implements ExclusionRegion
{
    /**
     * Constructor.
     *
     * @param geometries the geometries from which to exclude results.
     * @param typeKeyToFilterMap the types to which the exclusions apply.
     */
    public DefaultExclusionRegion(Collection<? extends PolygonGeometry> geometries,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap)
    {
        super(geometries, typeKeyToFilterMap);
    }
}
