package com.xk.toolkit;

import com.xk.batch.DisruptorExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xiaokang on 2017-09-13.
 */
public class DD
{
public static void main(String[] args){
    DisruptorExecutor d=new DisruptorExecutor();
    Future<String> dd=d.submit(new aa());
    try {
        dd.get(1999, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
        e.printStackTrace();
    } catch (ExecutionException e) {
        e.printStackTrace();
    } catch (TimeoutException e) {
        e.printStackTrace();
    }
}
}
