package io.asterisque.wire.gateway

import java.net.SocketAddress

import io.asterisque.utils.EventDispatcher
import io.asterisque.wire.gateway.Wire._
import javax.annotation.{Nonnull, Nullable}
import javax.net.ssl.SSLSession
import org.slf4j.LoggerFactory

/**
  * メッセージの伝達ラインを実装するインターフェースです。TCP 接続における非同期 Socket に相当し、Wire のクローズは TCP 接続の
  * クローズを意味します。[[io.asterisque.wire.rpc.Session]] に対して再接続が行われる場合、新しい Wire のインスタンスが
  * 生成されます。
  *
  * このクラスではメッセージのキュー/バッファリングが行われます。back pressure 等のフロー制御、再接続の処理はより上位層で
  * 行われます。
  *
  * @param name              この Wire の名前
  * @param inboundQueueSize  受信キューサイズ
  * @param outboundQueueSize 送信キューサイズ
  */
abstract class Wire protected(@Nonnull val name:String, val inboundQueueSize:Int, val outboundQueueSize:Int)
  extends EventDispatcher[Listener] with AutoCloseable {

  /**
    * 受信メッセージのキュー。
    */
  val inbound = new MessageQueue(name + ":IN", inboundQueueSize)

  /**
    * 送信メッセージのキュー。
    */
  val outbound = new MessageQueue(name + ":OUT", outboundQueueSize)

  /**
    * この Wire のローカル側アドレスを参照します。ローカルアドレスが確定していない場合は null を返します。
    */
  @Nullable
  def local:SocketAddress

  /**
    * この Wire のリモート側アドレスを参照します。リモートアドレスが確定していない場合は null を返します。
    */
  @Nullable
  def remote:SocketAddress

  /**
    * こちら側の端点が接続を受け付けた場合に true を返します。プロトコルの便宜上どちらが master でどちらが worker かの役割を
    * 決定する必要がある場合に使用することができます。
    *
    * @return こちらの端点が接続を受け付けた側の場合 true
    */
  def isPrimary:Boolean

  /**
    * この Wire の通信相手の証明書セッションを参照します。ピアとの通信に認証が使用されていなければ Optional.empty() を返します。
    *
    * @return 通信の SSL セッション
    */
  @Nonnull
  def session:Option[SSLSession]

  /**
    * この Wire をクローズしリソースを解放します。登録されている [[Listener]] に対して wireClosed() が通知されます。
    */
  override def close():Unit = {
    inbound.close()
    outbound.close()
    super.foreach(_.wireClosed(this))
  }

}

object Wire {
  private[Wire] val logger = LoggerFactory.getLogger(classOf[Wire])

  /**
    * [[Wire]] のエラー状況通知を受けるためのリスナ。
    */
  trait Listener {
    def wireClosed(@Nonnull wire:Wire):Unit

    def wireError(@Nonnull wire:Wire, @Nonnull ex:Throwable):Unit
  }

}
