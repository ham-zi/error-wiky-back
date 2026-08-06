package com.errorwiky.post;

import com.errorwiky.common.BusinessException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path root;
    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        try { root=Paths.get(uploadDir).toAbsolutePath().normalize(); Files.createDirectories(root); }
        catch(IOException e){ throw new IllegalStateException("업로드 폴더를 만들 수 없습니다.",e); }
    }
    public List<AttachmentEntity> store(PostEntity post,List<MultipartFile> files){
        if(files==null||files.isEmpty()) return List.of();
        if(files.size()>5) throw new BusinessException(HttpStatus.BAD_REQUEST,"첨부파일은 최대 5개입니다.");
        List<AttachmentEntity> result=new ArrayList<>();
        for(MultipartFile file:files){
            if(file==null||file.isEmpty()) continue;
            String original=Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String safe=Paths.get(original).getFileName().toString().replaceAll("[^a-zA-Z0-9가-힣._-]","_");
            String stored=UUID.randomUUID()+"_"+safe;
            try{ 
            	Files.copy(file.getInputStream(),root.resolve(stored),StandardCopyOption.REPLACE_EXISTING);
            } catch(IOException e){ 
            	throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,"첨부파일 저장에 실패했습니다.");
            }
            result.add(new AttachmentEntity(post,safe,stored,file.getContentType(),file.getSize()));
        }
        return result;
    }
    public Resource load(String storedName){
        try{ Resource r=new UrlResource(root.resolve(storedName).toUri()); if(r.exists()) return r; }
        catch(Exception ignored){}
        throw new BusinessException(HttpStatus.NOT_FOUND,"첨부파일을 찾을 수 없습니다.");
    }
    public void deleteFiles(List<AttachmentEntity> files){
        for(AttachmentEntity file:files){ try{Files.deleteIfExists(root.resolve(file.getStoredName()));}catch(IOException ignored){} }
    }
}
