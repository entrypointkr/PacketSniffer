package kr.entree.packetsniffer.protocol;

import com.comphenix.protocol.PacketType;

/**
 * Created by JunHyung Lim on 2019-10-10
 */
public class Packet {
    public final PacketType type;
    public final long bytes;

    public Packet(PacketType type, long bytes) {
        this.type = type;
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + type +
                ", bytes=" + bytes +
                '}';
    }
}
