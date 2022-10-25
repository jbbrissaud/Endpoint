ThisBuild / organization := "com.jbbrissaud"
ThisBuild / name         := "zioUI5Counters"
ThisBuild / scalaVersion := "3.2.0"
ThisBuild / version      := "0.1.0"

val zioVersion        = "2.0.2"
val zioHttpVersion    = "2.0.0-RC5"
val zioJsonVersion    = "0.3.0-RC5"
val zioProcessVersion = "0.7.0-RC5"  // remove this if you end up not using it
val laminarVersion    = "0.14.2"

lazy val backend = project
  .in(file("backend"))
  .settings(
    name    := "backend",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      //"dev.zio"               %% "zio"            % zioVersion,
      "dev.zio"               %% "zio-json"       % zioJsonVersion,
      "dev.zio"               %% "zio-prelude"    % "1.0.0-RC9",
      "io.d11"                %% "zhttp"          % zioHttpVersion,
      "com.github.jwt-scala"  %% "jwt-core"       % "9.0.5",
      "io.d11"                %% "zhttp-test"     % zioHttpVersion % Test
    )
  )
  .dependsOn(shared.jvm)


lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(EsbuildPlugin)  // for UI5 components
  .settings(
    name                            := "frontend",
    version                         := "0.1.0-SNAPSHOT",
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
    },
    scalaJSLinkerConfig ~= {
      _.withSourceMap(true)
    },
    scalaJSUseMainModuleInitializer := true,
    resolvers += "jitpack" at "https://jitpack.io",  // for UI5 components
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time"           % "2.3.0",
      "io.github.cquiroz" %%% "scala-java-time-tzdb"      % "2.3.0",
      "org.scala-js"      %%% "scalajs-java-securerandom" % "1.0.0" cross CrossVersion.for3Use2_13,
      //zio
      "dev.zio"           %%% "zio"                       % zioVersion,
      "dev.zio"           %%% "zio-json"                  % zioJsonVersion,
      "dev.zio"           %%% "zio-prelude"               % "1.0.0-RC9",
      //laminar
      "com.raquo"         %%% "laminar"                   % laminarVersion,
      "io.laminext"       %%% "fetch"                     % "0.14.3",
      //laminar SAP UI5 Components
      "com.github.sherpal" % "LaminarSAPUI5Bindings" % "1.3.0-8f02a832",
      //http client
      //"io.d11"                %%% "zhttp"          % "1.0.0-SNAPSHOT-RC10" cross CrossVersion.for3Use2_13,
      "com.softwaremill.sttp.client3" %%% "core" % "3.8.2",
    ),
    // for ui5 components
    Compile / npmDependencies ++= Seq(
      "@ui5/webcomponents" -> "^1.3.0",
      "@ui5/webcomponents-fiori" -> "^1.3.0",
      "@ui5/webcomponents-icons" -> "^1.3.0",
      "highlight.js" -> "^11.6.0"
    ),
    esPackageManager := Npm
  )
  .dependsOn(shared.js)
  //.aggregate(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio-json" % zioJsonVersion,
    )
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
    )
  )
  .jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Seq(
    )
  )
