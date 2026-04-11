package com.petshop.petshop.service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service xử lý tích hợp thanh toán VNPay.
 * Port chính xác từ sortObject() + generateVnpayUrl() của dự án PET (Node.js).
 *
 * Luồng Node.js gốc:
 * 1. Tạo vnp_Params (raw)
 * 2. vnp_Params = sortObject(vnp_Params) → encode key & value, sort theo key
 * 3. signData = querystring.stringify(vnp_Params, {encode: false}) → nối encoded pairs
 * 4. HMAC-SHA512(signData) → signed
 * 5. URL = vnpUrl + '?' + querystring.stringify(vnp_Params, {encode: false}) + '&vnp_SecureHash=' + signed
 */
@Service
public class VnPayService {

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    /**
     * Tạo URL thanh toán VNPay.
     * Match chính xác logic Node.js: sortObject → stringify → HMAC → URL
     */
    public String generatePaymentUrl(int orderId, long amount, String ipAddr) {
        // Bước 1: Tạo raw params (giống Node.js)
        Map<String, String> rawParams = new HashMap<>();
        rawParams.put("vnp_Version", "2.1.0");
        rawParams.put("vnp_Command", "pay");
        rawParams.put("vnp_TmnCode", vnpTmnCode);
        rawParams.put("vnp_Locale", "vn");
        rawParams.put("vnp_CurrCode", "VND");
        rawParams.put("vnp_TxnRef", String.valueOf(orderId));
        rawParams.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        rawParams.put("vnp_OrderType", "other");
        rawParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
        rawParams.put("vnp_ReturnUrl", vnpReturnUrl);
        rawParams.put("vnp_IpAddr", ipAddr);

        // Tạo vnp_CreateDate
        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(tz);
        rawParams.put("vnp_CreateDate", sdf.format(new Date()));

        // Bước 2: sortObject — encode key + value, sort theo encoded key
        // Giống hệt hàm sortObject() trong Node.js:
        //   encodeURIComponent(key) → sort → encodeURIComponent(value).replace(/%20/g, '+')
        Map<String, String> sortedEncoded = sortObject(rawParams);

        // Bước 3: querystring.stringify(vnp_Params, {encode: false})
        // = nối các cặp encoded_key=encoded_value bằng "&"
        String signData = buildQueryString(sortedEncoded);

        // Bước 4: HMAC-SHA512
        String signed = hmacSHA512(vnpHashSecret, signData);

        // Bước 5: Tạo URL cuối cùng
        // URL = vnpUrl + '?' + signData + '&vnp_SecureHash=' + signed
        return vnpPayUrl + "?" + signData + "&vnp_SecureHash=" + signed;
    }

    /**
     * Xác thực chữ ký từ VNPay callback (return URL).
     * Khi VNPay redirect về, Spring đã tự decode các query params.
     * Nên cần encode lại trước khi verify (giống sortObject).
     */
    public boolean validateSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) {
            return false;
        }

        // Loại bỏ vnp_SecureHash và vnp_SecureHashType
        Map<String, String> rawParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                rawParams.put(entry.getKey(), entry.getValue());
            }
        }

        // sortObject lại → stringify → HMAC → so sánh
        Map<String, String> sortedEncoded = sortObject(rawParams);
        String signData = buildQueryString(sortedEncoded);
        String computed = hmacSHA512(vnpHashSecret, signData);

        return computed.equalsIgnoreCase(receivedHash);
    }

    /**
     * Port chính xác hàm sortObject() từ Node.js.
     *
     * function sortObject(obj) {
     *   let sorted = {};
     *   let str = [];
     *   for (key in obj) str.push(encodeURIComponent(key));
     *   str.sort();
     *   for (key of str) sorted[key] = encodeURIComponent(obj[key]).replace(/%20/g, '+');
     *   return sorted;
     * }
     *
     * JavaScript encodeURIComponent + replace %20→+ ≈ Java URLEncoder.encode (UTF-8)
     */
    private Map<String, String> sortObject(Map<String, String> rawParams) {
        // TreeMap tự sort theo key
        Map<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : rawParams.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            sorted.put(encodedKey, encodedValue);
        }
        return sorted;
    }

    /**
     * Tạo query string từ map đã encode (giống querystring.stringify với encode: false).
     * Kết quả: key1=val1&key2=val2&...
     */
    private String buildQueryString(Map<String, String> sortedEncoded) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedEncoded.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Hàm tạo chữ ký HMAC-SHA512.
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký HMAC-SHA512", e);
        }
    }
}
