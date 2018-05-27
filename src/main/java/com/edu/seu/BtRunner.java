package com.edu.seu;

import com.edu.seu.BHTServer.DhtServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;


/*
* 作为加入dht网络节点的启动器
*
* */
@Slf4j
@Component
public class BtRunner implements CommandLineRunner {

    private DhtServer dhtServer;

    public BtRunner(DhtServer dhtServer){
        this.dhtServer=dhtServer;
    }

    @Override
    public void run(String... args) throws Exception {
        dhtServer.init();
        InetSocketAddress address1 = new InetSocketAddress("router.bittorrent.com", 6881);
        InetSocketAddress address = new InetSocketAddress("dht.transmissionbt.com", 6881);

    }
}
