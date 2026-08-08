# Why `maven.config` sets `eclipseP2RepoId`

`org.apache.uima:uimaj-parent` (3.6.0 through at least 3.7.0-SNAPSHOT) declares a
p2 repository whose id is a property that the same POM no longer defines:

```xml
<repository>
  <id>${eclipseP2RepoId}</id>
  <url>https://download.eclipse.org/releases/2022-09/</url>
  <layout>p2</layout>
</repository>
```

The property was dropped after 3.5.0; the repository block was left behind. We
reach `uimaj-parent` through the `uimaj-bom` import in
`dkpro-core-parent-common/pom.xml`.

Maven 3 tolerated the uninterpolated id. Maven 4 validates repository
definitions and aborts *reactor model building* — before any goal runs — with:

```
Invalid RemoteRepositories: [..., ${eclipseP2RepoId} (https://download.eclipse.org/releases/2022-09/, p2, releases+snapshots)]
Caused by: Not fully interpolated remote repository ${eclipseP2RepoId}
```

That breaks every multi-module command, including `versions:display-*`.

Defining the property in our own `pom.xml` does **not** help: `uimaj-parent` is
an external POM, so its `${eclipseP2RepoId}` is interpolated in its own
model-building context where our project properties are not visible. Only a
*user* property reaches it — hence `.mvn/maven.config` rather than a
`<properties>` entry.

The value only has to be a syntactically valid repository id; the p2 repository
is never used to resolve any dkpro-core artifact.

Remove this once UIMA ships a `uimaj-parent` without the stale repository block.
