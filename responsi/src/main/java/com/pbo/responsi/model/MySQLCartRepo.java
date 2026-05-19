package com.pbo.responsi.model;

import com.pbo.responsi.dto.CartItemDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLCartRepo implements CartRepository {
    private final Connection connection;
    
    public MySQLCartRepo(Connection connection) {
        this.connection=connection;
        initTable();
    }
    
    private void initTable() {
        String ddl="CREATE TABLE IF NOT EXISTS cart_items("
                + "name VARCHAR(255) PRIMARY KEY,"
                + "price DOUBLE NOT NULL,"
                + "quantity INT NOT NULL)";
        try (Statement stmt=connection.createStatement()){
            stmt.execute(ddl);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal membuat tabel: "+e.getMessage(),e);
        }
    }
    
    @Override
    public List<CartItemDTO>findAll() {
    List<CartItemDTO>items=new ArrayList<>();
    try (Statement stmt=connection.createStatement();
            ResultSet rs=stmt.executeQuery("SELECT name,price,quantity FROM cart_items")) {
                while (rs.next()) {
                    items.add(new CartItemDTO(rs.getString("name"),rs.getDouble("price"),rs.getInt("quantity")));
                }
            }catch (SQLException e){
                    throw new RuntimeException("Gagal mengambil data:"+e.getMessage(),e);
                    }
            return items;
}
    
    @Override
    public void save(CartItemDTO item) {
        String sql= "INSERT INTO cart_items(name,price,quantity)VALUES(?,?,?)"+
                "ON DUPLICATE KEY UPDATE price=VALUES(price),quantity=VALUES(quantity)";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1,item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan item."+e.getMessage(),e);
        }
    }
    
    @Override
    public void updateQuantity(String name,int newQty) {
        try (PreparedStatement ps=connection.prepareStatement(
                "UPDATE cart_items SET quantity = ? WHERE name=?")){
            ps.setInt(1, newQty);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update qty"+e.getMessage(),e);
        }
    }
    
    @Override
    public void delete(String name) {
        try (PreparedStatement ps=connection.prepareStatement(
                "DELETE FROM cart_items WHERE name=?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus item: "+e.getMessage(),e);
        }
    }
}
