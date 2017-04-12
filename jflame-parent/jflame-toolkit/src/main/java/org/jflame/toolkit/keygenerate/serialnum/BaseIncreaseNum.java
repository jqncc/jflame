package org.jflame.toolkit.keygenerate.serialnum;

import java.util.concurrent.atomic.AtomicLong;

public class BaseIncreaseNum {

    protected long initSeq = 1L;
    protected AtomicLong sequence;
    protected AtomicLong lastTimestamp = new AtomicLong(0L);

    public BaseIncreaseNum() {
        sequence = new AtomicLong(initSeq);
    }

    public BaseIncreaseNum(long initSeq) {
        this.initSeq = initSeq;
        sequence = new AtomicLong(initSeq);
    }

    public long nextNum() {
        return sequence.getAndIncrement();
    }

}
