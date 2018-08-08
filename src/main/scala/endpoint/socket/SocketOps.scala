package endpoint.socket

import cats.effect.IO

import endpoint.{ReadEndpoint, WriteEndpoint}

import java.io._
import java.net._
import java.nio.channels.{SocketChannel, ServerSocketChannel}
import javax.net.ssl.{SSLServerSocket, SSLServerSocketFactory}

/** Set of operations that create [[endpoint.ReadEndpoint]] and 
 *  [[endpoint.WriteEndpoint]] instances to access sockets. They
 *  are created by wrapping instances of java streams that operate
 *  on those sockets.
 */
object ServerSocketOps {

  def serverSocket(portO: Option[Int], backlogO: Option[Int], addrO: Option[InetAddress]): IO[ServerSocket] =
    (portO, backlogO, addrO) match {
      case (None, None, None) => IO { new ServerSocket() }
      case (Some(port), None, None) => IO { new ServerSocket(port) }
      case (Some(port), Some(backlog), None) => IO { new ServerSocket(port, backlog) }
      case (Some(port), Some(backlog), Some(addr)) => IO { new ServerSocket(port, backlog, addr) }
      case _ => IO.raiseError(new IllegalArgumentException(s"Not valid configuration when asking to create a server socket: ($portO, $backlogO, $addrO)"))
    }

  def sslServerSocket(portO: Option[Int], backlogO: Option[Int], addrO: Option[InetAddress]): IO[SSLServerSocket] = 
    (portO, backlogO, addrO) match {
      case (None, None, None) => IO { SSLServerSocketFactory.getDefault().createServerSocket().asInstanceOf[SSLServerSocket]}
      case (Some(port), None, None) => IO { SSLServerSocketFactory.getDefault().createServerSocket(port).asInstanceOf[SSLServerSocket] }
      case (Some(port), Some(backlog), None) => IO { SSLServerSocketFactory.getDefault().createServerSocket(port, backlog).asInstanceOf[SSLServerSocket] }
      case (Some(port), Some(backlog), Some(addr)) => IO { SSLServerSocketFactory.getDefault().createServerSocket(port, backlog, addr).asInstanceOf[SSLServerSocket] }
      case _ => IO.raiseError(new IllegalArgumentException(s"Not valid configuration when asking to create an SSL server socket: ($portO, $backlogO, $addrO)"))
    }

  def setSocketFactoryIO(fac: SocketImplFactory): IO[Unit] = IO {
    ServerSocket.setSocketFactory(fac)
  }

  implicit class Ops(socket: ServerSocket) {

    def acceptIO: IO[Socket] = IO {
      socket.accept()
    }

    def bindIO(endpoint: SocketAddress): IO[Unit] = IO {
      socket.bind(endpoint)
    }

    def bindIO(endpoint: SocketAddress, backlog: Int): IO[Unit] = IO {
      socket.bind(endpoint, backlog)
    }

    def closeIO: IO[Unit] = IO {
      socket.close()
    }

    def isBoundIO: IO[Boolean] = IO {
      socket.isBound()
    }

    def getChannelIO: IO[Option[ServerSocketChannel]] = IO {
      Option(socket.getChannel())
    }

    def getInetAddressIO: IO[Option[InetAddress]] = IO {
      Option(socket.getInetAddress())
    }

    def getLocalPortIO: IO[Int] = IO {
      socket.getLocalPort()
    }

    def getLocalSocketAddressIO: IO[Option[SocketAddress]] = IO {
      Option(socket.getLocalSocketAddress())
    }

    def isClosedIO: IO[Boolean] = IO {
      socket.isClosed()
    }

    def getSoTimeoutIO(timeout: Int): IO[Int] = IO {
      socket.getSoTimeout()
    }

    def setSoTimeoutIO(timeout: Int): IO[Unit] = IO {
      socket.setSoTimeout(timeout)
    }

    def setReuseAddressIO(on: Boolean): IO[Unit] = IO {
      socket.setReuseAddress(on)
    }

    def getReuseAddressIO: IO[Boolean] = IO {
      socket.getReuseAddress()
    }

    def getReceiveBufferSizeIO: IO[Int] = IO {
      socket.getReceiveBufferSize()
    }

    def setReceiveBufferSizeIO(size: Int): IO[Unit] = IO {
      socket.setReceiveBufferSize(size)
    }

    def setPerformancePreferencesIO(connectionTime: Int, latency: Int, bandwidth: Int): IO[Unit] = IO {
      socket.setPerformancePreferences(connectionTime, latency, bandwidth)
    }

  }

}

// TODO: UDP sockets: https://docs.oracle.com/javase/8/docs/api/java/net/DatagramSocket.html
// TODO: SSL sockets: https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLSocket.html and SSLServerSockets
// TODO: See what is all that of SocketChannel

object SocketOps {

  def socket: IO[Socket] = IO {
    new Socket()
  }

  def socket(addr: InetAddress, port: Int): IO[Socket] = IO {
    new Socket(addr, port)
  }

  def socket(addr: InetAddress, port: Int, localAddr: InetAddress, localPort: Int): IO[Socket] = IO {
    new Socket(addr, port, localAddr, localPort)
  }

