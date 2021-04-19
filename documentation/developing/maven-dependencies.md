# MesaKit - Maven Dependencies &nbsp; ![](../images/dependencies-32.png)

![](../images/horizontal-line.png)

The *README.md* markdown file for each MesaKit project includes the Maven dependency, so it can
be easily cut and pasted into the *pom.xml* file of a project using MesaKit.

### Maven Repositories

To add MesaKit modules as dependencies in a project, this repository reference must be added to *pom.xml*:

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

Maven dependencies on MesaKit modules have the following general format:

- The group identifier is *com.telenav.mesakit* in all cases
- Artifact identifiers start with *mesakit* and are separated by dashes

For example, this is the dependency for *mesakit-map-measurements*:

    <dependency>
        <groupId>com.telenav.mesakit</groupId>
        <artifactId>mesakit-map-measurements</artifactId>
        <version>${mesakit.version}</version>
    </dependency>

### Module Names

Module names use the fully qualified group and artifact ids with dots substituted for any hyphens.
For example, this *module-info.java* statement imports the *mesakit-map-measurements* project:

    requires com.telenav.mesakit.map.measurements;

<br/> 

![](../images/horizontal-line.png)
