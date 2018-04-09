package com.xk.resource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.SQLNestedException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressWarnings("deprecation")
public class Datasource extends BasicDataSource{
	public ConnectionFactory createConnectionFactory() throws SQLNestedException{
		Class<?> driverFromCCL = null;
		if (this.driverClassName != null) {
			try {
				try {
					if (this.driverClassLoader == null)
						Class.forName(this.driverClassName);
					else
						Class.forName(this.driverClassName, true,
								this.driverClassLoader);
				} catch (ClassNotFoundException cnfe) {
					driverFromCCL = Thread.currentThread()
							.getContextClassLoader()
							.loadClass(this.driverClassName);
				}
			} catch (Throwable t) {
				String message = new StringBuilder()
						.append("Cannot load JDBC driver class '")
						.append(this.driverClassName).append("'").toString();

				this.logWriter.println(message);
				t.printStackTrace(this.logWriter);
				throw new SQLNestedException(message, t);
			}
		}
		Driver driver = null;
		try {
			if (driverFromCCL == null) {
				driver = DriverManager.getDriver(this.url);
			} else {
				driver = (Driver) driverFromCCL.newInstance();
				if (!(driver.acceptsURL(this.url)))
					throw new SQLException("No suitable driver", "08001");
			}
		} catch (Throwable t) {
			String message = new StringBuilder()
					.append("Cannot create JDBC driver of class '")
					.append((this.driverClassName != null) ? this.driverClassName
							: "").append("' for connect URL '")
					.append(this.url).append("'").toString();

			this.logWriter.println(message);
			t.printStackTrace(this.logWriter);
			throw new SQLNestedException(message, t);
		}

		if (this.validationQuery == null) {
			setTestOnBorrow(false);
			setTestOnReturn(false);
			setTestWhileIdle(false);
		}

		String user = this.username;
		if (user != null)
			this.connectionProperties.put("user", user);
		else {
			log("DBCP DataSource configured without a 'username'");
		}

		String pwd = dec(this.password);
		if (pwd != null)
			this.connectionProperties.put("password", pwd);
		else {
			log("DBCP DataSource configured without a 'password'");
		}

		ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(
				driver, this.url, this.connectionProperties);
		return driverConnectionFactory;
	}
	private byte[] stringToByte(String str){
		str = str.replace("A", "D").replace("B", "D").replace("C", "D").replace("E", "-").replace("F", "-");
		String[] keys = str.split("D");
		byte[] b = new byte[keys.length];
		for(int i=0;i<b.length;i++){
			b[i] = Byte.valueOf(keys[i]);
		}
		return b;
	}
	public String dec(String content){   
		try{
			String sk = "80B"+"78C"+"F116CE"+"111AE74"+"AF98D6BF55"+"BF30AF75"+"D76CF123B"+"6AF37CF"+"123AF40";
			byte[] data =stringToByte(content);
			Key key =  new SecretKeySpec(stringToByte(sk), "A"+"ES");   
			Cipher cipher = Cipher.getInstance("A"+"ES/E"+"CB/P"+"KCS"+"5Pa"+"dding");   
			cipher.init(Cipher.DECRYPT_MODE, key);  
			return new String(cipher.doFinal(data)); 
		}catch(Exception e){
			e.printStackTrace();
		}
		return content;
	}
}
