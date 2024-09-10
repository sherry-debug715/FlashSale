package com.flashsell.flashsell.util;

public class SnowFlake {
    /**
     * Start timestamp
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * The number of bits occupied by each part.
     */
    //The number of bits occupied by the serial number.
    private final static long SEQUENCE_BIT = 12; 
    //The number of bits occupied by machines.
    private final static long MACHINE_BIT = 5;   
    //The number of bits occupied by data centers.
    private final static long DATACENTER_BIT = 5;

    /**
     * Maxnum of each part
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * The left shift of each part.
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId;  //data center
    private long machineId;     //machine ID
    private long sequence = 0L; //serial NO.
    private long lastStmp = -1L;//previous timestamp

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * generate next ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            // The sequence number increments within the same millisecond.
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // The sequence number has reached its maximum within the same millisecond
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            // The sequence number is reset to 0 in different milliseconds.
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 1);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            System.out.println(snowFlake.nextId());
        }

        System.out.println("Total time cost: " + (System.currentTimeMillis() - start));
    }
}
