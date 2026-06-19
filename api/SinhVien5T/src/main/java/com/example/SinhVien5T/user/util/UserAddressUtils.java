package com.example.SinhVien5T.user.util;

import com.example.SinhVien5T.user.entity.AddressType;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.entity.UserAddress;

public final class UserAddressUtils {

    private UserAddressUtils() {
    }

    public static UserAddress findAddress(User user, AddressType addressType) {
        if (user.getAddresses() == null) {
            return null;
        }

        return user.getAddresses().stream()
                .filter(address -> addressType.equals(address.getAddressType()))
                .findFirst()
                .orElse(null);
    }
}
