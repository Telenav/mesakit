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

package com.telenav.mesakit.map.geography.shape.polyline;

import com.telenav.kivakit.serialization.kryo.BaseSerializer;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSession;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

public class PolygonKryoSerializer extends BaseSerializer<Polygon>
{
    public PolygonKryoSerializer()
    {
        super(Polygon.class);
    }

    @Override
    protected Polygon onRead(KryoSerializationSession session)
    {
        return session.read(Polygon.class);
    }

    @Override
    protected void onWrite(KryoSerializationSession session, Polygon polygon)
    {
        ensure(polygon != null);
        session.write(polygon);
    }
}
