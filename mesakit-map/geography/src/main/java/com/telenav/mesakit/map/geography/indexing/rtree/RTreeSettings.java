////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.geography.indexing.rtree;

import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.kivakit.core.language.reflection.property.KivaKitIncludeProperty;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

public class RTreeSettings
{
    public static final RTreeSettings DEFAULT = new RTreeSettings();

    /** The maximum children for an interior node before it must be split */
    @KivaKitIncludeProperty
    private Maximum maximumChildrenPerInteriorNode = Maximum.maximum(8);

    /** Initial allocation size for interior node */
    private final Estimate estimatedChildrenPerInteriorNode = maximumChildrenPerInteriorNode.asEstimate();

    /** The maximum elements in a leaf node before it must be split */
    @KivaKitIncludeProperty
    private Maximum maximumElementsPerLeaf = Maximum.maximum(32);

    /** Initial allocation size for lead node */
    private Estimate estimatedChildrenPerLeaf = maximumElementsPerLeaf.asEstimate();

    /** The estimated number of nodes in the index */
    private Estimate estimatedNodes = Estimate.estimate(10_000);

    public RTreeSettings()
    {
    }

    private RTreeSettings(RTreeSettings that)
    {
        maximumChildrenPerInteriorNode = that.maximumChildrenPerInteriorNode;
        maximumElementsPerLeaf = that.maximumElementsPerLeaf;
        estimatedNodes = that.estimatedNodes;
        estimatedChildrenPerLeaf = that.estimatedChildrenPerLeaf;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object object)
    {
        return unsupported();
    }

    @KivaKitIncludeProperty
    public Estimate estimatedChildrenPerInteriorNode()
    {
        return estimatedChildrenPerInteriorNode;
    }

    @KivaKitIncludeProperty
    public Estimate estimatedChildrenPerLeaf()
    {
        return estimatedChildrenPerLeaf;
    }

    @KivaKitIncludeProperty
    public Estimate estimatedElements()
    {
        return estimatedNodes;
    }

    @KivaKitIncludeProperty
    public Estimate estimatedLeaves()
    {
        return estimatedElements().dividedBy(maximumElementsPerLeaf);
    }

    @Override
    public int hashCode()
    {
        return unsupported();
    }

    public boolean isInteriorNodeFull(Count size)
    {
        return size.isGreaterThanOrEqualTo(maximumChildrenPerInteriorNode);
    }

    public boolean isLeafFull(Count size)
    {
        return size.isGreaterThanOrEqualTo(maximumElementsPerLeaf);
    }

    @KivaKitIncludeProperty
    public Maximum maximumChildrenPerInteriorNode()
    {
        return maximumChildrenPerInteriorNode;
    }

    @KivaKitIncludeProperty
    public Maximum maximumChildrenPerLeaf()
    {
        return maximumElementsPerLeaf;
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    public RTreeSettings withEstimatedChildrenPerInteriorNode(Estimate estimate)
    {
        var copy = new RTreeSettings(this);
        copy.estimatedChildrenPerLeaf = estimate;
        return copy;
    }

    public RTreeSettings withEstimatedChildrenPerLeaf(Estimate estimate)
    {
        var copy = new RTreeSettings(this);
        copy.estimatedChildrenPerLeaf = estimate;
        return copy;
    }

    public RTreeSettings withEstimatedNodes(Estimate estimate)
    {
        var copy = new RTreeSettings(this);
        copy.estimatedNodes = estimate;
        return copy;
    }

    public RTreeSettings withMaximumChildrenPerInteriorNode(Maximum maximum)
    {
        var copy = new RTreeSettings(this);
        copy.maximumChildrenPerInteriorNode = maximum;
        return copy;
    }

    public RTreeSettings withMaximumChildrenPerLeaf(Maximum maximum)
    {
        var copy = new RTreeSettings(this);
        copy.maximumElementsPerLeaf = maximum;
        return copy;
    }
}
