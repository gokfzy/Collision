package io.github.gokfzy.core.util;

import java.math.BigInteger;

public class BigIntegerPack {
    private BigInteger integer;
    private Boolean isMaxOrMin;
    public BigIntegerPack(BigInteger integer, Boolean isMaxOrMin) {
        this.integer = integer;
        this.isMaxOrMin = isMaxOrMin;
    }

    public void setInteger(BigInteger integer) {
        this.integer = integer;
        this.isMaxOrMin = null;
    }

    public int compareTo(BigInteger other) {
        if (isMaxOrMin != null) {
            if (isMaxOrMin) {
                return 1;
            } else {
                return -1;
            }
        }
        return integer.compareTo(other);
    }
}