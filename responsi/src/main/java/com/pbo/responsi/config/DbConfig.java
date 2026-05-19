package com.pbo.responsi.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {
    
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/responsi74pbo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=TRUE";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
            
            private DbConfig(){}
            
            public static Connection getConnection() throws SQLException {
                return DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
            }
}