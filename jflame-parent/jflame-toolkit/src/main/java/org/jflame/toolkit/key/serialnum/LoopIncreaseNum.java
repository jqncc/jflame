package org.jflame.toolkit.key.serialnum;

/**
 * 循环递增数.原子数每次+1递增，到达最大值时重新从初始值开始递增。
 * <p>
 * 同一实例线程安全 ，不同实例线程不安全
 * 
 * @author yucan.zhang
 */
public class LoopIncreaseNum extends BaseIncreaseNum {

    private long maxSeq;

    /**
     * 构造函数.初始值默认1
     * 
     * @param maxNum 最大值
     */
    public LoopIncreaseNum(long maxNum) {
        super();
        this.maxSeq = maxNum;
    }

    /**
     * 构造函数
     * 
     * @param maxNum 最大值
     * @param initNum 初始值
     */
    public LoopIncreaseNum(long maxNum, int initNum) {
        super(initNum);
        this.maxSeq = maxNum;
    }

    /**
     * 下一个递增值
     * 
     * @return
     */
    @Override
    public long nextNum() {
        long next = sequence.getAndIncrement();
        if (next > maxSeq) {
            sequence.set(initSeq);
        }
        return next;
    }

    /*
     * public static void main(String[] args) { final LoopIncreaseNum increaseNum = new LoopIncreaseNum(100); Thread
     * thread1 = new Thread(new Runnable() { public void run() { for (int i = 0; i < 10; i++) {
     * System.out.println("thread1:" + increaseNum.nextNum()); } } }); Thread thread2 = new Thread(new Runnable() {
     * public void run() { for (int i = 0; i < 20; i++) { System.out.println("thread2:" + increaseNum.nextNum()); } }
     * }); thread1.start(); thread2.start(); }
     */
}
