package com.example.demo.service;

import com.example.demo.dto.Dtos.*;
import com.example.demo.entity.*;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.CustomerOrderRepository;
import com.example.demo.repository.ProductVariantRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final CustomerOrderRepository orders;
    private final UserRepository users;
    private final ProductVariantRepository variants;

    // ---------- Khách hàng ----------
    public OrderDto create(String email, OrderRequest r) {
        if (!List.of("bank", "cod").contains(r.paymentMethod()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không hợp lệ");
        User user = user(email);

        CustomerOrder o = new CustomerOrder();
        o.setUser(user);
        ShippingRequest s = r.shipping();
        o.setShipName(s.name().trim());
        o.setShipPhone(s.phone().trim());
        o.setShipAddress(s.address().trim());
        o.setShipNote(s.note() == null ? "" : s.note().trim());
        o.setPaymentMethod(r.paymentMethod());

        long total = 0;
        for (ItemRequest it : r.items()) {
            ProductVariant v = variants.findByIdForUpdate(it.variantId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Sản phẩm không tồn tại"));
            Product p = v.getProduct();
            if (!p.isActive())
                throw new ApiException(HttpStatus.BAD_REQUEST, "\"" + p.getName() + "\" đã ngừng bán");
            if (v.getStock() < it.qty())
                throw new ApiException(HttpStatus.BAD_REQUEST, "\"" + p.getName() + "\" không đủ hàng");

            v.setStock(v.getStock() - it.qty());

            OrderItem oi = new OrderItem();
            oi.setOrder(o);
            oi.setVariantId(v.getId());
            oi.setProductName(p.getName());
            oi.setImageUrl(p.getImageUrl());
            oi.setSizeMl(v.getSizeMl());
            oi.setUnitPrice(v.getPrice());      // giá lấy từ DB, không tin client
            oi.setQty(it.qty());
            o.getItems().add(oi);

            total += (long) v.getPrice() * it.qty();
        }
        o.setTotal(total);
        o.setOrderCode(pickCode(r.orderCode()));
        return toDto(orders.save(o));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> mine(String email) {
        return orders.findByUserOrderByCreatedAtDesc(user(email)).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OrderDto detail(String email, String code) {
        return toDto(find(email, code));
    }

    public OrderDto cancel(String email, String code) {
        CustomerOrder o = find(email, code);
        if (!"pending".equals(o.getStatus()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ hủy được đơn đang chờ xác nhận");
        changeStatus(o, "cancelled");
        return toDto(o);
    }

    // ---------- Admin (dùng ở Giai đoạn 7) ----------
    @Transactional(readOnly = true)
    public List<OrderDto> all() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    public OrderDto updateStatus(String code, String status) {
        if (!List.of("pending", "confirmed", "shipping", "completed", "cancelled").contains(status))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        CustomerOrder o = orders.findByOrderCode(code)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
        changeStatus(o, status);
        return toDto(o);
    }

    // ---------- Hàm phụ ----------
    private void changeStatus(CustomerOrder o, String status) {
        if ("cancelled".equals(status) && !"cancelled".equals(o.getStatus())) {   // hoàn kho khi hủy
            for (OrderItem i : o.getItems())
                variants.findByIdForUpdate(i.getVariantId())
                        .ifPresent(v -> v.setStock(v.getStock() + i.getQty()));
        }
        o.setStatus(status);
    }

    private User user(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"));
    }

    private CustomerOrder find(String email, String code) {
        return orders.findByOrderCodeAndUser(code, user(email))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));
    }

    // Giữ mã PFxxxxxx mà checkout.html đã dùng làm nội dung QR (nếu hợp lệ và chưa trùng)
    private String pickCode(String wanted) {
        if (wanted != null && wanted.matches("PF\\d{6}") && orders.findByOrderCode(wanted).isEmpty())
            return wanted;
        String c;
        do {
            c = String.format("PF%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        } while (orders.findByOrderCode(c).isPresent());
        return c;
    }

    private OrderDto toDto(CustomerOrder o) {
        return new OrderDto(o.getOrderCode(), o.getTotal(), o.getStatus(), o.getPaymentMethod(),
                new ShippingRequest(o.getShipName(), o.getShipPhone(), o.getShipAddress(), o.getShipNote()),
                o.getCreatedAt(), o.getUser().getEmail(),
                o.getItems().stream().map(i -> new OrderItemDto(
                        i.getProductName(), i.getImageUrl(), i.getSizeMl(),
                        i.getUnitPrice(), i.getQty())).toList());
    }
}