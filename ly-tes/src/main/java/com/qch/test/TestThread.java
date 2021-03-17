package com.qch.test;

import java.util.concurrent.*;

public class TestThread {
    /**
     * 1.Thread类
     * 2.runnable接口
     * 3.callable + future<>
     * 4.线程池
     */
    public static ExecutorService executor = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main.........start");
        /**
         * JDK1.8后引入了CompletableFuture 它有两个异步执行的2方法，分别是runAsync、supplyAsync不同的是后者需要有返回值
         * 这个方法需要传入两个参数，一个是业务逻辑，一个是线程池。所以他是专门用来处理线程池的异步操作
         */
//        CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果是：" + i);
//        }, executor);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果是：" + i);
            return i;
        }, executor);
        Integer integer = future.get();
        System.out.println("main.........end" + integer);
    }
    public static void main1(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main.........start");
//        1.使用Thread类创建线程：
//                可以发现 这是一个异步的过程，主函数执行完之后才执行这个线程（amin方法也是一个线程）
//        Thread01 thread = new Thread01();
//        thread.start();

//        Runnable02 runnable02 = new Runnable02();  创建一个线程使用Runnable接口
//        new Thread(runnable02).start();

//        callable不同与runnable，他是有返回值的，那么我们可以用FutureTask来接受callable的返回参数
        Callable03 callable03 = new Callable03();
        FutureTask<Integer> integerFutureTask = new FutureTask<>(callable03);
        new Thread(integerFutureTask).start();
//        等到整个线程执行完才获取返回结果
        Integer integer = integerFutureTask.get();
        System.out.println("main.........end" + integer);
    }

}

 class Thread01  extends  Thread{
     @Override
     public void run() {
         System.out.println("当前线程："+ Thread.currentThread().getId());
         int i = 10/2;
         System.out.println("运行结果是："+i);
     }
 }
 class Runnable02 implements Runnable{

     @Override
     public void run() {
         System.out.println("当前线程："+ Thread.currentThread().getId());
         int i = 10/2;
         System.out.println("运行结果是："+i);
     }
 }
class Callable03 implements Callable<Integer> {


    @Override
    public Integer call() throws Exception {
        System.out.println("当前线程："+ Thread.currentThread().getId());
        int i = 10/2;
        System.out.println("运行结果是："+i);
        return i;
    }
}

