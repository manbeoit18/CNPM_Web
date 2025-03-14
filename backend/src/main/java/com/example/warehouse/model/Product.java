package com.example.warehouse.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "MayTinh")
@Data
public class Product {
    @Id
    private String maMay;
    private String tenMay;
    private int soLuong;
    private String tenCpu;
    private String ram;
    private String cardManHinh;
    private double gia;
    private String mainBoard;
    private Integer congSuatNguon;
    private String loaiMay;
    private String rom;
    private Double kichThuocMan;
    private String dungLuongPin;
    private String xuatXu;
    private Integer trangThai;
}