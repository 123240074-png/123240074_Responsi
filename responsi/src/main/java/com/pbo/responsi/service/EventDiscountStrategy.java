/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pbo.responsi.service;

/**
 *
 * @author Lab Informatika
 */
public class EventDiscountStrategy implements DiscountStrategy {
    
    @Override 
    public double calculateDiscount(double totalAmount) {
        return totalAmount*0.12;
    }
    
    @Override
    public String getDiscountName(){
        return "Event 12.12(12%)";
    }
}
