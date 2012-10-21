package org.elasticsearch.script

import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.Scorer
import org.elasticsearch.common.Nullable
import org.elasticsearch.common.component.AbstractComponent
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.settings.Settings
// import org.elasticsearch.script.ExecutableScript
// import org.elasticsearch.script.ScriptEngineService
// import org.elasticsearch.script.{ AbstractDoubleSearchScript, AbstractFloatSearchScript, AbstractSearchScript }
// import org.elasticsearch.script.SearchScript

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

object ScalaScriptImplicitHelpers {
  class RichDocFieldData(docFieldData: DocFieldData[_]) {
    def double = docFieldData.asInstanceOf[NumericDocFieldData[_]].getDoubleValue
    def float  = docFieldData.asInstanceOf[NumericDocFieldData[_]].getFloatValue
    def long   = docFieldData.asInstanceOf[NumericDocFieldData[_]].getLongValue
  }
  implicit def docDataToRich(docFieldData: DocFieldData[_]): RichDocFieldData =
    new RichDocFieldData(docFieldData)
}

class ExtendedDocLookup(val docLookup: DocLookup) {
  import ScalaScriptImplicitHelpers._

  def apply(str: String): DocFieldData[_] = docLookup.get(str).asInstanceOf[DocFieldData[_]]
  def numeric(str: String): NumericDocFieldData[_] = {
    val numberData: NumericDocFieldData[_] = docLookup.numeric(str)
    numberData
  }
  def doubleField(str: String) = numeric(str).getDoubleValue
  def floatField(str: String)  = numeric(str).getFloatValue
  def longField(str: String)   = numeric(str).getLongValue
  def field(str: String) = docLookup.field(str).asInstanceOf[DocFieldData[_]]
}

abstract class ScalaSearchScript(params: Map[String, AnyRef]) extends AbstractFloatSearchScript {
  override def run: AnyRef =
    runAsFloat(): java.lang.Float //type ascripting to java Float, so scala can convert to AnyRef
  override def runAsFloat:  Float // = scalaScript(params, this.doc(), this.source(), this.fields(), this.score)
  override def runAsDouble: Double = run().asInstanceOf[Double]
  override def runAsLong:   Long   = run().asInstanceOf[Long]

  // helpers
  def _doc = new ExtendedDocLookup(doc())
  def time = System.currentTimeMillis


  // aliases
  def _score = score
  def _source = source()
  def _fields = fields()
}


class ScalaNativeScriptEngineService @Inject() (settings: Settings) extends NativeScriptEngineService(settings, java.util.Collections.emptyMap()) {
  override def types() = Array("scala")
  override def extensions() = Array("scala")
  override def compile(script: String): AnyRef = {
    val scalaCode = getScalaScriptCode(script)
    val eval = new Eval
    val scalaScriptFactory: NativeScriptFactory = eval(scalaCode)
    scalaScriptFactory
  }

    private def getScalaScriptCode(code: String): String = {
      """
        import org.elasticsearch.script.ScalaScript
        import org.elasticsearch.search.lookup.DocLookup;
        import org.elasticsearch.search.lookup.FieldsLookup;
        import org.elasticsearch.search.lookup.SourceLookup;
        import java.util.Map // so scala compiler does not confuse with scala Map
        import org.elasticsearch.script.ExtendedDocLookup

        new NativeScriptFactory {
          override def newScript(params: Map[String, AnyRef]): ExecutableScript = {
            new ScalaSearchScript(params) {
              override def runAsFloat {
                import math._
                import ScalaScriptImplicitHelpers._
                val _score = score
                val _doc = new ExtendedDocLookup(doc)
                """+code+"""
              }
            }
          }
        }
      """
    }

    override def close() {}
    override def unwrap(value: AnyRef) = value
}