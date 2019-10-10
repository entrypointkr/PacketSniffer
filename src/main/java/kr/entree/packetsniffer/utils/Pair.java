package kr.entree.packetsniffer.utils;

/**
 * Created by JunHyung Lim on 2019-10-10
 */
public class Pair<K, V> {
    public final K first;
    public final V second;

    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }
}
