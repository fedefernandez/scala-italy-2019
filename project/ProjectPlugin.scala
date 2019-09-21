import sbt._
import sbt.Keys._
import higherkindness.mu.rpc.idlgen.IdlGenPlugin.autoImport._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val V = new {
      lazy val mu    = "0.18.4"
      lazy val fs2   = "1.0.4"
      lazy val slf4j = "1.7.28"
    }

    lazy val protocolSettings: Seq[Def.Setting[_]] = Seq(
      sourceGenerators in Compile += (srcGen in Compile).taskValue,
      libraryDependencies += "io.higherkindness" %% "mu-rpc-channel" % V.mu
    )

    lazy val avroProtocolSettings: Seq[Def.Setting[_]] = protocolSettings ++ Seq(
      idlType := "avro",
      srcGenSerializationType := "AvroWithSchema"
    )

    lazy val pbProtocolSettings: Seq[Def.Setting[_]] = protocolSettings ++ Seq(
      idlType := "proto",
      srcGenSerializationType := "Protobuf",
      libraryDependencies += "io.higherkindness" %% "mu-rpc-fs2" % V.mu
    )

    lazy val serverSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.higherkindness" %% "mu-rpc-server" % V.mu,
        "io.higherkindness" %% "mu-rpc-prometheus" % V.mu,
        "org.slf4j" % "slf4j-simple" % V.slf4j
      )
    )

    lazy val clientSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.higherkindness" %% "mu-rpc-channel" % V.mu,
        "io.higherkindness" %% "mu-rpc-netty" % V.mu,
        "io.higherkindness" %% "mu-rpc-prometheus" % V.mu,
        "org.slf4j" % "slf4j-simple" % V.slf4j
      )
    )

    def n(suffix: String) = s"scala-italy-$suffix"
  }

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      name := "scala-italy",
      organization := "47deg",
      organizationName := "47 Degrees",
      scalaVersion := "2.12.8",
      resolvers ++= Seq(
        Resolver.sonatypeRepo("snapshots"),
        Resolver.sonatypeRepo("releases")
      ),
      scalafmtCheck := true,
      scalafmtOnCompile := true,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    )
}
