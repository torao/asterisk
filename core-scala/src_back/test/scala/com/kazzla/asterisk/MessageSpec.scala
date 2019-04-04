/*
 * Copyright (c) 2013 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package com.kazzla.asterisk

import org.specs2.Specification
import java.lang.reflect.Modifier

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// MessageSPec
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
class MessageSpec extends Specification { def is = s2"""
The message class should:
declared with sealed and abstract. $msg01

The block class should:
be EOF if it's eof parameter is true, length is zero. $block01
not be EOF is it's length is not zero. $block02
create empty block with static method. $block03
be normal block if it is empty buffer. $block04
"""

  def msg01 = {
    Modifier.isAbstract(classOf[Message].getModifiers) must beTrue
  }

  def block01 = {
    val eof = Block.eof(0)
    (eof.length === 0) and (eof.eof must beTrue)
  }

  def block02 = {
    val eof = Block(0, Array[Byte](1))
    (eof.length === 1) and (eof.eof must beFalse)
  }

  def block03 = Block.eof(10).eof must beTrue

  def block04 = {
    val b = Block(0, Array[Byte]())
    (b.length === 0) and (b.eof must beFalse)
  }

}
