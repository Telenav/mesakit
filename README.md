# aonia &nbsp;&nbsp;![](documentation/images/aonia-64.png)

Aonia is a modular toolkit for developing map and navigation software.

![](documentation/images/horizontal-line.png)

[//]: # (start-user-text)

### Index <a name = "index"></a>

[**Welcome!**](#welcome)  
[**Summary**](#summary)  
[**Project Resources**](#project-resources)  
[**Quick Start**](#quick-start)  
[**Downloads**](#downloads)  
[**Reference**](#reference)  
[**Projects**](#projects)  
[**Javadoc Coverage**](#javadoc-coverage)

![](documentation/images/short-horizontal-line.png)

### Welcome <a name = "welcome"></a>! &nbsp; ![](documentation/images/stars-32.png)

> *The mission of Aonia is to accelerate the development of mapping and navigation software*

![](documentation/images/horizontal-line.png)

### Summary <a name = "summary"></a>

Aonia provides useful APIs and tools for complex back-end map data analysis problems.  
It may also appropriate for serving map data in some specialized production applications.

#### What is it?

Aonia is a way to rapidly develop mapping and navigation software.

#### Why use it?

Aonia simplifies access to map data by providing an object-oriented API.  
This API makes it easy to perform complex data analysis operations with minimal code.

#### What can it do?

Aonia provides support for:

- [OpenStreetMap (OSM)](https://www.openstreetmap.org/) data processing
    - OSM [Protobuf (PBF)](https://wiki.openstreetmap.org/wiki/PBF_Format) format
    - Road name parsing and standardization
    - [Overpass](https://wiki.openstreetmap.org/wiki/Overpass_API) map data retrieval
- Directional road network graphs
    - Building graphs from raw data sources
    - Flexible data specifications
        - Metadata schema
        - Custom data sources
        - Flexible attributes
    - Spatial indexing
    - Graph queries
    - Composite graphs
    - Graph analysis tools
    - Administrative regions
    - Reverse Geocoding
    - [Java OpenStreetMap (JOSM)](https://josm.openstreetmap.de) plugin
    - [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra's_algorithm) and bi-Dijkstra heuristic routing
- Traffic
    - [TMC](https://en.wikipedia.org/wiki/Traffic_message_channel) codes
    - Historical data processing
    - Tomtom and Navteq codes

![](documentation/images/short-horizontal-line.png)

### Project Resources <a name = "project-resources"></a> &nbsp; ![](documentation/images/water-32.png)

| Resource     |     Description                   |
|--------------|-----------------------------------|
| Project Name | Aonia |
| Summary | A toolkit for developing mapping and navigation software |
| Javadoc Coverage |  <!-- ${project-javadoc-average-coverage-meter} -->  ![](documentation/images/meter-40-12.png) <!-- end --> |
| Lead | Jonathan Locke (Luo, Shibo) <br/> [jonathanl@telenav.com](mailto:jonathanl@telenav.com) |
| Administrator | Jonathan Locke (Luo, Shibo) <br/> [jonathanl@telenav.com](mailto:jonathanl@telenav.com) |
| Email | [jonathanl@telenav.com](mailto:jonathanl@telenav.com) |
| Twitter | [@OpenAonia](https://twitter.com/openaonia) |
| Issues | [GitHub Issues](https://github.com/Telenav/aonia/issues) |
| Code | [GitHub](https://github.com/Telenav/aonia) |
| Checkout | `git clone git@github.com:Telenav/aonia.git` |

![](documentation/images/short-horizontal-line.png)

### Quick Start <a name = "quick-start"></a>&nbsp; ![](documentation/images/rocket-40.png)

[**Setup**](documentation/overview/setup.md)  
[**Building**](documentation/overview/building.md)  
[**Developing**](documentation/developing/index.md)

![](documentation/images/short-horizontal-line.png)

### Reference <a name = "reference"></a>&nbsp; ![](documentation/images/books-40.png)

[**Javadoc**](https://telenav.github.io/aonia/javadoc)  
[**CodeFlowers**](https://telenav.github.io/aonia/codeflowers/site/index.html)  
[**System Properties**](documentation/developing/system-properties.md)

![](documentation/images/short-horizontal-line.png)

[//]: # (end-user-text)

### Projects <a name = "projects"></a> &nbsp; ![](documentation/images/gears-40.png)

[**aonia-map**](aonia-map/README.md)  

![](documentation/images/short-horizontal-line.png)

### Javadoc Coverage <a name = "javadoc-coverage"></a> &nbsp; ![](documentation/images/bargraph-32.png)

&nbsp;  ![](documentation/images/meter-30-12.png) &nbsp; &nbsp; [**aonia-map-cutter**](cutter/README.md)  
&nbsp;  ![](documentation/images/meter-40-12.png) &nbsp; &nbsp; [**aonia-map-data-formats-pbf**](pbf/README.md)  
&nbsp;  ![](documentation/images/meter-50-12.png) &nbsp; &nbsp; [**aonia-map-data-library**](library/README.md)  
&nbsp;  ![](documentation/images/meter-40-12.png) &nbsp; &nbsp; [**aonia-map-geography**](geography/README.md)  
&nbsp;  ![](documentation/images/meter-60-12.png) &nbsp; &nbsp; [**aonia-map-measurements**](measurements/README.md)  
&nbsp;  ![](documentation/images/meter-10-12.png) &nbsp; &nbsp; [**aonia-map-overpass**](overpass/README.md)  
&nbsp;  ![](documentation/images/meter-40-12.png) &nbsp; &nbsp; [**aonia-map-region**](region/README.md)  
&nbsp;  ![](documentation/images/meter-40-12.png) &nbsp; &nbsp; [**aonia-map-road-model**](model/README.md)  
&nbsp;  ![](documentation/images/meter-50-12.png) &nbsp; &nbsp; [**aonia-map-road-name-parser**](name-parser/README.md)  
&nbsp;  ![](documentation/images/meter-30-12.png) &nbsp; &nbsp; [**aonia-map-road-name-standardizer**](name-standardizer/README.md)  
&nbsp;  ![](documentation/images/meter-40-12.png) &nbsp; &nbsp; [**aonia-map-ui**](ui/README.md)  
&nbsp;  ![](documentation/images/meter-40-12.png) &nbsp; &nbsp; [**aonia-map-utilities-geohash**](geohash/README.md)  
&nbsp;  ![](documentation/images/meter-30-12.png) &nbsp; &nbsp; [**aonia-map-utilities-geojson**](geojson/README.md)  
&nbsp;  ![](documentation/images/meter-50-12.png) &nbsp; &nbsp; [**aonia-map-utilities-grid**](grid/README.md)

[//]: # (start-user-text)

![](documentation/images/horizontal-line.png)

[**Issues**](https://github.com/Telenav/aonia/issues) |
[**Change Log**](change-log.md) |
[**Java Migration Notes**](documentation/overview/java-migration-notes.md)

[//]: # (end-user-text)

![](documentation/images/horizontal-line.png)

<sub>Copyright &#169; 2011-2021 [Telenav](http://telenav.com), Inc. Distributed under [Apache License, Version 2.0](LICENSE)</sub>  
<sub>This documentation was generated by [Lexakai](https://github.com/Telenav/lexakai) on 2021.04.15. UML diagrams courtesy
of [PlantUML](http://plantuml.com).</sub>
