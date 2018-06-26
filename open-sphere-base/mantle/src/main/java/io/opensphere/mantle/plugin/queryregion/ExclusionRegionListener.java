package io.opensphere.mantle.plugin.queryregion;

/**
 * The listener interface for receiving exclusionRegion events.
 */
public interface ExclusionRegionListener
{
    /**
     * All exclusions removed.
     *
     * @param animationPlanCancelled true if the animation plan was cancelled,
     *            causing this remove all queries.
     */
    void allExclusionsRemoved(boolean animationPlanCancelled);

    /**
     * Exclusion region added.
     *
     * @param region the region that was added
     */
    void exclusionRegionAdded(ExclusionRegion region);

    /**
     * Exclusion region removed.
     *
     * @param region the region that was removed
     */
    void exclusionRegionRemoved(ExclusionRegion region);
}
