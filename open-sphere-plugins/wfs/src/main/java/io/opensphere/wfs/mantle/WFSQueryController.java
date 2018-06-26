package io.opensphere.wfs.mantle;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animationhelper.TriadGovernerManager;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.plugin.queryregion.ExclusionRegion;
import io.opensphere.mantle.plugin.queryregion.ExclusionRegionListener;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wfs.layer.WFSDataType;

/**
 * Controls when to query and what to query when WFS feature layers are active.
 */
public class WFSQueryController implements QueryRegionListener, ExclusionRegionListener,
        EventListener<ActiveDataGroupsChangedEvent>, ListDataListener<TimeSpan>
{
    /**
     * Used to listen for layer deactivations.
     */
    private final EventManager myEventManager;

    /**
     * Manages all the {@link WFSGovernor}.
     */
    private final TriadGovernerManager<DataTypeInfo, QueryRegion, Collection<ExclusionRegion>> myGovernorManager;

    /**
     * Responsible for notifying us of new query regions.
     */
    private final QueryRegionManager myQueryManager;

    /**
     * Notifies us when load times have changed.
     */
    private final TimeManager myTimeManager;

    /**
     * Used to get WFS layers.
     */
    private final DataTypeController myTypeController;

    /**
     * Constructs a new query controller.
     *
     * @param toolbox The system toolbox.
     */
    public WFSQueryController(Toolbox toolbox)
    {
        MantleToolbox mantle = MantleToolboxUtils.getMantleToolbox(toolbox);
        myQueryManager = mantle.getQueryRegionManager();
        myEventManager = toolbox.getEventManager();
        myTimeManager = toolbox.getTimeManager();
        myGovernorManager = new TriadGovernerManager<>(
                p -> new WFSGovernor(mantle, toolbox.getDataRegistry(), p.getLeft(), p.getMiddle(), p.getRight()));
        myTypeController = mantle.getDataTypeController();
        myQueryManager.addQueryRegionListener(this);
        myQueryManager.addExclusionRegionListener(this);
        myEventManager.subscribe(ActiveDataGroupsChangedEvent.class, this);
        myTimeManager.getLoadTimeSpans().addChangeListener(this);
    }

    @Override
    public void allQueriesRemoved(boolean animationPlanCancelled)
    {
        myGovernorManager.clearData(new ImmutableTriple<>(null, null, null));
    }

    /**
     * Stops listening for time and query region changes. Removes all wfs data
     * from the globe.
     */
    public void close()
    {
        myQueryManager.removeQueryRegionListener(this);
        myQueryManager.removeExclusionRegionListener(this);
        myEventManager.unsubscribe(ActiveDataGroupsChangedEvent.class, this);
        myTimeManager.getLoadTimeSpans().removeChangeListener(this);
        myGovernorManager.clearData(new ImmutableTriple<>(null, null, null));
    }

    @Override
    public void elementsAdded(ListDataEvent<TimeSpan> e)
    {
        myGovernorManager.requestData(new ImmutableTriple<>(null, null, null), e.getChangedElements());
    }

    @Override
    public void elementsChanged(ListDataEvent<TimeSpan> e)
    {
        List<TimeSpan> removeSpans = New.list();
        int index = 0;
        for (TimeSpan previous : e.getPreviousElements())
        {
            TimeSpan newSpan = e.getChangedElements().get(index);
            if (!previous.overlaps(newSpan))
            {
                removeSpans.add(previous);
            }
            else
            {
                removeSpans.addAll(previous.subtract(newSpan));
            }
            index++;
        }
        if (!removeSpans.isEmpty())
        {
            myGovernorManager.clearData(new ImmutableTriple<>(null, null, null), removeSpans);
        }
        myGovernorManager.requestData(new ImmutableTriple<>(null, null, null), e.getChangedElements());
    }

    @Override
    public void elementsRemoved(ListDataEvent<TimeSpan> e)
    {
        myGovernorManager.clearData(new ImmutableTriple<>(null, null, null), e.getChangedElements());
    }

    @Override
    public void notify(ActiveDataGroupsChangedEvent event)
    {
        for (DataGroupInfo group : event.getDeactivatedGroups())
        {
            for (DataTypeInfo dataType : group.getMembers(false))
            {
                if (dataType instanceof WFSDataType)
                {
                    myGovernorManager.clearData(new ImmutableTriple<>(dataType, null, null));
                }
            }
        }
    }

    @Override
    public void queryRegionAdded(QueryRegion region)
    {
        Collection<WFSDataType> wfsLayers = CollectionUtilities.filterDowncast(myTypeController.getDataTypeInfo(),
                WFSDataType.class);
        for (DataTypeInfo wfsLayer : wfsLayers)
        {
            if (wfsLayer.isVisible())
            {
                Collection<? extends TimeSpan> timeSpans = myTimeManager.getLoadTimeSpans();
                if (wfsLayer.getTimeExtents() == null || wfsLayer.getTimeExtents().getExtent().isTimeless())
                {
                    timeSpans = New.list(TimeSpan.TIMELESS);
                }
                Collection<ExclusionRegion> exclusionRegions = myQueryManager.getExclusionRegions().stream()
                        .map(e -> (ExclusionRegion)e).collect(Collectors.toList());
                myGovernorManager.requestData(new ImmutableTriple<>(wfsLayer, region, exclusionRegions), timeSpans);
            }
        }
    }

    @Override
    public void queryRegionRemoved(QueryRegion region)
    {
        myGovernorManager.clearData(new ImmutableTriple<>(null, region, null));
    }

    /**
     * Gets the governor manager.
     *
     * @return The governor manager.
     */
    protected TriadGovernerManager<DataTypeInfo, QueryRegion, Collection<ExclusionRegion>> getGovernorManager()
    {
        return myGovernorManager;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.ExclusionRegionListener#allExclusionsRemoved(boolean)
     */
    @Override
    public void allExclusionsRemoved(boolean animationPlanCancelled)
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.ExclusionRegionListener#exclusionRegionAdded(io.opensphere.mantle.plugin.queryregion.ExclusionRegion)
     */
    @Override
    public void exclusionRegionAdded(ExclusionRegion region)
    {
        Collection<WFSDataType> wfsLayers = CollectionUtilities.filterDowncast(myTypeController.getDataTypeInfo(),
                WFSDataType.class);
        for (DataTypeInfo wfsLayer : wfsLayers)
        {
            if (wfsLayer.isVisible())
            {
                Collection<? extends TimeSpan> timeSpans = myTimeManager.getLoadTimeSpans();
                if (wfsLayer.getTimeExtents() == null || wfsLayer.getTimeExtents().getExtent().isTimeless())
                {
                    timeSpans = New.list(TimeSpan.TIMELESS);
                }
                myGovernorManager.requestData(new ImmutableTriple<>(wfsLayer, null, Collections.singleton(region)), timeSpans);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.plugin.queryregion.ExclusionRegionListener#exclusionRegionRemoved(io.opensphere.mantle.plugin.queryregion.ExclusionRegion)
     */
    @Override
    public void exclusionRegionRemoved(ExclusionRegion region)
    {
        // TODO Auto-generated method stub

    }
}
