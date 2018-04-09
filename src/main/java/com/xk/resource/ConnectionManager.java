package com.xk.resource;

import com.xk.httpclient.common.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

public class ConnectionManager {
    private InitialContext jndiCtx = null;
    private DataSource     ds      = null;
    private static Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private String         datasourceName;

    public ConnectionManager(String _datasourceName) {
        this.datasourceName = "java:comp/env/" + _datasourceName;
        try {
            jndiCtx = new InitialContext();
            ds = (DataSource) jndiCtx.lookup(this.datasourceName);
        }
        catch (Exception e) {

            logger.error("getting JNDI context Exception ");
            e.printStackTrace();
        }
    }

    public ConnectionManager() {
        this.datasourceName =  PropertiesUtil.getProperty("application.properties","jdbc_datasource");
        try {
            jndiCtx = new InitialContext();
            ds = (DataSource) jndiCtx.lookup(this.datasourceName);
        }
        catch (Exception e) {

            logger.error("getting JNDI context Exception");
            e.printStackTrace();
        }
    }

    public Connection createConnection() throws Exception {
        Connection conn = null;
        try {
            // 注意在部署描述文件和部署计划文件中对jdbc/InsureDB进行定义
            conn = ds.getConnection();
            return conn;
        }
        catch (Exception e) {

            logger.error("getting Connectioin Exception");
            e.printStackTrace();
            throw e;
        }

    }
}
