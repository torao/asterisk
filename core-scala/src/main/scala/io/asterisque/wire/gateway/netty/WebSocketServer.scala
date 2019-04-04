package io.asterisque.wire.gateway.netty

import java.net.{InetSocketAddress, SocketAddress, URI}
import java.util.concurrent.atomic.AtomicBoolean

import io.asterisque.wire.gateway.Server
import io.asterisque.wire.gateway.netty.WebSocketServer.logger
import io.netty.channel.{Channel, ChannelHandlerContext}
import javax.annotation.{Nonnull, Nullable}
import org.slf4j.LoggerFactory


private[netty] class WebSocketServer(uri:URI) extends Server with WebSocket.Server.Listener {
  final private var channel:Channel = _
  final private val closed = new AtomicBoolean(false)

  override def wsServerReady(@Nonnull ch:Channel):Unit = {
    logger.trace("WebSocketServer.wsServerReady({})", ch)
    if(!closed.get) {
      channel = ch
    } else {
      logger.debug("websocket server is ready but it has already been closed")
      ch.close
    }
  }

  override def wsServerCaughtException(@Nullable ctx:ChannelHandlerContext, @Nonnull ex:Throwable):Unit = {
    logger.error(s"WebSocketServer.wsServerCaughtException($ctx, $ex)")
    if(ctx != null) {
      ctx.close()
    }
  }

  @Nonnull
  override def acceptURI:URI = if(uri.getPort <= 0) {
    Option(channel).map(_.localAddress()) match {
      case Some(addr:InetSocketAddress) =>
        new URI(uri.getScheme, uri.getUserInfo, addr.getAddress.getHostName, addr.getPort, uri.getPath, uri.getQuery, uri.getFragment)
      case _ => uri
    }
  } else uri

  override def close():Unit = {
    if(closed.compareAndSet(false, true)) {
      Option(channel).foreach(_.close())
    }
  }
}

object WebSocketServer {
  private[WebSocketServer] val logger = LoggerFactory.getLogger(classOf[WebSocketServer])
}
