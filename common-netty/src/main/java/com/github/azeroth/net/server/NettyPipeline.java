package com.github.azeroth.net.server;

public interface NettyPipeline {

    String LEFT                  = "netty.left.";
    String RIGHT                 = "netty.right.";

    String AccessLogHandler      = LEFT + "accessLogHandler";
    String ChannelMetricsHandler = LEFT + "channelMetricsHandler";
    String ChunkedWriter         = LEFT + "chunkedWriter";
    String CompressionHandler    = LEFT + "compressionHandler";
    String ConnectMetricsHandler = LEFT + "connectMetricsHandler";
    String H2CUpgradeHandler     = LEFT + "h2cUpgradeHandler";
    String H2Flush               = LEFT + "h2Flush";
    String H2MultiplexHandler    = LEFT + "h2MultiplexHandler";
    String H2OrHttp11Codec       = LEFT + "h2OrHttp11Codec";
    String H2ToHttp11Codec       = LEFT + "h2ToHttp11Codec";
    String H3ToHttp11Codec       = LEFT + "h3ToHttp11Codec";
    String HttpAggregator        = LEFT + "httpAggregator";
    String HttpCodec             = LEFT + "httpCodec";
    String HttpDecompressor      = LEFT + "httpDecompressor";
    String HttpMetricsHandler    = LEFT + "httpMetricsHandler";
    String HttpTrafficHandler    = LEFT + "httpTrafficHandler";
    String IdleTimeoutHandler    = LEFT + "idleTimeoutHandler";
    String LoggingHandler        = LEFT + "loggingHandler";
    String NonSslRedirectDetector = LEFT + "nonSslRedirectDetector";
    String NonSslRedirectHandler = LEFT + "nonSslRedirectHandler";
    String OnChannelReadIdle     = LEFT + "onChannelReadIdle";
    String OnChannelWriteIdle    = LEFT + "onChannelWriteIdle";
    String ProxyHandler          = LEFT + "proxyHandler";


    String ProxyProtocolReader   = LEFT + "proxyProtocolReader";
    String ReadTimeoutHandler    = LEFT + "readTimeoutHandler";
    String ResponseTimeoutHandler = LEFT + "responseTimeoutHandler";
    String SslHandler            = LEFT + "sslHandler";
    String SslLoggingHandler     = LEFT + "sslLoggingHandler";
    String SslReader             = LEFT + "sslReader";

    String ReactiveBridge        = RIGHT + "reactiveBridge";
    String WorldClientInitializer = LEFT + "worldClientInitializer";
    String WorldProtocolCodec  = LEFT + "worldProtocolCodec";

}
