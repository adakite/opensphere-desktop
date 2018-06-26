package io.opensphere.mantle.plugin.queryregion;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;

/**
 * A generic super-interface for regions composed of geometries, that are bound
 * to datatypes.
 */
public interface TypedGeometicRegion
{
    /**
     * Determine if this region applies to a data type.
     *
     * @param typeKey The key for the data type.
     * @return {@code true} if the region applies to the type.
     */
    boolean appliesToType(String typeKey);

    /**
     * Get the polygons that compose the region.
     *
     * @return The polygons.
     */
    Collection<? extends PolygonGeometry> getGeometries();

    /**
     * Get the type keys associated with this region. If this is empty, this
     * region applies to all data types.
     *
     * @return The type keys.
     */
    Collection<? extends String> getTypeKeys();

    /**
     * Get a mapping of type keys for layers associated with the to filters for
     * those layers.
     *
     * @return The type key to filter map.
     */
    Map<? extends String, ? extends DataFilter> getTypeKeyToFilterMap();

    /**
     * Gets the ID of this region.
     *
     * @return the ID
     */
    String getId();
}
