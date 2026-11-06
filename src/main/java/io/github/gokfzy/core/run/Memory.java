package io.github.gokfzy.core.run;

import io.github.gokfzy.core.util.block.blockAttribute.Data;
import io.github.gokfzy.core.util.block.blockAttribute.Pos;
import io.github.gokfzy.core.util.block.enums.Type;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Memory implements Iterable<Map.Entry<Pos, Data>> {

    private volatile Map<Pos, Data> blockMap;

    public Memory() {
        blockMap = new ConcurrentHashMap<>();
    }

    public Data get(Pos pos) {
        Data v = blockMap.get(pos);
        return v == null ? Data.empty() : v;
    }

    public Data get (BigInteger x, BigInteger y) {
        return get(new Pos(x, y));
    }

    public void set(Pos pos, Data data) {
        Data old;
        if (data.type() == Type.NONE) {
            old = blockMap.remove(pos);
        } else {
            old = blockMap.put(pos, data);
        }
        if (old == null) {
            Data.empty();
        }
    }

    public void set(BigInteger x, BigInteger y, Data data) {
        set(Pos.of(x, y), data);
    }

    public Map<Pos, Data> getBlockMap() {
        return Map.copyOf(blockMap);
    }

    public void setBlockMap(Map<Pos, Data> newDataMap) {
        Map<Pos, Data> copy = new ConcurrentHashMap<>();
        newDataMap.forEach((pos, data) -> {
            if (data != null && data.type() != Type.NONE) copy.put(pos, data);
        });
        this.blockMap = copy;
    }

    @Override
    public Iterator<Map.Entry<Pos, Data>> iterator() {
        Iterator<Map.Entry<Pos, Data>> it = blockMap.entrySet().iterator();
        return new Iterator<>() {
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public Map.Entry<Pos, Data> next() {
                Map.Entry<Pos, Data> e = it.next();
                return Map.entry(e.getKey(), e.getValue());
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Pos, Data> e : blockMap.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }
}