package com.alec.InnovateX.spring.scope;

/**
 * prototype 循环依赖的另一端：与 {@link JobShardA} 互为 setter 依赖。
 */
public class JobShardB {

    private JobShardA partner;

    public void setPartner(JobShardA partner) {
        this.partner = partner;
    }

    public JobShardA getPartner() {
        return partner;
    }
}
