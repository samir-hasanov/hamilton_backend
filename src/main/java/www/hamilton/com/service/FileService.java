package www.hamilton.com.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;

    @Value("${hamilton.minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    public void uploadLog(String bucketName, String fileName, String logContent) {
        try {
            ensureBucket(bucketName);
            InputStream inputStream = new ByteArrayInputStream(logContent.getBytes(StandardCharsets.UTF_8));
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, logContent.length(), -1)
                            .contentType("application/json")
                            .build()
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("MinIO-ya fayl yüklənmədi: " + e.getMessage(), e);
        }
    }

    /**
     * Faylı bucket-ə yükləyir və brauzer/API üçün tam URL qaytarır (path-style: endpoint/bucket/object).
     */
    public String uploadToMinio(MultipartFile file, String bucket) throws Exception {
        try {
            ensureBucket(bucket);
            String safeName = sanitizeFileName(file.getOriginalFilename());
            String objectName = UUID.randomUUID() + "_" + safeName;
            String contentType = resolveContentType(file);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            return buildPublicObjectUrl(bucket, objectName);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw minioFailure("MinIO yükləmə", e);
        }
    }

    /**
     * DB-də saxlanılan dəyər tam URL və ya (köhnə) yalnız obyekt açarı ola bilər.
     */
    public Optional<byte[]> getBytesForProfileImage(String stored, String defaultBucket) {
        if (stored == null || stored.isBlank()) {
            return Optional.empty();
        }
        Optional<BucketObject> parsed = parsePublicUrl(stored);
        if (parsed.isPresent()) {
            BucketObject bo = parsed.get();
            return getObjectBytes(bo.bucket(), bo.objectKey());
        }
        return getObjectBytes(defaultBucket, stored);
    }

    public void deleteProfileImage(String stored, String defaultBucket) {
        if (stored == null || stored.isBlank()) {
            return;
        }
        parsePublicUrl(stored).ifPresentOrElse(
                bo -> deleteObject(bo.bucket(), bo.objectKey()),
                () -> deleteObject(defaultBucket, stored)
        );
    }

    public void deleteFileFromMinio(String fileUrl, String bucket) throws Exception {
        parsePublicUrl(fileUrl).ifPresentOrElse(
                bo -> deleteObject(bo.bucket(), bo.objectKey()),
                () -> {
                    String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                    deleteObject(bucket, fileName);
                }
        );
    }

    public void ensureBucket(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO bucket yaradıldı: {}", bucketName);
            }
        } catch (Exception e) {
            throw minioFailure("MinIO bucket əməliyyatı", e);
        }
    }

    private Optional<byte[]> getObjectBytes(String bucket, String objectName) {
        try (InputStream in = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        )) {
            return Optional.of(in.readAllBytes());
        } catch (Exception e) {
            log.warn("MinIO getObject uğursuz: bucket={}, object={}, {}", bucket, objectName, e.getMessage());
            return Optional.empty();
        }
    }

    private void deleteObject(String bucket, String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.warn("MinIO remove uğursuz: bucket={}, object={}, {}", bucket, objectName, e.getMessage());
        }
    }

    private String buildPublicObjectUrl(String bucketName, String objectName) {
        String base = minioEndpoint.endsWith("/")
                ? minioEndpoint.substring(0, minioEndpoint.length() - 1)
                : minioEndpoint;
        return base + "/" + bucketName + "/" + objectName;
    }

    private static Optional<BucketObject> parsePublicUrl(String url) {
        if (url == null) {
            return Optional.empty();
        }
        String t = url.trim();
        if (!t.startsWith("http://") && !t.startsWith("https://")) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(t);
            String path = uri.getPath();
            if (path == null || path.isEmpty() || "/".equals(path)) {
                return Optional.empty();
            }
            List<String> segments = new ArrayList<>();
            for (String p : path.split("/")) {
                if (!p.isEmpty()) {
                    segments.add(p);
                }
            }
            if (segments.size() < 2) {
                return Optional.empty();
            }
            String bucket = segments.get(0);
            String objectKey = String.join("/", segments.subList(1, segments.size()));
            return Optional.of(new BucketObject(bucket, objectKey));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private record BucketObject(String bucket, String objectKey) {
    }

    private static String sanitizeFileName(String original) {
        if (original == null || original.isBlank()) {
            return "file";
        }
        String base = original.substring(original.lastIndexOf('/') + 1);
        int bs = base.lastIndexOf('\\');
        if (bs >= 0) {
            base = base.substring(bs + 1);
        }
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String resolveContentType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct != null && !ct.isBlank()) {
            return ct;
        }
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            String ext = name.substring(name.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            return switch (ext) {
                case ".png" -> "image/png";
                case ".jpg", ".jpeg" -> "image/jpeg";
                case ".webp" -> "image/webp";
                default -> "application/octet-stream";
            };
        }
        return "application/octet-stream";
    }

    private static RuntimeException minioFailure(String context, Exception e) {
        String raw = e.getMessage() != null ? e.getMessage() : e.toString();
        if (raw.contains("Access Key")
                || raw.contains("does not exist in our records")
                || raw.contains("InvalidAccessKeyId")
                || raw.contains("SignatureDoesNotMatch")) {
            return new RuntimeException(
                    "MinIO giriş uğursuz: hamilton.minio.access-key / secret-key "
                            + "MinIO root ilə eyni olmalıdır (tez-tez minioadmin / minioadmin). "
                            + "[MinIO: " + raw + "]",
                    e
            );
        }
        return new RuntimeException(context + ": " + raw, e);
    }
}
