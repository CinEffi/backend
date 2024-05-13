package shinzo.cineffi.config;

import java.net.http.HttpClient;
import java.net.ProxySelector;
import java.net.InetSocketAddress;

public class NetworkUtils {

    private static final String PROXY_HOST = "krmp-proxy.9rum.cc";
    private static final int PROXY_PORT = 3128;

    public static HttpClient createHttpClientWithProxy() {
        HttpClient client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(PROXY_HOST, PROXY_PORT)))
                .build();
        return client;
    }
}