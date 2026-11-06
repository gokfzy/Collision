package io.github.gokfzy.core.util.block.blockAttribute;

import java.math.BigInteger;

public record Pos(BigInteger x, BigInteger y) {
    public static Pos of(BigInteger x, BigInteger y) {
        return new Pos(x, y);
    }

    public Pos addX(BigInteger x) {
        return Pos.of(this.x.add(x), y);
    }

    public Pos addY(BigInteger y) {
        return Pos.of(x, this.y.add(y));
    }
}