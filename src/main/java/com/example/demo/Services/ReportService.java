package com.example.demo.Services;

import com.example.demo.Data.Order;
import com.example.demo.Data.OrderItem;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    public ByteArrayInputStream generateOrdersReport(List<Order> orders) throws IOException {
        XWPFDocument document = new XWPFDocument();

        // Заголовок
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("ОТЧЁТ ПО ЗАКАЗАМ");
        titleRun.setBold(true);
        titleRun.setFontSize(20);
        titleRun.addBreak();

        // Дата
        XWPFParagraph datePara = document.createParagraph();
        datePara.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun dateRun = datePara.createRun();
        dateRun.setText("Дата формирования: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        dateRun.addBreak();
        dateRun.addBreak();

        // Статистика
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();
        long newOrders = orders.stream().filter(o -> "NEW".equals(o.getStatus())).count();
        long shippedOrders = orders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count();
        long deliveredOrders = orders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count();
        long cancelledOrders = orders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

        XWPFParagraph stats = document.createParagraph();
        XWPFRun statsRun = stats.createRun();
        statsRun.setText("ОБЩАЯ СТАТИСТИКА:");
        statsRun.setBold(true);
        statsRun.addBreak();
        statsRun.setText("Всего заказов: " + totalOrders);
        statsRun.addBreak();
        statsRun.setText("Общая выручка: " + totalRevenue + " руб.");
        statsRun.addBreak();
        statsRun.setText("Новые заказы: " + newOrders);
        statsRun.addBreak();
        statsRun.setText("В обработке: " + shippedOrders);
        statsRun.addBreak();
        statsRun.setText("Доставлено: " + deliveredOrders);
        statsRun.addBreak();
        statsRun.setText("Отменено: " + cancelledOrders);
        statsRun.addBreak();
        statsRun.addBreak();

        // Таблица заказов
        XWPFTable table = document.createTable(orders.size() + 1, 6);
        table.setWidth("100%");

        // Заголовки таблицы
        String[] headers = {"№", "Дата", "Клиент", "Телефон", "Сумма", "Статус"};
        XWPFTableRow headerRow = table.getRow(0);
        for (int i = 0; i < headers.length; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            XWPFParagraph p = cell.getParagraphs().get(0);
            XWPFRun run = p.createRun();
            run.setText(headers[i]);
            run.setBold(true);
        }

        // Данные
        int rowNum = 1;
        for (Order order : orders) {
            XWPFTableRow row = table.getRow(rowNum);
            if (row == null) row = table.createRow();

            row.getCell(0).setText(String.valueOf(order.getId()));
            row.getCell(1).setText(order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            row.getCell(2).setText(order.getCustomerName());
            row.getCell(3).setText(order.getCustomerPhone());
            row.getCell(4).setText(order.getTotalCost().toString() + " руб.");
            row.getCell(5).setText(getStatusRu(order.getStatus()));

            rowNum++;
        }

        // Подпись
        XWPFParagraph signature = document.createParagraph();
        signature.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun sigRun = signature.createRun();
        sigRun.addBreak();
        sigRun.addBreak();
        sigRun.setText("Руководитель: ___________________");
        sigRun.addBreak();
        sigRun.setText("Главный бухгалтер: ___________________");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String getStatusRu(String status) {
        switch (status) {
            case "NEW": return "Новый";
            case "SHIPPED": return "Отправлен";
            case "DELIVERED": return "Доставлен";
            case "CANCELLED": return "Отменён";
            default: return status;
        }
    }
}