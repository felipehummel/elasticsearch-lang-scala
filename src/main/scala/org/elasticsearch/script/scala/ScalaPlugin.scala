package org.elasticsearch.script.scala

import org.elasticsearch.plugins.AbstractPlugin
import org.elasticsearch.script.ScriptModule

class ScalaPlugin extends AbstractPlugin {
  override def name() = "lang-scala"

  override def description() = "Scala plugin allowing to add scala scripting";

  def onModule(module: ScriptModule) =
    module.addScriptEngine(classOf[org.elasticsearch.script.ScalaNativeScriptEngineService]);
}