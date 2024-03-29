[//]: # (start-user-text)

<a href="https://www.mesakit.org">
<img src="https://telenav.github.io/telenav-assets/images/icons/web-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/web-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://twitter.com/openmesakit">
<img src="https://telenav.github.io/telenav-assets/images/logos/twitter/twitter-32.png" srcset="https://telenav.github.io/telenav-assets/images/logos/twitter/twitter-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://mesakit.zulipchat.com">
<img src="https://telenav.github.io/telenav-assets/images/logos/zulip/zulip-32.png" srcset="https://telenav.github.io/telenav-assets/images/logos/zulip/zulip-32-2x.png 2x"/>
</a>

[//]: # (end-user-text)

# mesakit-map-cutter &nbsp;&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/gears-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/gears-32-2x.png 2x"/>

This module contains code for cutting maps.

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

### Index

[**Summary**](#summary)  

[**Dependencies**](#dependencies) | [**Code Quality**](#code-quality) | [**Class Diagrams**](#class-diagrams) | [**Package Diagrams**](#package-diagrams)

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

### Dependencies <a name="dependencies"></a> &nbsp;&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/dependencies-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/dependencies-32-2x.png 2x"/>

[*Dependency Diagram*](https://www.mesakit.org/0.17.1/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/dependencies.svg)

#### Maven Dependency

    <dependency>
        <groupId>com.telenav.mesakit</groupId>
        <artifactId>mesakit-map-cutter</artifactId>
        <version>0.17.1</version>
    </dependency>

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

[//]: # (start-user-text)

### Summary <a name = "summary"></a>

This module cuts map data in OSM PBF format into pieces, forming multiple output files. Supports "hard cutting"
where ways that cross the cutting boundary are clipped, and "soft cutting" where ways that cross the boundary 
are left uncut.

[//]: # (end-user-text)

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Code Quality <a name="code-quality"></a> &nbsp;&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/ruler-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/ruler-32-2x.png 2x"/>

Code quality for this project is 0.0%.  
  
&nbsp; &nbsp; <img src="https://telenav.github.io/telenav-assets/images/meters/meter-0-96.png" srcset="https://telenav.github.io/telenav-assets/images/meters/meter-0-96-2x.png 2x"/>

| Measurement   | Value                    |
|---------------|--------------------------|
| Stability     | 0.0%&nbsp; &nbsp; <img src="https://telenav.github.io/telenav-assets/images/meters/meter-0-96.png" srcset="https://telenav.github.io/telenav-assets/images/meters/meter-0-96-2x.png 2x"/>     |
| Testing       | 0.0%&nbsp; &nbsp; <img src="https://telenav.github.io/telenav-assets/images/meters/meter-0-96.png" srcset="https://telenav.github.io/telenav-assets/images/meters/meter-0-96-2x.png 2x"/>       |
| Documentation | 0.0%&nbsp; &nbsp; <img src="https://telenav.github.io/telenav-assets/images/meters/meter-0-96.png" srcset="https://telenav.github.io/telenav-assets/images/meters/meter-0-96-2x.png 2x"/> |

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Class Diagrams <a name="class-diagrams"></a> &nbsp; &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/diagram-40.png" srcset="https://telenav.github.io/telenav-assets/images/icons/diagram-40-2x.png 2x"/>

[*Map Cutting*](https://www.mesakit.org/0.17.1/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/diagram-map-cutter.svg)

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Package Diagrams <a name="package-diagrams"></a> &nbsp;&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/box-24.png" srcset="https://telenav.github.io/telenav-assets/images/icons/box-24-2x.png 2x"/>

[*com.telenav.mesakit.map.cutter*](https://www.mesakit.org/0.17.1/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.svg)  
[*com.telenav.mesakit.map.cutter.cuts*](https://www.mesakit.org/0.17.1/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.cuts.svg)  
[*com.telenav.mesakit.map.cutter.cuts.maps*](https://www.mesakit.org/0.17.1/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.cuts.maps.svg)  
[*com.telenav.mesakit.map.cutter.internal.lexakai*](https://www.mesakit.org/0.17.1/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.internal.lexakai.svg)

### Javadoc <a name="code-quality"></a> &nbsp;&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/books-24.png" srcset="https://telenav.github.io/telenav-assets/images/icons/books-24-2x.png 2x"/>

| Class | Documentation Sections  |
|-------|-------------------------|
| [*Cut*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/Cut.html) |  |  
| [*DiagramMapCutter*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/internal/lexakai/DiagramMapCutter.html) |  |  
| [*FastCut*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/cuts/FastCut.html) |  |  
| [*PbfRegionCutter*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/PbfRegionCutter.html) |  |  
| [*RegionIndexMap*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/cuts/maps/RegionIndexMap.html) |  |  
| [*RegionNodes*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/cuts/maps/RegionNodes.html) |  |  
| [*RegionWays*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/cuts/maps/RegionWays.html) |  |  
| [*SoftCut*](https://www.mesakit.org/0.17.1/javadoc/mesakit/mesakit-map-cutter/com/telenav/mesakit/map/cutter/cuts/SoftCut.html) |  |  

[//]: # (start-user-text)



[//]: # (end-user-text)

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

<sub>Copyright &#169; 2011-2021 [Telenav](https://telenav.com), Inc. Distributed under [Apache License, Version 2.0](LICENSE)</sub>  
<sub>This documentation was generated by [Lexakai](https://lexakai.org). UML diagrams courtesy of [PlantUML](https://plantuml.com).</sub>
