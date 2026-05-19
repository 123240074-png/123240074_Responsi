/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.pbo.responsi;

import com.pbo.responsi.dto.CartItemDTO;
import com.pbo.responsi.model.CartRepository;
//import com.pbo.responsi.model.FakeCartRepository; tidak dipakai
import com.pbo.responsi.service.DiscountStrategy;
//import com.pbo.responsi.service.NoDiscountStrategy; tidak dipakai 
import com.pbo.responsi.view.CartView;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.pbo.responsi.config.DbConfig;
import com.pbo.responsi.model.MySQLCartRepo;
import com.pbo.responsi.service.EventDiscountStrategy;
import java.sql.Connection;

/**
 *
 * @author farhannivta
 */
public class Responsi {

    public static void main(String[] args) {
        Connection connection;
        try {
            connection=DbConfig.getConnection();
        } catch (Exception e){
            JOptionPane.showMessageDialog(null,
                    "Gagal terhubung ke database MySQL!\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CartRepository repository = new MySQLCartRepo(connection);
        DiscountStrategy discountStrategy = new EventDiscountStrategy();

        CartView view = new CartView();

        // Runnable untuk Sinkronisasi Data Model ke View (Refresh)
        Runnable refreshView = () -> {
            List<CartItemDTO> items = repository.findAll();
            
            // Hitung logika billing
            double subtotal = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
            double discountAmount = discountStrategy.calculateDiscount(subtotal);
            double grandTotal = subtotal - discountAmount;
            
            // Kirim data mentah hasil kalkulasi ke view bodoh
            view.showCartItems(items, subtotal, discountAmount, grandTotal, discountStrategy.getDiscountName());
        };

        // --- BINDING EVENT LISTENERS (LOGIKA MVP) ---

        // 1. Aksi Tambah Item
        view.onAdd(e -> {
            String name = view.getNameInput();
            String priceStr = view.getPriceInput();
            String qtyStr = view.getQtyInput();

            if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                view.showMessage("Semua field input wajib diisi!");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int qty = Integer.parseInt(qtyStr);

                repository.save(new CartItemDTO(name, price, qty));
                refreshView.run();
                view.clearForm();
            } catch (NumberFormatException ex) {
                view.showMessage("Harga dan Qty harus berupa angka!");
            }
        });

        // 2. Aksi Ubah Qty
        view.onUpdate(e -> {
            String selectedName = view.getSelectedRowItemName();
            if (selectedName == null) {
                view.showMessage("Pilih barang di tabel terlebih dahulu!");
                return;
            }

            String inputStr = JOptionPane.showInputDialog(view, "Masukkan Jumlah (Qty) Baru untuk " + selectedName + ":");
            if (inputStr != null) {
                try {
                    int newQty = Integer.parseInt(inputStr);
                    repository.updateQuantity(selectedName, newQty);
                    refreshView.run();
                    view.clearForm();
                } catch (NumberFormatException ex) {
                    view.showMessage("Qty harus diisi angka!");
                }
            }
        });

        // 3. Aksi Hapus Item
        view.onDelete(e -> {
            String selectedName = view.getSelectedRowItemName();
            if (selectedName == null) {
                view.showMessage("Pilih barang di tabel terlebih dahulu!");
                return;
            }

            repository.delete(selectedName);
            refreshView.run();
            view.clearForm();
        });

        // 4. Aksi Pilih Baris Tabel (Mengisi Form Otomatis jika baris di-klik)
        view.onTableSelect(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = view.getSelectedRowItemName();
                if (selectedName == null) return;

                // Cari data item dari repository berdasarkan nama yang di-klik
                CartItemDTO targetItem = repository.findAll().stream()
                        .filter(item -> item.getName().equals(selectedName))
                        .findFirst()
                        .orElse(null);

                if (targetItem != null) {
                    view.setForm(targetItem.getName(), targetItem.getPrice(), targetItem.getQuantity());
                }
            }
        });

        // Render awal tabel saat program pertama kali dibuka
        SwingUtilities.invokeLater(refreshView::run);
    }
}
