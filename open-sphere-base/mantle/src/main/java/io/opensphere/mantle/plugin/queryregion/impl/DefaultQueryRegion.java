package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;

/**
 * Default implementation for {@link QueryRegion}.
 */
public class DefaultQueryRegion extends AbstractTypedGeometricRegion implements QueryRegion
{
    /** The times that the query region is valid. */
    private final Collection<? extends TimeSpan> myValidTimes;

    /**
     * Constructor.
     *
     * @param geometries The geometries that compose the query region.
     * @param validTimes The times that the query region is valid.
     * @param typeKeyToFilterMap Mapping of type keys for layers associated with
     *            the query to filters for those layers.
     */
    public DefaultQueryRegion(Collection<? extends PolygonGeometry> geometries, Collection<? extends TimeSpan> validTimes,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap)
    {
        super(geometries, typeKeyToFilterMap);
        myValidTimes = New.unmodifiableCollection(Utilities.checkNull(validTimes, "validTimes"));
    }

    @Override
    public Collection<? extends TimeSpan> getValidTimes()
    {
        return myValidTimes;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append(" [");
        for (PolygonGeometry geom : getGeometries())
        {
            if (geom.getVertices().size() < 10)
            {
                sb.append(geom).append(' ');
            }
            else
            {
                sb.append(geom.getClass().getSimpleName()).append(" [").append(geom.getVertices().size()).append(" vertices] ");
            }
        }
        for (TimeSpan timeSpan : getValidTimes())
        {
            sb.append(timeSpan).append(' ');
        }
        sb.append(getTypeKeyToFilterMap());
        return sb.toString();
    }
}
