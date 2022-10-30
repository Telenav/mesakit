////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.world.virtual;

import com.telenav.kivakit.interfaces.value.Source;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.graph.world.virtual.types.HardReferencedValue;
import com.telenav.mesakit.graph.world.virtual.types.SoftReferencedValue;
import com.telenav.mesakit.graph.world.virtual.types.WeakReferencedValue;

/**
 * A reference that can be hard or soft depending on how recently it was accessed relative to other references being
 * tracked.
 * <p>
 * The {@link #onLoad()} method loads a value for the reference when none is available (either because no value has been
 * loaded or because the reference was softened and the value was discarded by the garbage collector).
 * <p>
 * The {@link VirtualReferenceTracker} instance passed to the constructor cooperates with this class to track reference
 * accesses (via {@link #get()}), maintaining a fixed number of most-recently-accessed hard references and reducing the
 * rest to soft references.
 * <p>
 * For example, the following code keeps up to four hard-references to most-recently accessed instances of MyClass,
 * keeping any other references as soft-references:
 *
 * <pre>
 *    private static VariableStrengthReference.Tracker&lt;MyClass&gt; tracker = new VariableStrengthReference.Tracker&lt;&gt;(
 *                   Count._4);
 *
 *    private final VariableStrengthReference&lt;MyClass&gt; reference = new VariableStrengthReference&lt;MyClass&gt;(tracker) {
 *
 *       &#64;Override
 *       protected Graph load() {
 *
 *            return ...
 *        }
 *    };
 *
 *    ...
 *
 *    reference.get();
 * </pre>
 * <p>
 * NOTE: The "tracker" field in this example is static so one instance of it can be shared by many reference instances.
 */
@UmlRelation(label = "can take form of", referent = WeakReferencedValue.class)
@UmlRelation(label = "can take form of", referent = SoftReferencedValue.class)
@UmlRelation(label = "can take form of", referent = HardReferencedValue.class)
public abstract class VirtualReference<T>
{
    /** The tracker to which this reference belongs */
    private final VirtualReferenceTracker<T> tracker;

    /**
     * The value, which might be a {@link HardReferencedValue}, a {@link SoftReferencedValue} or a
     * {@link WeakReferencedValue}.
     */
    private volatile Source<T> value = new HardReferencedValue<>(null);

    private volatile boolean triedToLoad;

    protected VirtualReference(VirtualReferenceTracker<T> tracker)
    {
        this.tracker = tracker;
    }

    /**
     * Returns the value for this reference
     */
    public T get()
    {
        // Get the value
        var value = this.value.get();

        // If we got a value
        if (value != null)
        {
            // return it
            return value;
        }

        if (!triedToLoad)
        {
            try
            {
                // There's no value, so load it
                return load();
            }
            catch (Exception ignored)
            {
            }
        }

        triedToLoad = true;
        return null;
    }

    @Override
    public String toString()
    {
        return name();
    }

    protected abstract String name();

    protected abstract T onLoad();

    synchronized void soften()
    {
        // If we're currently pointing to a hard referenced value
        if (value instanceof HardReferencedValue)
        {
            // get the value
            var value = this.value.get();

            // and if it's non-null
            if (value != null)
            {
                // Weaken the reference
                switch (tracker.type())
                {
                    case SOFT -> this.value = new SoftReferencedValue<>(name(), value, tracker.queue());
                    case WEAK -> this.value = new WeakReferencedValue<>(name(), value, tracker.queue());
                    case NONE -> this.value = null;
                }
            }
        }
    }

    private synchronized T load()
    {
        // Get any value that might have been loaded while we were waiting to enter this method
        var value = this.value.get();

        // and if a value was loaded by another thread
        if (value != null)
        {
            // just return that
            return value;
        }

        // Load a new value, if possible
        value = onLoad();

        // If we loaded the value,
        if (value != null)
        {
            // hard reference the value,
            this.value = new HardReferencedValue<>(value);

            // and notify the tracker that it was loaded
            tracker.onLoaded(this);

            return value;
        }
        else
        {
            // we didn't load a value
            return null;
        }
    }
}
