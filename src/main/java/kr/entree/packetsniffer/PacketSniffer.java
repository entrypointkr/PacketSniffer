package kr.entree.packetsniffer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import kr.entree.packetsniffer.protocol.PacketMonitor;
import kr.entree.packetsniffer.protocol.PacketSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by JunHyung Lim on 2019-10-10
 */
public class PacketSniffer extends JavaPlugin {
    private final PacketMonitor handler = new PacketMonitor(this);

    public static PacketSniffer get() {
        return (PacketSniffer) Bukkit.getPluginManager().getPlugin("PacketSniffer");
    }

    public void turnOn() {
        turnOff();
        ProtocolLibrary.getProtocolManager().addPacketListener(handler);
    }

    public void turnOff() {
        handler.clear();
        ProtocolLibrary.getProtocolManager().removePacketListener(handler);
    }

    @Override
    public void onDisable() {
        turnOff();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            execute(sender, label, args);
        } catch (CommandException ex) {
            sender.sendMessage(ex.getMessage());
        }
        return true;
    }

    private void execute(CommandSender sender, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "on":
                    checkPermission(sender);
                    turnOn();
                    tellSuccess(sender);
                    return;
                case "off":
                    checkPermission(sender);
                    turnOff();
                    tellSuccess(sender);
                    return;
                case "info":
                    checkPermission(sender);
                    sendTopFive(sender);
                    return;
                case "clear":
                    checkPermission(sender);
                    handler.clear();
                    tellSuccess(sender);
                    return;
            }
        }
        throw new CommandException(String.format("Usage: /%s (on|off|info|clear)", label));
    }

    private void tellSuccess(CommandSender sender) {
        sender.sendMessage("Done.");
    }

    public void sendTopFive(CommandSender sender) {
        List<PacketSnapshot> snapshots = new ArrayList<>(handler.getSnapshots());
        snapshots.sort((o1, o2) -> Long.compare(o2.getTotalBytes(), o1.getTotalBytes()));
        int count = Math.min(snapshots.size(), 5);
        for (int i = 0; i < count; i++) {
            PacketSnapshot snapshot = snapshots.get(i);
            sender.sendMessage(String.format("#%d %s Bytes, %s",
                    i + 1, snapshot.getTotalBytes(), snapshot.getStart().format(DateTimeFormatter.ISO_LOCAL_TIME)));
            sendSnapshotInfo(sender, snapshot);
        }
    }

    private void sendSnapshotInfo(CommandSender sender, PacketSnapshot snapshot) {
        Map<PacketType, AtomicLong> map = snapshot.toMap();
        LinkedList<Map.Entry<PacketType, AtomicLong>> entries = new LinkedList<>(map.entrySet());
        entries.sort((o1, o2) -> Long.compare(o2.getValue().get(), o1.getValue().get()));
        sender.sendMessage("-------------------");
        for (Map.Entry<PacketType, AtomicLong> entry : entries) {
            sender.sendMessage(String.format("%s %s bytes", entry.getKey().name(), entry.getValue().get()));
        }
        sender.sendMessage("-------------------");
    }

    private void checkPermission(CommandSender sender) {
        if (!sender.hasPermission("packetsniffer.on")) {
            throw new CommandException("No permission");
        }
    }
}
