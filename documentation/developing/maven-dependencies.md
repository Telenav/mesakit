# Aonia - Maven Dependencies &nbsp; ![](../images/dependencies-40.png)

![](../images/horizontal-line.png)

The *README.md* markdown file for each Aonia project includes the Maven dependency, so it can  
be easily cut and pasted into the *pom.xml* file of a project using Aonia.

### Maven Repositories

To add Aonia modules as dependencies in a project, this repository reference must be added to *pom.xml*:

    <repositories>
        <repository>
          <id>github-telenav</id>
          <url>https://maven.pkg.github.com/Telenav/*</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
    </repositories>

### Maven Dependencies

Maven dependencies on Aonia modules have the following general format:

- The group identifier is *com.telenav.aonia* in all cases
- Artifact identifiers start with *aonia* and are separated by dashes

For example, this is the dependency for *aonia-map-measurements*:

    <dependency>
        <groupId>com.telenav.aonia</groupId>
        <artifactId>aonia-map-measurements</artifactId>
        <version>${aonia.version}</version>
    </dependency>

### Module Names

Module names use the fully qualified group and artifact ids with dots substituted for any hyphens.  
For example, this *module-info.java* statement imports the *aonia-map-measurements* project:

    requires com.telenav.aonia.map.measurements;

<br/> 

![](../images/horizontal-line.png)
