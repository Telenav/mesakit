[//]: # (start-user-text)

<a href="https://www.mesakit.org">
<img src="https://www.kivakit.org/images/web-32.png" srcset="https://www.kivakit.org/images/web-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://twitter.com/openmesakit">
<img src="https://www.kivakit.org/images/twitter-32.png" srcset="https://www.kivakit.org/images/twitter-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://mesakit.zulipchat.com">
<img src="https://www.kivakit.org/images/zulip-32.png" srcset="https://www.kivakit.org/images/zulip-32-2x.png 2x"/>
</a>

[//]: # (end-user-text)

# mesakit-map-cutter &nbsp;&nbsp; <img src="https://www.mesakit.org/images/gears-32.png" srcset="https://www.mesakit.org/images/gears-32-2x.png 2x"/>

This module contains code for cutting maps.

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"/>

### Index

[**Summary**](#summary)  

[**Dependencies**](#dependencies) | [**Class Diagrams**](#class-diagrams) | [**Package Diagrams**](#package-diagrams) | [**Javadoc**](#javadoc)

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"/>

### Dependencies <a name="dependencies"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/dependencies-32.png" srcset="https://www.kivakit.org/images/dependencies-32-2x.png 2x"/>

[*Dependency Diagram*](https://www.mesakit.org/0.9.11/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/dependencies.svg)

#### Maven Dependency

    <dependency>
        <groupId>com.telenav.mesakit</groupId>
        <artifactId>mesakit-map-cutter</artifactId>
        <version>0.9.11</version>
    </dependency>

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

[//]: # (start-user-text)

### Summary <a name = "summary"></a>

This module cuts map data in OSM PBF format into pieces, forming multiple output files. Supports "hard cutting"
where ways that cross the cutting boundary are clipped, and "soft cutting" where ways that cross the boundary 
are left uncut.

[//]: # (end-user-text)

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Class Diagrams <a name="class-diagrams"></a> &nbsp; &nbsp; <img src="https://www.kivakit.org/images/diagram-40.png" srcset="https://www.kivakit.org/images/diagram-40-2x.png 2x"/>

[*Map Cutting*](https://www.mesakit.org/0.9.11/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/diagram-map-cutter.svg)

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Package Diagrams <a name="package-diagrams"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"/>

[*com.telenav.mesakit.map.cutter*](https://www.mesakit.org/0.9.11/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.svg)  
[*com.telenav.mesakit.map.cutter.cuts*](https://www.mesakit.org/0.9.11/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.cuts.svg)  
[*com.telenav.mesakit.map.cutter.cuts.maps*](https://www.mesakit.org/0.9.11/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.cuts.maps.svg)  
[*com.telenav.mesakit.map.cutter.lexakai*](https://www.mesakit.org/0.9.11/lexakai/mesakit/mesakit-map/cutter/documentation/diagrams/com.telenav.mesakit.map.cutter.lexakai.svg)

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Javadoc <a name="javadoc"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/books-32.png" srcset="https://www.kivakit.org/images/books-32-2x.png 2x"/>

Javadoc coverage for this project is 36.3%.  
  
&nbsp; &nbsp; <img src="https://www.mesakit.org/images/meter-40-96.png" srcset="https://www.mesakit.org/images/meter-40-96-2x.png 2x"/>


The following significant classes are undocumented:  

- Cut  
- PbfRegionCutter  
- RegionNodes  
- RegionWays  
- SoftCut

| Class | Documentation Sections |
|---|---|
| [*Cut*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/Cut.html) |  |  
| [*DiagramMapCutter*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/lexakai/DiagramMapCutter.html) |  |  
| [*FastCut*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/cuts/FastCut.html) |  |  
| [*PbfRegionCutter*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/PbfRegionCutter.html) |  |  
| [*RegionIndexMap*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/cuts/maps/RegionIndexMap.html) |  |  
| [*RegionNodes*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/cuts/maps/RegionNodes.html) |  |  
| [*RegionWays*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/cuts/maps/RegionWays.html) |  |  
| [*SoftCut*](https://www.mesakit.org/0.9.11/javadoc/mesakit/mesakit.map.cutter/com/telenav/mesakit/map/cutter/cuts/SoftCut.html) |  |  

[//]: # (start-user-text)



[//]: # (end-user-text)

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"/>

<sub>Copyright &#169; 2011-2021 [Telenav](https://telenav.com), Inc. Distributed under [Apache License, Version 2.0](LICENSE)</sub>  
<sub>This documentation was generated by [Lexakai](https://lexakai.org). UML diagrams courtesy of [PlantUML](https://plantuml.com).</sub>