  def setSocketImplFactory(fac: SocketImplFactory): IO[Unit] = IO {
    Socket.setSocketImplFactory(fac)
  }

  def getReadEndpoint(socket: Socket): IO[ReadEndpoint] = IO {
      val is = new BufferedInputStream(socket.getInputStream())
      new ReadEndpoint(is)
  }
  
  def getWriteEndpoint(socket: Socket): IO[WriteEndpoint] = IO {
      val os = new BufferedOutputStream(socket.getOutputStream())
      new WriteEndpoint(os)
  }
  
  implicit class Ops(socket: Socket) {

    def bindIO(bindpoint: SocketAddress): IO[Unit] = IO {
      socket.bind(bindpoint)
    }

    def isBoundIO: IO[Boolean] = IO {
      socket.isBound()
    }

    def closeIO: IO[Unit] = IO {
      socket.close()
    }

    def isClosedIO: IO[Boolean] = IO {
      socket.isClosed()
    }

    def connectIO(endpoint: SocketAddress): IO[Unit] = IO {
      socket.connect(endpoint)
    }

    def connectIO(endpoint: SocketAddress, timeout: Int): IO[Unit] = IO {
      socket.connect(endpoint, timeout)
    }

    def isConnectedIO: IO[Boolean] = IO {
      socket.isConnected()
    }

    def getChannelIO: IO[Option[SocketChannel]] = IO {
      Option(socket.getChannel())
    }

    def getInetAddressIO: IO[Option[InetAddress]] = IO {
      Option(socket.getInetAddress())
    }

    def getLocalAddressIO: IO[Option[InetAddress]] = IO {
      Option(socket.getLocalAddress())
    }

    def getPortIO: IO[Int] = IO {
      socket.getPort()
    }

    def getLocalPortIO: IO[Int] = IO {
      socket.getLocalPort()
    }

    def getRemoteSocketAddressIO: IO[Option[SocketAddress]] = IO {
      Option(socket.getRemoteSocketAddress())
    }

    def getLocalSocketAddressIO: IO[Option[SocketAddress]] = IO {
      Option(socket.getLocalSocketAddress())
    }

    def getInputStreamIO: IO[InputStream] = IO {
      socket.getInputStream()
    }

    def getOutputStreamIO: IO[OutputStream] = IO {
      socket.getOutputStream()
    }

    def getTcpNoDelayIO: IO[Boolean] = IO {
      socket.getTcpNoDelay()
    }

    def setTcpNoDelayIO(on: Boolean): IO[Unit] = IO {
      socket.setTcpNoDelay(on)
    }

    def getSoLingerIO: IO[Int] = IO {
      socket.getSoLinger()
    }

    def setSoLingerIO(on: Boolean, linger: Int): IO[Unit] = IO {
      socket.setSoLinger(on, linger)
    }

    def sendUrgentDataIO(data: Int): IO[Unit] = IO {
      socket.sendUrgentData(data)
    }

    def getOOBInlineIO: IO[Unit] = IO {
      socket.getOOBInline()
    }

    def setOOBInlineIO(on: Boolean): IO[Unit] = IO {
      socket.setOOBInline(on)
    }

    def getSoTimeoutIO: IO[Int] = IO {
      socket.getSoTimeout()
    }

    def setSoTimeoutIO(int: Int): IO[Unit] = IO {
      socket.setSoTimeout(int)
    }

    def getSendBufferSizeIO: IO[Int] = IO {
      socket.getSendBufferSize()
    }

    def setSendBufferSizeIO(size: Int): IO[Unit] = IO {
      socket.setSendBufferSize(size)
    }

    def getReceiveBufferSizeIO: IO[Int] = IO {
      socket.getReceiveBufferSize()
    }

    def setReceiveBufferSizeIO(size: Int): IO[Unit] = IO {
      socket.setReceiveBufferSize(size)
    }

    def getKeepAliveIO: IO[Boolean] = IO {
      socket.getKeepAlive()
    }

    def setKeepAliveIO(on: Boolean): IO[Unit] = IO {
      socket.setKeepAlive(on)
    }

    def getTrafficClassIO: IO[Int] = IO {
      socket.getTrafficClass
    }

    def setTrafficClassIO(tc: Int): IO[Unit] = IO {
      socket.setTrafficClass(tc)
    }

    def getReuseAddressIO: IO[Boolean] = IO {
      socket.getReuseAddress()
    }

    def setReuseAddressIO(on: Boolean): IO[Unit] = IO {
      socket.setReuseAddress(on)
    }

    def shutdownInputIO: IO[Unit] = IO {
      socket.shutdownInput()
    }

    def isInputShutdownIO: IO[Boolean] = IO {
      socket.isInputShutdown()
    }

    def shutdownOutputIO: IO[Unit] = IO {
      socket.shutdownOutput()
    }

    def isOutputShutdownIO: IO[Boolean] = IO {
      socket.isOutputShutdown()
    }

    def setPerformancePreferencesIO(connectionTime: Int, latency: Int, bandwith: Int): IO[Unit] = IO {
      socket.setPerformancePreferences(connectionTime, latency, bandwith)
    }

  }

}
