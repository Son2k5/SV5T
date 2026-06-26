package com.example.SinhVien5T.auth.exception;

public final class AuthErrorMessages {

    public static final String EMAIL_ALREADY_REGISTERED = "Email đã được đăng ký";
    public static final String INVALID_EMAIL_DOMAIN = "Vui lòng sử dụng email do nhà trường cấp (@ms.hanu.edu.vn)";
    public static final String INVALID_TOKEN = "Liên kết không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.";
    public static final String INVALID_CREDENTIALS = "Tài khoản hoặc mật khẩu không chính xác";
    public static final String ACCOUNT_NOT_FOUND = "Tài khoản không tồn tại";
    public static final String ACCOUNT_NOT_VERIFIED = "Tài khoản chưa xác minh email. Vui lòng kiểm tra email để xác minh tài khoản.";
    public static final String ACCOUNT_DISABLED = "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.";
    public static final String SESSION_EXPIRED = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";

    public static final String INVALID_SESSION = "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.";

    private AuthErrorMessages() {
    }
}
