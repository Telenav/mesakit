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

<p></p>

<img src="https://telenav.github.io/telenav-assets/images/backgrounds/kivakit-background.png" srcset="https://telenav.github.io/telenav-assets/images/backgrounds/kivakit-background-2x.png 2x"/>

[//]: # (end-user-text)

# mesakit &nbsp;&nbsp; <img src="https://telenav.github.io/telenav-assets/images/logos/mesakit/mesakit-64.png" srcset="https://telenav.github.io/telenav-assets/images/logos/mesakit/mesakit-64-2x.png 2x"/>

MesaKit is a modular toolkit for developing map and navigation software.

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

[//]: # (start-user-text)

### Welcome <a name = "welcome"></a>! &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/stars-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/stars-32-2x.png 2x"/>

> *The mission of MesaKit is to simplify the development of mapping and navigation code, and to accelerate development*

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

### Summary <a name = "summary"></a>

MesaKit provides useful APIs and tools for complex back-end map data analysis and processing problems. It may also be appropriate for serving map data in some specialized production applications. MesaKit provides a simple, object-oriented model for working with map data as a directed road network graph. It is designed to be customizable and provides built-in search functionality.

### Quick Start <a name = "quick-start"></a>&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/rocket-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/rocket-32-2x.png 2x"/>

[**About MesaKit**](#about)  
[**Example Code**](https://github.com/Telenav/mesakit-examples)  
[**Published Releases**](https://repo1.maven.org/maven2/com/telenav/mesakit/)  
[**How to Build This Project**](https://github.com/Telenav/telenav-build/blob/release/1.6.2/documentation/building.md) <!-- [cactus.replacement-branch-name] --> 

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Build Status <a name = "quick-start"></a>&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/gears-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/gears-32-2x.png 2x"/>

| Repository                                                                  | Develop                                                                                                  | Release                                                                                                  |
|-----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| [mesakit](https://github.com/Telenav/mesakit/actions)                       | <img src="https://github.com/Telenav/mesakit/actions/workflows/build-develop.yml/badge.svg"/>            | <img src="https://github.com/Telenav/mesakit/actions/workflows/build-release.yml/badge.svg"/>            |
| [mesakit-extensions](https://github.com/Telenav/mesakit-extensions/actions) | <img src="https://github.com/Telenav/mesakit-extensions/actions/workflows/build-develop.yml/badge.svg"/> | <img src="https://github.com/Telenav/mesakit-extensions/actions/workflows/build-release.yml/badge.svg"/> |
| [mesakit-examples](https://github.com/Telenav/mesakit-examples/actions)     | <img src="https://github.com/Telenav/mesakit-examples/actions/workflows/build-develop.yml/badge.svg"/>   | <img src="https://github.com/Telenav/mesakit-examples/actions/workflows/build-release.yml/badge.svg"/>   |

<br/>

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Reference <a name = "reference"></a>&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/books-24.png" srcset="https://telenav.github.io/telenav-assets/images/icons/books-24-2x.png 2x"/>

[**Setup and Build**](#setup-and-build)  
[**Development**](#development)  
[**Javadoc**](https://telenav.github.io/mesakit/1.6.1/javadoc)  <!-- [cactus.replacement-version] -->   
[**CodeFlowers**](https://www.mesakit.org/1.6.1/codeflowers/site/index.html)  <!-- [cactus.replacement-version] -->   
[**System Properties**](documentation/markdown/system-properties.md)

<a name = "about"></a>
<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

### About MesaKit <a name = "about-mesakit"></a>

MesaKit provides support for:

- *Map Data Sources*
    - [OpenStreetMap (OSM)](https://www.openstreetmap.org/) [Protobuf (PBF)](https://wiki.openstreetmap.org/wiki/PBF_Format) format
    - [Overpass](https://wiki.openstreetmap.org/wiki/Overpass_API) map data retrieval
    - Custom data sources
- *Road Network Graphs*
    - Simple, memory-efficient, object-oriented API
    - Directional edges
    - Graph metadata
    - Flexible data specifications
    - Flexible attributes
    - Composite graphs
    - Road name standardization
- *Search*
    - Spatial indexing (quad-tree, r-tree, polygon indexing, geohashing)
    - Graph query language
    - Reverse Geocoding
    - Administrative regions
- *Tools*
    - [Java OpenStreetMap (JOSM)](https://josm.openstreetmap.de) graph viewer plugin
    - [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra's_algorithm) and bi-Dijkstra heuristic routing
    - Graph analysis tools
- *Traffic*
    - [TMC](https://en.wikipedia.org/wiki/Traffic_message_channel) codes
    - Tomtom and Navteq codes
    - Historical traffic data processing

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Setup and Build <a name = "setup-and-build"></a> &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/box-24.png" srcset="https://telenav.github.io/telenav-assets/images/icons/box-24-2x.png 2x"/>

[**Initial Setup**](https://github.com/Telenav/telenav-build/blob/release/1.6.2/documentation/initial-setup-instructions.md)  <!-- [cactus.replacement-branch-name] -->  
[**Building**](https://github.com/Telenav/telenav-build/blob/release/1.6.2/documentation/building.md)  <!-- [cactus.replacement-branch-name] -->  

### Development <a name = "development"></a> &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/gears-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/gears-32-2x.png 2x"/>

[**Maven Dependencies**](maven-dependencies.md)  
[**Developing**](https://github.com/Telenav/telenav-build/blob/release/1.6.2/documentation/developing.md) <!-- [cactus.replacement-branch-name] -->  
[**Releasing**](https://github.com/Telenav/telenav-build/blob/release/1.6.2/documentation/releasing.md) <!-- [cactus.replacement-branch-name] -->

### Downloads <a name = "downloads"></a>&nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/down-arrow-24.png" srcset="https://telenav.github.io/telenav-assets/images/icons/down-arrow-24-2x.png 2x"/>

[**Java 17**](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)  
[**Maven**](https://maven.apache.org/download.cgi)  
[**IntelliJ**](https://www.jetbrains.com/idea/download/)

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Project Resources <a name = "project-resources"></a> &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/water-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/water-32-2x.png 2x"/>

| Resource         | Description                                                                                                                                                                                                                                                                                                                                    |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Project Name     | MesaKit                                                                                                                                                                                                                                                                                                                                        |
| Summary          | A modular toolkit for developing mapping and navigation software                                                                                                                                                                                                                                                                               |
| Javadoc Coverage | <!-- <img src="https://telenav.github.io/telenav-assets/images/meter-50-96.png" srcset="https://telenav.github.io/telenav-assets/images/meter-50-96-2x.png 2x"/>  --> <img src="https://telenav.github.io/telenav-assets/images/meter-50-96.png" srcset="https://telenav.github.io/telenav-assets/images/meter-50-96-2x.png 2x"/> <!-- end --> |
| Lead             | [Jonathan Locke (Luo, Shibo)](mailto:jonathanl@telenav.com)                                                                                                                                                                                                                                                                                    |
| Administrator    | [Jonathan Locke (Luo, Shibo)](mailto:jonathanl@telenav.com)                                                                                                                                                                                                                                                                                    |
| Twitter          | [@OpenMesaKit](https://twitter.com/openmesakit)                                                                                                                                                                                                                                                                                                |
| Issues           | [GitHub Issues](https://github.com/Telenav/mesakit/issues)                                                                                                                                                                                                                                                                                     |
| Code             | [GitHub](https://github.com/Telenav/mesakit)                                                                                                                                                                                                                                                                                                   |
| Checkout         | `git clone https://github.com/Telenav/mesakit.git`                                                                                                                                                                                                                                                                                             |

[//]: # (end-user-text)

### Sub-Projects <a name = "projects"></a> &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/diagram-32.png" srcset="https://telenav.github.io/telenav-assets/images/icons/diagram-32-2x.png 2x"/>

[**mesakit-core**](mesakit-core/README.md)  

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-128-2x.png 2x"/>

### Javadoc Coverage <a name = "javadoc-coverage"></a> &nbsp; <img src="https://telenav.github.io/telenav-assets/images/icons/bargraph-24.png" srcset="https://telenav.github.io/telenav-assets/images/icons/bargraph-24-2x.png 2x"/>

&nbsp; <img src="https://telenav.github.io/telenav-assets/images/meters/meter-100-96.png" srcset="https://telenav.github.io/telenav-assets/images/meters/meter-100-96-2x.png 2x"/>
 &nbsp; &nbsp; [**mesakit-core**](mesakit-core/README.md)

[//]: # (start-user-text)

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

[**Issues**](https://github.com/Telenav/mesakit/issues) |
[**Change Log**](change-log.md) |
[**Java Migration Notes**](documentation/markdown/java-migration-notes.md)

[//]: # (end-user-text)

<img src="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512.png" srcset="https://telenav.github.io/telenav-assets/images/separators/horizontal-line-512-2x.png 2x"/>

<sub>Copyright &#169; 2011-2021 [Telenav](https://telenav.com), Inc. Distributed under [Apache License, Version 2.0](LICENSE)</sub>  
<sub>This documentation was generated by [Lexakai](https://www.lexakai.org). UML diagrams courtesy of [PlantUML](https://plantuml.com).</sub>
