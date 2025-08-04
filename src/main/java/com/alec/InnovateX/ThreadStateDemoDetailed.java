package com.alec.InnovateX;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * 一个比 ThreadLifecycleLogger 更详细的 demo：
 * 1. 展示 Java 线程状态转换（NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED）
 * 2. 展示各种 API 触发的转换：start, synchronized 竞争, wait/notify, sleep, join, LockSupport.park/parkNanos, Future cancel(interrupt), interrupt 本身的标志变化
 * 3. 记录每次状态/中断标志变化，并附原因标签（调用方发起的动作）
 * 4. 用多个子任务模拟不同场景，使流程清晰可复用
 */

public class ThreadStateDemoDetailed {
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    /**
     * 监听并打印线程状态 + 中断标志变化的封装器（更细粒度：可以带上“上下文原因”）
     */
    static class DetailedTracker {
        private final Thread target;
        private final ScheduledExecutorService poller;
        private final long intervalMs;
        private Thread.State lastState;
        private boolean lastInterrupted;
        private volatile boolean stopped = false;

        public DetailedTracker(Thread target, long intervalMs) {
            this.target = target;
            this.intervalMs = Math.max(10, intervalMs);
            this.lastState = target.getState();
            this.lastInterrupted = target.isInterrupted();
            this.poller = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Tracker-for-" + target.getName());
                t.setDaemon(true);
                return t;
            });
        }

        public void start() {
            poller.scheduleAtFixedRate(this::poll, 0, intervalMs, TimeUnit.MILLISECONDS);
        }

        private void poll() {
            if (stopped) return;
            Thread.State curState = target.getState();
            boolean curInterrupt = target.isInterrupted();
            String ts = TS_FMT.format(Instant.now());
            if (curState != lastState || curInterrupt != lastInterrupted) {
                StringBuilder sb = new StringBuilder();
                sb.append("[" + ts + "] ");
                sb.append("Thread='" + target.getName() + "' ");
                sb.append("state: " + lastState + " -> " + curState + "; ");
                sb.append("interruptFlag: " + lastInterrupted + " -> " + curInterrupt + "; ");
                sb.append("liveStackTop: ");
                StackTraceElement[] stack = target.getStackTrace();
                if (stack.length > 0) {
                    sb.append(stack[0].toString());
                } else {
                    sb.append("<no stack>");
                }
                System.out.println(sb);
                lastState = curState;
                lastInterrupted = curInterrupt;
            }
            if (curState == Thread.State.TERMINATED) {
                stop();
            }
        }

        public void stop() {
            stopped = true;
            poller.shutdownNow();
        }
    }

    // 用来制造 synchronized BLOCKED 场景
    static class SyncHolder {
        public synchronized void hold(long millis) {
            try {
                Thread.sleep(millis); // 保持锁一段时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public synchronized void waitInside() throws InterruptedException {
            wait(); // 进入 WAITING，释放锁
        }

        public synchronized void notifyInside() {
            notify();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==== 线程生命周期详细演示 ====");

        // 1. NEW -> start() -> RUNNABLE 例子
        Thread simple = new Thread(() -> {
            System.out.println("[simple] run() 开始, 执行一些操作");
        }, "SimpleThread");
        DetailedTracker trackerSimple = new DetailedTracker(simple, 30);
        trackerSimple.start();
        System.out.println("[main] simple 状态(新建): " + simple.getState()); // NEW
        simple.start(); // 触发创建 OS 线程
        simple.join(); // 等待 finish
        Thread.sleep(100); // 观察 TERMINATED
        trackerSimple.stop();
        System.out.println();

        // 2. BLOCKED 场景：一个线程持有锁，另一个等待 -> STATE 从 RUNNABLE 到 BLOCKED 再回 RUNNABLE
        SyncHolder holder = new SyncHolder();
        Thread locker = new Thread(() -> {
            System.out.println("[locker] 获取锁并睡眠 500ms -> 模拟占用 synchronized");
            holder.hold(500);
            System.out.println("[locker] 释放锁");
        }, "LockerThread");
        Thread blocked = new Thread(() -> {
            System.out.println("[blocked] 尝试进入 synchronized（会被 BLOCKED）");
            holder.hold(100); // 竞争锁
            System.out.println("[blocked] 成功获得锁后立即释放");
        }, "BlockedThread");
        DetailedTracker tLocker = new DetailedTracker(locker, 30);
        DetailedTracker tBlocked = new DetailedTracker(blocked, 30);
        tLocker.start(); tBlocked.start();
        locker.start();
        Thread.sleep(50); // 让 locker 抢到锁先执行
        blocked.start();
        locker.join(); blocked.join();
        Thread.sleep(100);
        tLocker.stop(); tBlocked.stop();
        System.out.println();

        // 3. WAITING / TIMED_WAITING + notify 场景 + 中断
        Object waitObj = new Object();
        Thread waiter = new Thread(() -> {
            synchronized (waitObj) {
                try {
                    System.out.println("[waiter] 进入 wait 无超时 -> WAITING");
                    waitObj.wait(); // WAITING
                    System.out.println("[waiter] 被 notify 唤醒, 继续执行");
                } catch (InterruptedException e) {
                    System.out.println("[waiter] wait 时被中断, 进入 catch, 自行恢复中断");
                    Thread.currentThread().interrupt();
                }
            }
        }, "WaiterThread");
        DetailedTracker tWaiter = new DetailedTracker(waiter, 30);
        tWaiter.start();
        waiter.start();
        Thread.sleep(100); // 保证 waiter 进入 WAITING
        Thread notifier = new Thread(() -> {
            synchronized (waitObj) {
                System.out.println("[notifier] 进入 synchronized, 调用 notify，让 waiter 变回 RUNNABLE");
                waitObj.notify();
            }
        }, "NotifierThread");
        notifier.start();
        notifier.join();
        waiter.join();
        Thread.sleep(100);
        tWaiter.stop();
        System.out.println();

        // 4. sleep -> TIMED_WAITING, 被 interrupt 打断，状态与标志变化
        Thread sleeper = new Thread(() -> {
            try {
                System.out.println("[sleeper] 开始 sleep 1000ms -> TIMED_WAITING");
                Thread.sleep(1000); // TIMED_WAITING
                System.out.println("[sleeper] 睡眠自然结束");
            } catch (InterruptedException e) {
                System.out.println("[sleeper] sleep 中被中断, InterruptedException 抛出，线程恢复中断状态后退出");
                Thread.currentThread().interrupt();
            }
        }, "SleeperThread");
        DetailedTracker tSleeper = new DetailedTracker(sleeper, 30);
        tSleeper.start();
        sleeper.start();
        Thread.sleep(200); // 让它进入 sleep
        System.out.println("[main] 中断 sleeper");
        sleeper.interrupt(); // 触发 InterruptedException
        sleeper.join();
        Thread.sleep(100);
        tSleeper.stop();
        System.out.println();

        // 5. join 限时 / 无限 + interrupt
        Thread joinTarget = new Thread(() -> {
            try {
                System.out.println("[joinTarget] 睡眠 500ms");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "JoinTarget");
        Thread joiner = new Thread(() -> {
            try {
                System.out.println("[joiner] 等待 joinTarget 完成（无超时） -> WAITING");
                joinTarget.join(); // WAITING
                System.out.println("[joiner] join 结束，继续");
            } catch (InterruptedException e) {
                System.out.println("[joiner] join 时被中断");
                Thread.currentThread().interrupt();
            }
        }, "JoinerThread");
        DetailedTracker tJoiner = new DetailedTracker(joiner, 30);
        DetailedTracker tJoinTarget = new DetailedTracker(joinTarget, 30);
        tJoiner.start(); tJoinTarget.start();
        joinTarget.start();
        Thread.sleep(50); // 让 joinTarget 睡眠
        joiner.start();
        Thread.sleep(100); // 稍等
        System.out.println("[main] 中断 joiner（它在 joinTarget 上等待）");
        joiner.interrupt();
        joinTarget.join();
        joiner.join();
        Thread.sleep(100);
        tJoiner.stop(); tJoinTarget.stop();
        System.out.println();

        // 6. LockSupport.park/unpark + 中断示例
        Thread parker = new Thread(() -> {
            System.out.println("[parker] 调用 LockSupport.park() -> WAITING");
            LockSupport.park(); // WAITING
            if (Thread.interrupted()) { // park 会返回，但需要显式检查中断
                System.out.println("[parker] 因中断返回, 中断标志已清除 (interrupted() 清除)");
            } else {
                System.out.println("[parker] 被 unpark 唤醒");
            }
        }, "ParkerThread");
        DetailedTracker tParker = new DetailedTracker(parker, 30);
        tParker.start();
        parker.start();
        Thread.sleep(100);
        System.out.println("[main] 中断 parker（在 park 上）");
        parker.interrupt(); // 让 park 返回
        parker.join();
        Thread.sleep(50);
        tParker.stop();
        System.out.println();

        // 7. ExecutorService + Future.cancel(true) 中断 + 状态
        ExecutorService exec = Executors.newSingleThreadExecutor(r -> new Thread(r, "PoolWorker"));
        Future<?> task = exec.submit(() -> {
            try {
                System.out.println("[pool] 开始长任务: sleep 1000ms");
                Thread.sleep(1000); // TIMED_WAITING
                System.out.println("[pool] 长任务完成");
            } catch (InterruptedException e) {
                System.out.println("[pool] 被 cancel(true) 触发的中断, 捕获 InterruptedException");
                Thread.currentThread().interrupt();
            }
        });
        // Future 任务无法直接用 DetailedTracker（不是裸线程），但我们可以获取底层线程名字用于观察日志。
        Thread.sleep(100); // 让任务进入 sleep
        System.out.println("[main] 调用 cancel(true) 来中断 pool 任务");
        task.cancel(true); // 给执行线程发 interrupt
        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println();

        // 8. 复合场景：父线程创建子线程并 propagation interrupt
        Thread child = new Thread(() -> {
            try {
                System.out.println("[child] 开始 sleep 1000ms");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("[child] sleep 时被中断, 恢复并退出");
                Thread.currentThread().interrupt();
            }
        }, "ChildThread");
        Thread parent = new Thread(() -> {
            System.out.println("[parent] 启动 child 并等待它 join");
            child.start();
            try {
                child.join();
            } catch (InterruptedException e) {
                System.out.println("[parent] join child 时被中断, 传递 interrupt 给 child");
                child.interrupt();
                Thread.currentThread().interrupt();
            }
        }, "ParentThread");
        DetailedTracker tParent = new DetailedTracker(parent, 40);
        DetailedTracker tChild = new DetailedTracker(child, 40);
        tParent.start(); tChild.start();
        parent.start();
        Thread.sleep(100); // 中断 parent 过程
        System.out.println("[main] 中断 parent（它正在 join child）");
        parent.interrupt();
        parent.join(); child.join();
        Thread.sleep(100);
        tParent.stop(); tChild.stop();

        System.out.println("==== 演示结束 ====");
    }
}
