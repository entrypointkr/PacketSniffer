package kr.entree.packetsniffer.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputHandler;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JunHyung Lim on 2019-10-10
 */
public class PacketMonitor extends PacketAdapter {
    private final Plugin plugin;
    private final List<PacketSnapshot> snapshots = new ArrayList<>();
    private List<Packet> snapshotQueue = new ArrayList<>();
    private LocalTime start = LocalTime.MIN;

    public PacketMonitor(Plugin plugin) {
        super(plugin, getServerPackets());
        this.plugin = plugin;
    }

    public static Iterable<PacketType> getServerPackets() {
        List<PacketType> packets = new ArrayList<>();
        for (PacketType type : PacketType.values()) {
            if (type.isServer()) {
                packets.add(type);
            }
        }
        return packets;
    }

    private long calculateBytes() {
        long total = 0;
        for (Packet packet : snapshotQueue) {
            total += packet.bytes;
        }
        return total;
    }

    private void takeSnapshot() {
        snapshots.add(PacketSnapshot.of(start, snapshotQueue));
        start = LocalTime.now();
        snapshotQueue = new ArrayList<>();
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        event.getNetworkMarker().addOutputHandler(new RawHandler());
    }

    public void clear() {
        start = LocalTime.MIN;
        snapshots.clear();
        snapshotQueue.clear();
    }

    public List<PacketSnapshot> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    class RawHandler implements PacketOutputHandler {
        @Override
        public ListenerPriority getPriority() {
            return ListenerPriority.MONITOR;
        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public byte[] handle(PacketEvent event, byte[] bytes) {
            if (start == LocalTime.MIN) {
                start = LocalTime.now();
            }
            Duration diff = Duration.between(start, LocalTime.now());
            if (diff.getSeconds() >= 1) {
                takeSnapshot();
            }
            snapshotQueue.add(new Packet(event.getPacketType(), bytes.length));
            return bytes;
        }
    }
}
