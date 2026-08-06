package com.errorwiky.post;

import com.errorwiky.common.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public FileStorageService(
            S3Client s3Client,
            @Value("${cloud.aws.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public List<AttachmentEntity> store(
            PostEntity post,
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        if (files.size() > 5) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "첨부파일은 최대 5개입니다."
            );
        }

        List<AttachmentEntity> result = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalName = Optional
                    .ofNullable(file.getOriginalFilename())
                    .orElse("file");

            String safeName = Paths.get(originalName)
                    .getFileName()
                    .toString()
                    .replaceAll(
                            "[^a-zA-Z0-9가-힣._-]",
                            "_"
                    );

            /*
             * S3 내부 저장 경로입니다.
             *
             * 실제 폴더가 생성되는 것이 아니라
             * 객체 Key가 posts/UUID_파일명 형태로 저장됩니다.
             */
            String storedName =
                    "posts/" + UUID.randomUUID() + "_" + safeName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storedName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            try (InputStream inputStream = file.getInputStream()) {

                s3Client.putObject(
                        request,
                        RequestBody.fromInputStream(
                                inputStream,
                                file.getSize()
                        )
                );

            } catch (IOException | S3Exception e) {
                throw new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "첨부파일 S3 저장에 실패했습니다."
                );
            }

            /*
             * 기존 storedName 컬럼에는
             * 로컬 파일명이 아닌 S3 객체 Key를 저장합니다.
             */
            result.add(
                    new AttachmentEntity(
                            post,
                            safeName,
                            storedName,
                            file.getContentType(),
                            file.getSize()
                    )
            );
        }

        return result;
    }

    public Resource load(String storedName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(storedName)
                    .build();

            ResponseBytes<GetObjectResponse> response =
                    s3Client.getObjectAsBytes(request);

            return new ByteArrayResource(response.asByteArray()) {
                @Override
                public String getFilename() {
                    return Paths.get(storedName)
                            .getFileName()
                            .toString();
                }
            };

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "첨부파일을 찾을 수 없습니다."
                );
            }

            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "첨부파일을 불러오는 중 오류가 발생했습니다."
            );
        }
    }

    public void deleteFiles(List<AttachmentEntity> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (AttachmentEntity file : files) {
            try {
                DeleteObjectRequest request =
                        DeleteObjectRequest.builder()
                                .bucket(bucket)
                                .key(file.getStoredName())
                                .build();

                s3Client.deleteObject(request);

            } catch (S3Exception ignored) {
                /*
                 * 기존 코드와 마찬가지로
                 * 파일 삭제 실패가 게시글 삭제 전체를
                 * 실패시키지는 않도록 처리했습니다.
                 *
                 * 실무에서는 로그를 남기는 편이 좋습니다.
                 */
            }
        }
    }
}