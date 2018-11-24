package io.asterisque.core.msg

import java.lang.reflect.Modifier

class ControlSpec extends AbstractMessageSpec {
  override def is = super.is append
    s2"""
Control should declare as final class. ${Modifier.isFinal(classOf[Control].getModifiers) must beTrue}
have properties these are specified in constructor. $e0
throw IllegalArgumentException if data is null. ${new Control(Control.Close, null) must throwA[IllegalArgumentException]}
"""

  private[this] def e0 = {
    val c1 = new Control(Control.SyncConfig)
    val c2 = new Control(Control.Close, new Array[Byte](256))
    (c1.code === Control.SyncConfig) and (c1.data.length === 0) and (c2.code === Control.Close) and (c2.data.length === 256)
  }

  protected def newMessages = Seq(
    new Control(Control.SyncConfig),
    new Control(Control.Close, new Array[Byte](256))
  )

}