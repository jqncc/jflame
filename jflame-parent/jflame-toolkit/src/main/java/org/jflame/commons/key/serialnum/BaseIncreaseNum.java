package org.jflame.commons.key.serialnum;

import java.util.concurrent.atomic.AtomicInteger;

public class BaseIncreaseNum {

    protected int initSeq = 1;
    protected AtomicInteger sequence;

    public BaseIncreaseNum() {
        sequence = new AtomicInteger(initSeq);
    }

    public BaseIncreaseNum(int initSeq) {
        this.initSeq = initSeq;
        sequence = new AtomicInteger(initSeq);
    }

    public long nextNum() {
        return sequence.getAndIncrement();
    }

    public int getInitSeq() {
        return initSeq;
    }

}
