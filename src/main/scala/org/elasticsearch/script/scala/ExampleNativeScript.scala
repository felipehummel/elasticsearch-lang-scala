package org.spacevatican.elasticsearchexample

import org.elasticsearch.common.Nullable
import org.elasticsearch.script.ExecutableScript
import org.elasticsearch.script.NativeScriptFactory
import org.elasticsearch.script.AbstractDoubleSearchScript

import java.util.Map
import java.lang.Math
import org.elasticsearch.index.field.data.{ NumericDocFieldData, DocFieldData}

class CustomScriptFactory extends NativeScriptFactory {
  override def newScript(params: Map[String,Object]): ExecutableScript =
    new CustomScript(params)
}

class CustomScript(params: Map[String,Object]) extends AbstractDoubleSearchScript {
    val base = params.get("base").asInstanceOf[Double].doubleValue()
    val expo  = params.get("expo").asInstanceOf[Double].doubleValue()

    override def runAsDouble(): Double = {
      val number: NumericDocFieldData[_] = doc().numeric("a")
      val a: Double = number.getDoubleValue()

      (base * Math.log(a)) / expo
    }
}