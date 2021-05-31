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

package com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes;

import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;

public class AdasRegionCode implements Quantizable
{
    public static final long NULL = 0;

    private final int code;

    public AdasRegionCode(final int code)
    {
        this.code = code;
    }

    public int code()
    {
        return code;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof AdasRegionCode)
        {
            final var that = (AdasRegionCode) object;
            return code == that.code;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(code);
    }

    @Override
    public long quantum()
    {
        return code;
    }

    @Override
    public String toString()
    {
        return Integer.toString(code);
    }
}
