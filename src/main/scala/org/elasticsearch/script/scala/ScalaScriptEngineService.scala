package com.busk.elasticsearch.scala

import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.Scorer
import org.elasticsearch.common.Nullable
import org.elasticsearch.common.component.AbstractComponent
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.script.ExecutableScript
import org.elasticsearch.script.ScriptEngineService
import org.elasticsearch.script.{ AbstractDoubleSearchScript, AbstractFloatSearchScript, AbstractSearchScript, NativeScriptEngineService }
import org.elasticsearch.script.{ SearchScript, NativeScriptFactory }

import org.elasticsearch.search.lookup.DocLookup;
import org.elasticsearch.search.lookup.FieldsLookup;
import org.elasticsearch.search.lookup.SearchLookup;
import org.elasticsearch.search.lookup.SourceLookup;
import org.elasticsearch.index.field.data.{ NumericDocFieldData, DocFieldData}

import com.twitter.util.Eval

import java.util.Map

// class ScalaExecutableScript(scalaScript: ScalaScript, params: Map[String, AnyRef]) extends ExecutableScript {
//   val contextVars = collection.mutable.HashMap[String, AnyRef]()

//   override def setNextVar(name: String, value: AnyRef) =
//     contextVars += (name -> value)

//   override def run(): AnyRef =
//     scalaScript().asInstanceOf[java.lang.Double]

//   override def unwrap(value: AnyRef): AnyRef = value
// }

abstract class ScalaSearchScript(val params: Map[String, AnyRef]) extends AbstractDoubleSearchScript {

  override def runAsDouble: Double

  // helpers
  protected final def time() = System.currentTimeMillis
  // param helpers
  protected final def floatParam(str: String): Float   = params.get(str).asInstanceOf[Float].floatValue
  protected final def doubleParam(str: String): Double = params.get(str).asInstanceOf[Double].doubleValue
  protected final def longParam(str: String): Long     = params.get(str).asInstanceOf[Long].longValue

  // doc field helprs
  protected final def field(str: String) = doc.field(str).asInstanceOf[DocFieldData[_]]
  protected final def numericField(str: String): NumericDocFieldData[_] = {
    val numberData: NumericDocFieldData[_] = doc.numeric(str)
    numberData
  }
  protected final def doubleField(str: String) = numericField(str).getDoubleValue
  protected final def floatField(str: String)  = numericField(str).getFloatValue
  protected final def longField(str: String)   = numericField(str).getLongValue

  // aliases
  protected final def _score = score
  protected final def _source = source()
  protected final def _fields = fields()
}

class ScalaNativeScriptEngineService @Inject() (settings: Settings) extends NativeScriptEngineService(settings, java.util.Collections.emptyMap()) {
  override def types() = Array("scala")
  override def extensions() = Array("scala")
  override def compile(script: String): Object = {
    val eval = new Eval
    val scalaScriptFactory: NativeScriptFactory = eval(getScalaScriptCode(script))
    scalaScriptFactory
  }

  private def getScalaScriptCode(code: String): String = {
    """
      import org.elasticsearch.search.lookup.DocLookup;
      import org.elasticsearch.search.lookup.FieldsLookup
      import org.elasticsearch.search.lookup.SourceLookup
      import java.util.Map // so scala compiler does not confuse with scala Map
      import org.elasticsearch.script.{ ExecutableScript, NativeScriptFactory }
      import com.busk.elasticsearch.scala.ScalaSearchScript
      import org.elasticsearch.index.field.data.{ NumericDocFieldData, DocFieldData }
      import math._

      new NativeScriptFactory {
        override def newScript(params: Map[String, Object]): ExecutableScript = {
          new ScalaSearchScript(params) {
            override def runAsDouble: Double = {
              """+code+"""
            }
          }
        }
      }
    """
  }

  override def close() {}
  override def unwrap(value: Object): Object = value
}