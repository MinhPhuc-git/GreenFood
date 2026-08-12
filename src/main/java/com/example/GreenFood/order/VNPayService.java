package com.example.GreenFood.order;

import com.example.GreenFood.config.VNPayConfig;
import com.example.GreenFood.model.Orders;
import com.example.GreenFood.model.Payment;
import com.example.GreenFood.order.OrderRepository;
import com.example.GreenFood.order.PaymentRepository;
import com.example.GreenFood.loyalty.LoyaltyPointService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final LoyaltyPointService loyaltyPointService;

    public VNPayService(VNPayConfig vnPayConfig, OrderRepository orderRepository,
                        PaymentRepository paymentRepository, OrderService orderService,
                        LoyaltyPointService loyaltyPointService) {
        this.vnPayConfig       = vnPayConfig;
        this.orderRepository   = orderRepository;
        this.paymentRepository = paymentRepository;
        this.orderService      = orderService;
        this.loyaltyPointService = loyaltyPointService;
    }

    // ═══════════════════════════════════════════
    // TẠO URL THANH TOÁN VNPAY
    // ═══════════════════════════════════════════
    public String createPaymentUrl(int orderId, HttpServletRequest request) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Số tiền nhân 100 (VNPAY yêu cầu đơn vị VND * 100)
        long amount = order.getTotalAmount().longValue() * 100;

        String txnRef = orderId + "_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang " + orderId;
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String ipAddr = getClientIp(request);

        // Tham số gửi lên VNPAY (phải sort theo alphabet)
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version",    "2.1.0");
        params.put("vnp_Command",    "pay");
        params.put("vnp_TmnCode",    vnPayConfig.tmnCode);
        params.put("vnp_Amount",     String.valueOf(amount));
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_OrderInfo",  orderInfo);
        params.put("vnp_OrderType",  "other");
        params.put("vnp_Locale",     "vn");
        params.put("vnp_ReturnUrl",  vnPayConfig.returnUrl);
        params.put("vnp_IpAddr",     ipAddr);
        params.put("vnp_CreateDate", createDate);

        // Tạo chuỗi hash
        String hashData  = buildHashData(params);
        String secureHash = hmacSHA512(vnPayConfig.hashSecret, hashData);

        // Tạo URL cuối
        String queryString = buildQueryString(params);
        String paymentUrl  = vnPayConfig.paymentUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;

        // Lưu Payment với trạng thái PENDING
        savePaymentPending(order, txnRef);

        return paymentUrl;
    }

    // ═══════════════════════════════════════════
    // XỬ LÝ CALLBACK TỪ VNPAY (Return URL)
    // ═══════════════════════════════════════════
    public Map<String, String> processReturn(Map<String, String> params) {
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        // Xác thực chữ ký
        String hashData    = buildHashData(new TreeMap<>(params));
        String checkHash   = hmacSHA512(vnPayConfig.hashSecret, hashData);

        Map<String, String> result = new HashMap<>();

        if (!checkHash.equalsIgnoreCase(secureHash)) {
            result.put("status", "INVALID");
            result.put("message", "Chữ ký không hợp lệ");
            return result;
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef       = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");

        // Lấy orderId từ txnRef (format: orderId_timestamp)
        int orderId = Integer.parseInt(txnRef.split("_")[0]);
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            updatePayment(order, "SUCCESS", transactionNo);
            order.setStatus("PAID");
            orderRepository.save(order);
            loyaltyPointService.tryAwardPointsForOrder(orderId);

            result.put("status", "SUCCESS");
            result.put("message", "Thanh toán thành công!");
            result.put("orderId", String.valueOf(orderId));
        } else {
            // Thanh toán thất bại
            updatePayment(order, "FAILED", transactionNo);
            // Kích hoạt hàm hủy đơn hàng của Admin (không cần xét điều kiện) để hoàn kho
            try {
                orderService.adminCancelOrder(orderId);
            } catch (Exception e) {
                // Đã bị hủy trước đó hoặc lỗi khác
                order.setStatus("CANCELLED");
                orderRepository.save(order);
            }

            result.put("status", "FAILED");
            result.put("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
            result.put("orderId", String.valueOf(orderId));
        }

        return result;
    }

    // ═══════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════
    private void savePaymentPending(Orders order, String txnRef) {
        Payment payment = new Payment();
        payment.setOrders(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod("VNPAY");
        payment.setStatus("PENDING");
        payment.setTransactionCode(txnRef);
        paymentRepository.save(payment);
    }

    private void updatePayment(Orders order, String status, String transactionNo) {
        paymentRepository.findByOrders(order).ifPresent(payment -> {
            payment.setStatus(status);
            payment.setTransactionCode(transactionNo);
            paymentRepository.save(payment);
        });
    }

    private String buildHashData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (sb.length() > 0) sb.append("&");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (sb.length() > 0) sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký VNPAY", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
}