name := "context-helper-backend"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += "StORMeD Dev-Kit Repository" at "https://stormed.inf.usi.ch/releases/"
credentials += Credentials("Sonatype Nexus Repository Manager", "stormed.inf.usi.ch", "anonymous", "anonymous")
libraryDependencies += "ch.usi.inf.reveal.parsing" %% "stormed-devkit" % "2.0.0"
libraryDependencies += "org.scalaj"	%%	"scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.3"
