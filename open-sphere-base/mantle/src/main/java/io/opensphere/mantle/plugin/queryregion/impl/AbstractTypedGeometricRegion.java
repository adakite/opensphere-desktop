package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.plugin.queryregion.TypedGeometicRegion;

/**
 *
 */
public abstract class AbstractTypedGeometricRegion implements TypedGeometicRegion
{
    /** The geometries. */
    private final Collection<? extends PolygonGeometry> myGeometries;

    /**
     * Mapping of type keys for layers associated with the query to filters for
     * those layers.
     */
    private final Map<? extends String, ? extends DataFilter> myTypeKeyToFilterMap;

    /** The ID of the query region. */
    private volatile String myId;

    /**
     * Constructor.
     *
     * @param geometries The geometries that compose the query region.
     * @param typeKeyToFilterMap Mapping of type keys for layers associated with
     *            the query to filters for those layers.
     */
    public AbstractTypedGeometricRegion(Collection<? extends PolygonGeometry> geometries,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap)
    {
        myGeometries = New.unmodifiableCollection(Utilities.checkNull(geometries, "geometries"));
        myTypeKeyToFilterMap = New.unmodifiableMap(Utilities.checkNull(typeKeyToFilterMap, "typeKeyToFilterMap"));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.TypedGeometicRegion#appliesToType(java.lang.String)
     */
    @Override
    public boolean appliesToType(String typeKey)
    {
        return myTypeKeyToFilterMap.isEmpty() || myTypeKeyToFilterMap.containsKey(typeKey);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.TypedGeometicRegion#getGeometries()
     */
    @Override
    public Collection<? extends PolygonGeometry> getGeometries()
    {
        return myGeometries;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.TypedGeometicRegion#getTypeKeys()
     */
    @Override
    public Collection<? extends String> getTypeKeys()
    {
        return myTypeKeyToFilterMap.keySet();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.TypedGeometicRegion#getTypeKeyToFilterMap()
     */
    @Override
    public Map<? extends String, ? extends DataFilter> getTypeKeyToFilterMap()
    {
        return myTypeKeyToFilterMap;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.TypedGeometicRegion#getId()
     */
    @Override
    public String getId()
    {
        return myId;
    }

    /**
     * Sets the value of the id ({@link #myId}) field.
     *
     * @param id the value to store in the {@link #myId} field.
     */
    public void setId(String id)
    {
        myId = id;
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
        sb.append(getTypeKeyToFilterMap());
        return sb.toString();
    }
}
