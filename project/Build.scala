import sbt._
import Keys._


object ScalaScriptsPluginBuild extends Build {
  val getJars = TaskKey[Unit]("get-jars")
    val getJarsTask = getJars <<= (target, fullClasspath in Runtime) map { (target, cp) =>
      println("Target path is: "+target)
      println("Full classpath is: "+cp.map(_.data).mkString(":"))
    }
    lazy val project = Project (
      "project",
      file ("."),
      settings = Defaults.defaultSettings ++ Seq(getJarsTask)
    )
}



// vim: set ts=4 sw=4 et:
