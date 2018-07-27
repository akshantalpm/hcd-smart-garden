
lazy val `hcd-smart-garden-assembly` = project
  .settings(
    libraryDependencies ++= Dependencies.HcdsmartgardenAssembly
  )

lazy val `hcd-smart-garden-hcd` = project
  .settings(
    libraryDependencies ++= Dependencies.HcdsmartgardenHcd
  )

lazy val `hcd-smart-garden-deploy` = project
  .dependsOn(
    `hcd-smart-garden-assembly`,
    `hcd-smart-garden-hcd`
  )
  .enablePlugins(JavaAppPackaging, CswBuildInfo)
  .settings(
    libraryDependencies ++= Dependencies.HcdsmartgardenDeploy,
    mainClass in assembly := Some("org.tw.rpi.hcdsmartgardendeploy.HcdsmartgardenContainerCmdApp"),
    assemblyMergeStrategy in assembly := {
      case "META-INF/io.netty.versions.properties" =>
        MergeStrategy.discard
      case x if Assembly.isConfigFile(x) =>
        MergeStrategy.concat
      case PathList(ps@_*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
        MergeStrategy.rename
      case PathList("META-INF", xs@_*) =>
        xs map {_.toLowerCase} match {
          case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) => MergeStrategy.discard
          case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") => MergeStrategy.discard
          case "plexus" :: xs => MergeStrategy.discard
          case "services" :: xs => MergeStrategy.filterDistinctLines
          case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) => MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.deduplicate
        }
      case _ =>
        MergeStrategy.first
    }

  )
