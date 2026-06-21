package com.example.SinhVien5T.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCleanupScheduler {

    private final CloudinaryStorageService cloudinaryStorageService;

    public void deleteAfterCommit(String publicId, String resourceType) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteQuietly(publicId, resourceType);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteQuietly(publicId, resourceType);
            }
        });
    }

    public void deleteAfterRollback(String publicId, String resourceType) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    deleteQuietly(publicId, resourceType);
                }
            }
        });
    }

    private void deleteQuietly(String publicId, String resourceType) {
        try {
            cloudinaryStorageService.deleteAsset(publicId, resourceType);
        } catch (RuntimeException ex) {
            log.warn("Could not delete Cloudinary asset publicId={}, resourceType={}", publicId, resourceType, ex);
        }
    }
}
