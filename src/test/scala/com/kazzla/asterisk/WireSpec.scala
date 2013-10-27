/*
 * Copyright (c) 2013 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package com.kazzla.asterisk

import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.Specification
import org.specs2.execute.Result
import org.specs2.matcher.MatchResult

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// WireSpec
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
*/
abstract class WireSpec extends Specification { def is = s2"""
Wire should:
either server or not flag. $e0
have correct close status. $e1
have correct active/deactive status. $e2
transfer messages duplex. $e3
append and remove onReceive handlers. $e4
buffers all received messages before start. $e5
throw IOException from send() if wire closed. $e6
not dispatch any message from receive() if wire closed. $e7
be able to refer TLS/SSL session. $e8
have correct peer name. $e9
"""

	/**
	 * subclass should pair of transmission endpoint as tuple of wires.
	*/
	def wires[T](f:(Wire,Wire)=>Result):Result

	def secureWire[T](f:(Wire)=>Result):Result = skipped

	def e0 = wires{ (w1, w2) =>
		(w1.isServer !=== w2.isServer)
	}
	
	def e1 = wires{ (w1, w2) =>
		(w1.isClosed must beFalse) and (w2.isClosed must beFalse) and
		{ w1.close(); w1.isClosed must beTrue } and { w2.close(); w2.isClosed must beTrue }
	}

	def e2 = wires{ (w1, w2) =>
		(w1.isActive must beFalse) and (w2.isActive must beFalse) and
		{ w1.start(); w1.isActive must beTrue  } and { w2.start(); w2.isActive must beTrue  } and
		{ w1.stop();  w1.isActive must beFalse } and { w2.stop();  w2.isActive must beFalse }
	}

	def e3 = wires{ (w1, w2) =>
		val m1 = Open(1, 2)
		val m2 = Close(2, "success", null)
		val p1 = Promise[Message]()
		val p2 = Promise[Message]()
		w1.onReceive ++ { m => p1.success(m) }
		w2.onReceive ++ { m => p2.success(m) }
		w1.start()
		w2.start()
		w1.send(m1)
		w2.send(m2)
		(Await.result(p1.future, Limit) === m2) and (Await.result(p2.future, Limit) === m1)
	}

	def e4 = wires{ (w1, w2) =>
		var count = 0
		case class H(n:Int){
			private val p = Promise[Message]()
			def h(m:Message):Unit = { count += n; p.success(m) }
			def r:Message = Await.result(p.future, Limit)
		}

		count = 0
		val hs1 = List(H(1), H(2), H(3))
		hs1.foreach{ w2.onReceive ++ _.h }
		w2.start()
		w1.send(Open(0, 0))
		hs1.foreach{ _.r }
		count === hs1.map{ _.n }.sum
	}

	def e5 = wires{ (w1, w2) =>
		val msg = Array(Open(5, 0), Block.eof(5), Close(5, "hoge", null))
		val r = new collection.mutable.ArrayBuffer[Message]()
		val p1 = Promise[Int]()
		val p2 = Promise[Int]()
		def h(m:Message){
			r.append(m)
			if(r.length == 1){
				p1.success(1)
			}
			if(r.length == msg.length){
				p2.success(r.length)
			}
		}
		
		w2.onReceive ++ h
		assert(! w2.isActive)
		w1.send(msg(0))
		Thread.sleep(500)
		val notReceiveBeforeStart = (r.length === 0) and (p1.future.isCompleted must beFalse)
		w2.start()
		Await.result(p1.future, Limit)
		val receiveAfterStart = (r.length === 1) and (p1.future.isCompleted must beTrue)
		w1.send(msg(1))
		w1.send(msg(2))
		Await.result(p2.future, Limit)
		val receiveAll = (r.length === 3)
		notReceiveBeforeStart and receiveAfterStart and receiveAll
	}

	def e6 = wires{ (w1, w2) =>
		w1.send(Open(6, 0))
		w1.close()
		w1.send(Open(6, 1)) must throwA[java.io.IOException]
	}

	def e7 = wires{ (w1, w2) =>
		var count = 0
		w2.onReceive ++ { _ => count += 1 }
		w2.start()
		w1.send(Open(7, 0))
		w2.close()
		w1.send(Open(7, 1))
		Thread.sleep(500)
		count === 1
	}

	def e8 = secureWire { w =>
		Await.result(w.tls, Limit)
		success
	}

	def e9 = wires{ (w1, w2) =>
		(w1.peerName !=== null) and (w1.peerName !=== null)
	}

	val Limit = Duration(10, SECONDS)

}

class PipeWireSpec extends WireSpec {
	def wires[T](f:(Wire,Wire)=>Result):Result = {
		val w = Wire.newPipe()
		using(w._1){ w1 =>
			using(w._2){ w2 =>
				f(w1, w2)
			}
		}
	}
}