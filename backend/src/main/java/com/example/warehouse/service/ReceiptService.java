package com.example.warehouse.service;

import com.example.warehouse.model.Account;
import com.example.warehouse.model.Product;
import com.example.warehouse.model.Receipt;
import com.example.warehouse.model.ReceiptDetail;
import com.example.warehouse.entity.NhaCungCap;
import com.example.warehouse.repository.AccountRepository;
import com.example.warehouse.repository.ProductRepository;
import com.example.warehouse.repository.ReceiptRepository;
import com.example.warehouse.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Receipt findById(Long id) {
        return receiptRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<Receipt> findAll() {
        List<Receipt> receipts = receiptRepository.findAll();
        receipts.forEach(receipt -> {
            // Ép Hibernate tải các mối quan hệ liên quan trong cùng phiên
            if (receipt.getNhaCungCap() != null) {
                receipt.getNhaCungCap().getMaNhaCungCap(); // Force loading NhaCungCap
            }
            if (receipt.getNguoiTao() != null) {
                receipt.getNguoiTao().getUserName(); // Force loading NguoiTao
            }
            if (receipt.getChiTietPhieuNhaps() != null) {
                receipt.getChiTietPhieuNhaps().size(); // Force loading ChiTietPhieuNhaps
                receipt.getChiTietPhieuNhaps().forEach(detail -> {
                    if (detail.getSanPham() != null) {
                        detail.getSanPham().getMaSanPham(); // Force loading SanPham trong ChiTietPhieuNhap
                    }
                });
            }
            logger.info("Receipt: " + receipt.getMaPhieuNhap() +
                    ", NhaCungCap: "
                    + (receipt.getNhaCungCap() != null ? receipt.getNhaCungCap().getMaNhaCungCap() : "null") +
                    ", NguoiTao: " + (receipt.getNguoiTao() != null ? receipt.getNguoiTao().getUserName() : "null"));
        });
        return receipts;
    }

    @Transactional
    public Receipt save(Receipt receipt) {
        logger.info("Dữ liệu phiếu nhập nhận được: {}", receipt);

        // Kiểm tra và gán nguoiTao
        if (receipt.getNguoiTao() != null && receipt.getNguoiTao().getUserName() != null) {
            Account nguoiTao = accountRepository.findById(receipt.getNguoiTao().getUserName())
                    .orElseThrow(() -> new RuntimeException(
                            "Người tạo không tồn tại: " + receipt.getNguoiTao().getUserName()));
            receipt.setNguoiTao(nguoiTao);
        } else {
            logger.error("Người tạo không hợp lệ: {}", receipt.getNguoiTao());
            throw new IllegalArgumentException("Người tạo là bắt buộc cho phiếu nhập.");
        }

        // Kiểm tra và gán nhaCungCap
        if (receipt.getNhaCungCap() != null && receipt.getNhaCungCap().getMaNhaCungCap() != null) {
            NhaCungCap nhaCungCap = supplierRepository.findById(receipt.getNhaCungCap().getMaNhaCungCap())
                    .orElseThrow(() -> new RuntimeException(
                            "Nhà cung cấp không tồn tại: " + receipt.getNhaCungCap().getMaNhaCungCap()));
            receipt.setNhaCungCap(nhaCungCap);
        } else {
            logger.error("Nhà cung cấp không hợp lệ: {}", receipt.getNhaCungCap());
            throw new IllegalArgumentException("Nhà cung cấp là bắt buộc cho phiếu nhập.");
        }

        // Kiểm tra chiTietPhieuNhaps
        List<ReceiptDetail> details = (receipt.getChiTietPhieuNhaps() != null)
                ? new ArrayList<>(receipt.getChiTietPhieuNhaps())
                : new ArrayList<>();

        // Kiểm tra logic nghiệp vụ: phiếu nhập phải có ít nhất một chi tiết
        if (details.isEmpty()) {
            logger.error("Danh sách chi tiết phiếu nhập rỗng: {}", receipt.getChiTietPhieuNhaps());
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một chi tiết phiếu nhập.");
        }

        // Kiểm tra từng chi tiết phiếu nhập
        for (ReceiptDetail detail : details) {
            if (detail.getId() == null || detail.getId().getMaSanPham() == null) {
                logger.error("Chi tiết phiếu nhập không hợp lệ, thiếu mã sản phẩm: {}", detail);
                throw new IllegalArgumentException("Chi tiết phiếu nhập không hợp lệ: thiếu mã sản phẩm.");
            }
            if (detail.getSanPham() == null || detail.getSanPham().getMaSanPham() == null) {
                logger.error("Sản phẩm trong chi tiết phiếu nhập không hợp lệ: {}", detail.getSanPham());
                throw new IllegalArgumentException("Sản phẩm trong chi tiết phiếu nhập không hợp lệ.");
            }
        }

        // Lưu Receipt trước để tạo maPhieuNhap
        receipt.setChiTietPhieuNhaps(null); // Tạm thời bỏ chiTietPhieuNhaps để tránh lỗi cascade
        Receipt savedReceipt = receiptRepository.save(receipt);

        // Gán lại chiTietPhieuNhaps và cập nhật maPhieuNhap
        for (ReceiptDetail detail : details) {
            // Gán maPhieuNhap vào ReceiptDetailId
            detail.getId().setMaPhieuNhap(savedReceipt.getMaPhieuNhap());
            detail.setReceipt(savedReceipt);

            // Tìm và gán Product
            Product product = productRepository.findById(detail.getId().getMaSanPham())
                    .orElseThrow(
                            () -> new RuntimeException("Sản phẩm không tồn tại: " + detail.getId().getMaSanPham()));
            detail.setSanPham(product);

            // Cập nhật số lượng sản phẩm
            product.setSoLuong(product.getSoLuong() + detail.getSoLuong());
            productRepository.save(product);
        }

        // Gán lại chiTietPhieuNhaps và lưu
        savedReceipt.setChiTietPhieuNhaps(details);
        Receipt finalReceipt = receiptRepository.save(savedReceipt);
        logger.info("Lưu phiếu nhập thành công: {}", finalReceipt);
        return finalReceipt;
    }

    @Transactional
    public void deleteById(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu nhập không tồn tại: " + id));
        // Cập nhật số lượng sản phẩm trước khi xóa
        for (ReceiptDetail detail : receipt.getChiTietPhieuNhaps()) {
            Product product = productRepository.findById(detail.getId().getMaSanPham())
                    .orElseThrow(
                            () -> new RuntimeException("Sản phẩm không tồn tại: " + detail.getId().getMaSanPham()));
            product.setSoLuong(product.getSoLuong() - detail.getSoLuong());
            productRepository.save(product);
        }
        receiptRepository.deleteById(id);
    }

    public List<Receipt> findByNguoiTaoUserName(String userName) {
        return receiptRepository.findByNguoiTaoUserName(userName);
    }

    public List<Receipt> findByNgayNhapBetween(LocalDateTime start, LocalDateTime end) {
        return receiptRepository.findByNgayNhapBetween(start, end);
    }

    public Double getTotalReceiptAmount() {
        Double total = receiptRepository.getTotalReceiptAmount();
        return total != null ? total : 0.0;
    }

    public Double getTotalReceiptAmountByNgayNhapBetween(LocalDateTime start, LocalDateTime end) {
        Double total = receiptRepository.getTotalReceiptAmountByNgayNhapBetween(start, end);
        return total != null ? total : 0.0;
    }
}