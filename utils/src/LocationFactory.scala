import lxx.ts_log.attributes.{AttributesManager, Attribute}
import lxx.ts_log.TurnSnapshot
import util.Random

/**
 * User: Aleksey Zhidkov
 * Date: 15.06.12
 */
class LocationFactory(attrs: Array[Attribute], wghts: Array[Double]) {

  def attributes = attrs

  private def weights = wghts

  def getPlainLocation(ts: TurnSnapshot):Array[Double] =
    lxx.data_analysis.LocationFactory.getPlainLocation(ts, attrs)

  def getNormalLocation(ts: TurnSnapshot):Array[Double] =
    lxx.data_analysis.LocationFactory.getNormalLocation(ts, attrs)

  def getWeightedLocation(ts: TurnSnapshot):Array[Double] =
      lxx.data_analysis.LocationFactory.getNormalLocation(ts, attrs).zipWithIndex.map(multiplyByWeight)

  private def multiplyByWeight(attrValue:(Double, Int)): Double = attrValue._1 * weights(attrValue._2)

  override def toString = attrs.map((a:Attribute) => a.name).zip(weights).map((a:(String, Double)) => minimize(a._1) + "=" + a._2.formatted("%2.3f")).mkString(", ")

  private def minimize(s: String): String = {
    s.split(" ").map((s: String) => s.charAt(0)).toIterable.mkString
  }
}

object LocationFactory {

  def randomFactory: LocationFactory = {
    val prob = Random.nextDouble()

    val attrs = AttributesManager.attributes.
      filter((a:Attribute) => a.name.indexOf("My") == -1).
      filter(Attribute => Random.nextDouble() < prob)

    new LocationFactory(attrs, attrs.map(Attribute => Random.nextDouble()))
  }

  def randomizeFactory(original: LocationFactory): LocationFactory = {
    new LocationFactory(original.attributes, original.attributes.zipWithIndex.map(
      (a:(Attribute, Int)) => 1 / original.weights(a._2) * Random.nextDouble()
    ))
  }

  def addRandomAttr(original: LocationFactory): LocationFactory = {
    val newAttrs = Array[Attribute]()
    original.attributes.copyToArray(newAttrs)

    val newWeights = Array[Double]()
    original.weights.copyToArray(newWeights)

    val candidates: Array[Attribute] = AttributesManager.attributes.filter(((a: Attribute) => !newAttrs.contains(a)))
    newAttrs :+ candidates((candidates.length * Random.nextDouble()).toInt)

    newWeights :+ Random.nextDouble()

    new LocationFactory(newAttrs, newWeights)
  }

  def removeRandomAttr(original: LocationFactory): LocationFactory = {
    val idx = (original.attributes.length * Random.nextDouble()).toInt

    val newAttrs = original.attributes.slice(0 ,idx) ++ original.attributes.slice(idx + 1, original.attributes.length)
    val newWeights = original.weights.slice(0 ,idx) ++ original.weights.slice(idx + 1, original.attributes.length)

    new LocationFactory(newAttrs, newWeights)
  }

  //Es=0,037, Ea=0,913, Efwd=0,124, Elvg2=0,428
  // Es=0,655, Ea=0,491, Efwd=0,430, Fbft=0,796, Etsldc=0,031

}
