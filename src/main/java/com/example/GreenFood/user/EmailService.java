package com.example.GreenFood.user;

import com.example.GreenFood.model.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderInvoice(Orders order) {
        String to = order.getCustomer().getAccount();
        String subject = "Hóa đơn đơn hàng #" + order.getId() + " - GreenFood";
        String text = "Xin chào " + order.getCustomer().getName() + ",\n\n" +
                "Cảm ơn bạn đã đặt hàng tại GreenFood.\n" +
                "Mã đơn hàng: #" + order.getId() + "\n" +
                "Trạng thái: " + order.getStatus() + "\n" +
                "Tổng cộng: " + order.getTotalAmount() + "đ\n\n" +
                "Đơn hàng của bạn đã hoàn thành.\n" +
                "Trân trọng,\nGreenFood Team.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
