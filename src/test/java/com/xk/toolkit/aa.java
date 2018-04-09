package com.xk.toolkit;

import java.util.concurrent.Callable;

/**
 * Created by xiaokang on 2017-09-13.
 */
public class aa implements Callable<String> {
    @Override
    public String call() throws Exception
    {
        return "c";
    }
}
