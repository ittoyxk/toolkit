package com.xk.resource;

import com.xk.httpclient.common.util.PropertiesUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Created by xiaokang on 2017-05-13.
 */
public class ConnPool {
    private static InitialContext jndiCtx = null;
    private static DataSource ds = null;

    public static ConnPool getInstance()
    {
        return LazyHolder.INSTANCE;
    }

    public void init()
    {
        try {
            if (ds == null) {
                jndiCtx = new InitialContext();
                ds = (DataSource) jndiCtx.lookup(PropertiesUtil.getProperty("application.properties","jdbc_datasource"));
            }
        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("ConnPool:" + e);
        }
    }

    public ConnPool()
    {
        getInstance();
    }

    public ConnPool(String flag)
    {
        init();
    }

    public Connection createConnection()
    {
        if (ds == null) init();
        Connection conn = null;
        try {
            conn = ds.getConnection();

            return conn;
        } catch (Exception e) {
            e =
                    e;

            System.out.println("Error getting JNDI context ");
            e.printStackTrace();

        } finally {
        }
        return conn;
    }

    private static class LazyHolder {
        private static final ConnPool INSTANCE = new ConnPool("init");
    }
}
