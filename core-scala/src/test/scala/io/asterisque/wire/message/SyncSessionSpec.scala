package io.asterisque.wire.message

import java.lang.reflect.Modifier

import io.asterisque.test.{CertificateAuthority, _}
import io.asterisque.utils.Version
import org.msgpack.MessagePack
import org.specs2.Specification
import org.specs2.specification.BeforeAfterAll

import scala.util.Random

class SyncSessionSpec extends Specification with BeforeAfterAll {
  def is =
    s2"""
It should be declared as final class. ${Modifier.isFinal(classOf[SyncSession].getModifiers)}
It can serialize and deserialize. $serializeAndDeserialize
It should throw exception if data binary is broken. $throwExceptionIfBinaryIsBroken
It should throw exception if empty binary. $throwExceptionIfEmptyBinary
SyncSession constracts instance without wsCaughtException. $allConstructorsTest
Data size must be constant value. $verifyDataSize
"""

  private[this] def serializeAndDeserialize = {
    val r = new Random(7498374)
    val version = Version(r.nextInt())
    val envelope = CERT_ENVELOPES.head
    val cert = Codec.CERTIFICATE.decode(envelope.payload)
    val serviceId = r.nextASCIIString(0xFF)
    val utcTime = r.nextLong()
    val ping = r.nextInt()
    val sessionTimeout = r.nextInt()
    val attr = Map("role" -> "ca,miner", "address" -> "Tokyo")
    val config = Map("ping" -> ping.toString, "sessionTimeout" -> sessionTimeout.toString)
    val sc1 = SyncSession(version, envelope, serviceId, utcTime, config)
    val packer = new MessagePack().createBufferPacker()
    Codec.SYNC_SESSION.encode(packer, sc1)
    val sc2 = Codec.SYNC_SESSION.decode(new MessagePack().createBufferUnpacker(packer.toByteArray))
    (sc2.version === version) and (sc2.cert === cert) and
      (sc2.serviceId === serviceId) and (sc2.utcTime === utcTime) and
      (sc2.config === config)
  }

  private[this] def throwExceptionIfBinaryIsBroken = {
    Codec.SYNC_SESSION.decode(new MessagePack().createBufferUnpacker(Array[Byte](0, 0, 0, 0))) must throwA[CodecException]
  }

  private[this] def throwExceptionIfEmptyBinary = {
    Codec.SYNC_SESSION.decode(new MessagePack().createBufferUnpacker(Array.empty[Byte])) must throwA[CodecException]
  }

  private[this] def allConstructorsTest = {
    val envelope = CERT_ENVELOPES.head
    SyncSession(Version(0), envelope, "serviceId", 0, Map.empty)
    SyncSession(envelope, "serviceId", 0, Map.empty)
    success
  }

  private[this] def verifyDataSize = {
    val envelope = CERT_ENVELOPES.head
    val packer = new MessagePack().createBufferPacker()
    Codec.SYNC_SESSION.encode(packer, SyncSession(envelope, "", 0, Map.empty))
    val data = packer.toByteArray
    data.length must lessThan(0xFFFF)
  }

  implicit class _Random(random:Random) {
    def nextASCIIString(length:Int):String = {
      val s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
      (0 until length).map(_ => s(random.nextInt(s.length))).mkString
    }
  }

  private[this] var ca:CertificateAuthority = _

  override def beforeAll():Unit = ca = new CertificateAuthority()

  override def afterAll():Unit = ca.close()

}
