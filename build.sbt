import AssemblyKeys._ // put this at the top of the file

assemblySettings

organization := "com.busk"

name := "ScalaScriptsPlugin"

version := "0.1.0-SNAPSHOT"

scalacOptions += "-deprecation"

scalaVersion := "2.9.1"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "0.19.8" % "provided"

resolvers += "Twitter Repository" at "http://maven.twttr.com/"

libraryDependencies += "com.twitter"   % "util-eval_2.9.1"   % "4.0.1"

