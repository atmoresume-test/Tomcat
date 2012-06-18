import collection.immutable.Iterable
import collection.JavaConversions._
import collection.mutable
import java.io.{FileInputStream, ObjectInputStream}
import lxx.targeting.tomcat_claws.TomcatClaws
import lxx.ts_log.attributes.{AttributesManager, Attribute}
import lxx.ts_log.TurnSnapshot
import lxx.utils.QuickMath
import scala.util.Random

/**
 * User: jdev
 * Date: 15.06.12
 */

val ois = new ObjectInputStream(new FileInputStream(".\\res\\lxx.Tomcat 3.57d.debug-jk.mega.DrussGT 2.4.8-1339947698320"))

QuickMath.init()

var tsLog = List[TurnSnapshot]()
var wavesLog = Map[TurnSnapshot, Double]()
val snapshotsByRoundTime: mutable.HashMap[Int, TurnSnapshot] = new scala.collection.mutable.HashMap[Int, TurnSnapshot]

try {
  while (true) {
    val obj = ois.readObject()
    println(tsLog.size + ", " + wavesLog.size)
    obj match {
      case ts: TurnSnapshot => {
        tsLog = tsLog :+ ts
        AttributesManager.attributes.foreach((a: Attribute) => ts.getAttrValue(a))
        snapshotsByRoundTime.put(ts.roundTime, ts)
        val prev: Option[TurnSnapshot] = snapshotsByRoundTime.get(ts.roundTime - 1)
        if (!prev.isEmpty) {
          prev.get.next = ts
        }
      }

      case wave: java.util.HashMap[TurnSnapshot, Double] =>
        wavesLog = wavesLog + wave.toList.head

      case _ =>
      // ignore
    }
  }
} catch {
  case e: Exception =>
  // ignore
}

println("Data parsed")

var minDiff: Double = Integer.MAX_VALUE
var bestLocFactory: LocationFactory = null

var i = 0
while (true) {
  try {
    val locFactory =
      if (i == 0) {
        LocationFactory.randomFactory
      } else {
        (Random.nextDouble() * 4).toInt match {
          case 0 => LocationFactory.randomFactory
          case 1 => LocationFactory.randomizeFactory(bestLocFactory)
          case 2 => LocationFactory.addRandomAttr(bestLocFactory)
          case 3 => LocationFactory.removeRandomAttr(bestLocFactory)
        }
      }

    val mockDVM = new MockDataViewManager(locFactory.attributes)

    tsLog.foreach(ts => mockDVM.addTurnSnapshot(ts))

    val gun = new TomcatClaws(mockDVM)

    val map: Iterable[Double] = wavesLog.map(getDiff(gun)).filter((p: Double) => p < 999)
    val avgDiff: Double = map.sum / map.size

    if (avgDiff < minDiff) {
      println(avgDiff.formatted("%2.3f") + ": " + locFactory)
      minDiff = avgDiff
      bestLocFactory = locFactory
    }
    if (i % 100 == 0) {
      println(i + ": " + minDiff.formatted("%2.3f") + ": " + bestLocFactory)
    }
    i = i + 1
  } catch {
    case e: Exception => // ignore
  }
}

def getDiff(gun: TomcatClaws)(w: (TurnSnapshot, Double)): Double = {
  val ts = w._1
  if (ts == null) {
    return 999
  }

  val nextTs = snapshotsByRoundTime.get(ts.roundTime + 1)
  if (nextTs.isEmpty) {
    return 999
  }

  val bulletsInAir = nextTs.get.mySnapshot.getBulletsInAir
  val lastBullet = bulletsInAir.get(bulletsInAir.size() - 1)
  val bo = gun.getBearingOffset(ts, lastBullet.getSpeed)
  scala.math.toDegrees(scala.math.abs(bo - w._2))
}

/*println(AttributesManager.attributes.map((a: Attribute) => a.name + "=" + head.getAttrValue(a)).mkString(", "))
println(locFactory.getPlainLocation(head).mkString(", "))
println(locFactory.getNormalLocation(head).mkString(", "))
println(locFactory.getWeightedLocation(head).mkString(", "))*/

/*
if (ts == null) {
  null
} else {
  val nextTs = snapshotsByRoundTime.get (ts.roundTime + 1)
  if (nextTs.isEmpty) {
  null
} else {
  val bulletsInAir = nextTs.get.mySnapshot.getBulletsInAir
  val lastBullet = bulletsInAir.get (bulletsInAir.size () - 1)
  val bo = gun.getBearingOffset (ts, lastBullet.getSpeed)
  scala.math.abs (bo - w._2)
}
}
*/