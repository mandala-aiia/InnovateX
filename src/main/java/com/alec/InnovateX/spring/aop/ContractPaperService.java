package com.alec.InnovateX.spring.aop;

import java.util.ArrayList;
import java.util.List;

/**
 * 引介增强的目标类：注意它没有实现 Sealable，业务代码对"密封"概念一无所知。
 * 增强（能否写入由谁决定）与业务（写入内容本身）被彻底分离——这正是 AOP 的价值主张。
 */
public class ContractPaperService {

    private final List<String> clauses = new ArrayList<>();

    public void append(String clause) {
        clauses.add(clause);
        System.out.println("[ContractPaperService] append: " + clause);
    }

    public List<String> getClauses() {
        return clauses;
    }
}
