package io.opensphere.core.animationhelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Triple;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * A {@link TimeSpanGovernorManager} that can clear data based on a
 * {@link Triple} object whose null values represent wildcards.
 *
 * @param <T> The type of the first object in the pair context.
 * @param <Q> The type of the second object in the pair context.
 * @param <E> The type of the third object in the pair context.
 */
public class TriadGovernerManager<T, Q, E> extends TimeSpanGovernorManager<Triple<T, Q, E>>
{
    /**
     * Constructs a new governor.
     *
     * @param generator The function to generate new governors.
     */
    public TriadGovernerManager(Function<Triple<T, Q, E>, TimeSpanGovernor> generator)
    {
        super(generator);
    }

    @Override
    public void clearData(Triple<T, Q, E> context)
    {
        List<TimeSpanGovernor> governors = getGovernors(context, false, true);
        for (TimeSpanGovernor governor : governors)
        {
            governor.clearData();
        }
    }

    @Override
    public void clearData(Triple<T, Q, E> context, Collection<? extends TimeSpan> timeSpans)
    {
        List<TimeSpanGovernor> governors = getGovernors(context, false, false);
        for (TimeSpanGovernor governor : governors)
        {
            governor.clearData(timeSpans);
        }
    }

    @Override
    public void requestData(Triple<T, Q, E> context, Collection<? extends TimeSpan> timeSpans)
    {
        List<TimeSpanGovernor> governors = getGovernors(context, true, false);

        for (TimeSpan span : timeSpans)
        {
            for (TimeSpanGovernor governor : governors)
            {
                governor.requestData(span);
            }
        }
    }

    /**
     * Gets all governors that match context.
     *
     * @param context The key to key governors, either object in the pair can be
     *            null for wild card.
     * @param isCreate True if we should create a new governor if there isn't
     *            one in the map.
     * @param isRemove True if we should remove the found governors, false if we
     *            should leave them be.
     * @return The governors that match the context.
     */
    private List<TimeSpanGovernor> getGovernors(Triple<T, Q, E> context, boolean isCreate, boolean isRemove)
    {
        List<TimeSpanGovernor> matchingGovernors = New.list();
        Map<Triple<T, Q, E>, TimeSpanGovernor> governors = getTimeSpanGovernors();
        synchronized (governors)
        {
            // Find wildcards
            if (context.getLeft() == null || context.getMiddle() == null || context.getRight() == null)
            {
                List<Triple<T, Q, E>> collectedKeys = New.list();
                for (Map.Entry<Triple<T, Q, E>, TimeSpanGovernor> entry : governors.entrySet())
                {
                    boolean matches = (context.getLeft() == null || context.getLeft().equals(entry.getKey().getLeft()))
                            && (context.getMiddle() == null || context.getMiddle().equals(entry.getKey().getMiddle()))
                            && (context.getRight() == null || context.getRight().equals(entry.getKey().getRight()));
                    if (matches)
                    {
                        TimeSpanGovernor governor = entry.getValue();
                        if (governor != null)
                        {
                            collectedKeys.add(entry.getKey());
                            matchingGovernors.add(governor);
                        }
                    }
                }

                if (isRemove)
                {
                    for (Triple<T, Q, E> key : collectedKeys)
                    {
                        governors.remove(key);
                    }
                }
            }
            // Direct lookup in map
            else
            {
                TimeSpanGovernor governor = null;
                if (isCreate)
                {
                    governor = super.getGovernor(context);
                }
                else if (isRemove)
                {
                    governor = governors.remove(context);
                }
                else
                {
                    governor = governors.get(context);
                }

                if (governor != null)
                {
                    matchingGovernors.add(governor);
                }
            }
        }

        return matchingGovernors;
    }
}
