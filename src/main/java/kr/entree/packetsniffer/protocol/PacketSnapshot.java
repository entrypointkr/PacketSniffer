package kr.entree.packetsniffer.protocol;

import com.comphenix.protocol.PacketType;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by JunHyung Lim on 2019-10-10
 */
public class PacketSnapshot {
    private final LocalTime start;
    private final long totalBytes;
    private final List<Packet> packets;

    public static PacketSnapshot of(LocalTime start, List<Packet> packets) {
        long totalBytes = 0;
        for (Packet packet : packets) {
            totalBytes += packet.bytes;
        }
        return of(start, totalBytes, packets);
    }

    public static PacketSnapshot of(LocalTime start, long totalBytes, List<Packet> packets) {
        return new PacketSnapshot(start, totalBytes, packets);
    }

    private PacketSnapshot(LocalTime start, long totalBytes, List<Packet> packets) {
        this.start = start;
        this.totalBytes = totalBytes;
        this.packets = packets;
    }

    public LocalTime getStart() {
        return start;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public List<Packet> getPackets() {
        return Collections.unmodifiableList(packets);
    }

    public Map<PacketType, AtomicLong> toMap() {
        Map<PacketType, AtomicLong> map = new HashMap<>();
        for (Packet packet : getPackets()) {
            AtomicLong counter = map.computeIfAbsent(packet.type, k -> new AtomicLong());
            counter.addAndGet(packet.bytes);
        }
        return map;
    }
}
