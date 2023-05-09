package com.kyobo.platform.donots.common.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kyobo.platform.donots.common.exception.BusinessException;
import com.kyobo.platform.donots.config.AWSConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

@Component
@Slf4j
public class S3FileUploadUtil {

    private String[] PERMITTED_UPLOAD_EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif"};

    private String backendImageBucket;
    private String distributionDomain;
    private AmazonS3 amazonS3;

    private Environment environment;
    private Properties properties;

    public S3FileUploadUtil(Environment environment) throws IOException {
        this.environment = environment;

        properties = loadProperty();
        distributionDomain = properties.getProperty("distributionDomain");
        backendImageBucket = properties.getProperty("backendImageBucket");
        amazonS3 = AWSConfig.amazonS3(properties.getProperty("cloud.aws.region.static"));
    }

    public Properties loadProperty() throws IOException {
        log.info("S3FileUploadUtil.loadProperty");

        if (environment == null)
            throw new BusinessException("S3FileUploadUtil.environment field is failed to be injected. (의존성 주입 실패)");

        String profileToUseProperties;
        if (environment.getActiveProfiles().length == 0)
            profileToUseProperties = "dev";  // Active Profile이 없을 경우(default일 경우) 개발계 구성을 사용한다. 추후 로컬 구성파일이 별도로 필요할 경우 변경한다.
        else
            profileToUseProperties = environment.getActiveProfiles()[0];

        Properties properties = new Properties();
        Resource newResource = new ClassPathResource("awsAuth-" + profileToUseProperties + ".properties");
        log.info("Currently using [awsAuth-{}.properties]", profileToUseProperties);
        BufferedReader br = new BufferedReader(new InputStreamReader(newResource.getInputStream()));
        properties.load(br);

        return properties;
    }

    public String uploadImageToS3AndGetUrl(MultipartFile multipartFile, String asIsImageUrl, String imageDirectoryPathAfterDomain) throws IOException, DecoderException {

        // macOS에서 업로드시 생기는 자소분리현상을 해결하기 위한 로직
        String normalizedMultipartFilename = Normalizer.normalize(multipartFile.getOriginalFilename(), Normalizer.Form.NFC);

        // 파일명, 확장자 분리 및 확장자 허용여부 확인
        String nameWithoutExtension = normalizedMultipartFilename.replaceFirst("[.][^.]+$", ""); // 파일명에서 확장자를 제거

        final String extension = normalizedMultipartFilename.substring(normalizedMultipartFilename.lastIndexOf(".")); // 확장자만 추출

        if (Arrays.stream(PERMITTED_UPLOAD_EXTENSIONS).noneMatch(ext -> ext.equals(extension))) {
            throw new BusinessException("허용되지 않은 확장자입니다. 허용된 확장자: " + Arrays.toString(PERMITTED_UPLOAD_EXTENSIONS));
        }

        // 파일명 유일성을 확보하기 위해 파일명과 확장자 사이에 업로드 시각 붙이기
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String toBeFileNameCurrentDateTimeAppended = nameWithoutExtension + "_" + now.format(formatter) + extension;

        // S3에 업로드할 파일URL을 파일명만 URL인코딩한다
        String encodedToBeProfilePictureUrl = buildFullUrlWithEncodedFileName(distributionDomain, imageDirectoryPathAfterDomain, toBeFileNameCurrentDateTimeAppended);
        if (!isUrlLengthAvailable(encodedToBeProfilePictureUrl)) {
            throw new BusinessException("업로드 하려는 파일명이 너무 깁니다");
        }

        File toBeFile = convertMultipartFileToFileWithCustomName(multipartFile, toBeFileNameCurrentDateTimeAppended);

        if (!StringUtils.isBlank(asIsImageUrl)) {
            String[] slashSplittedAsIsImageUrl = asIsImageUrl.split("/");
            String asIsImageFileName = slashSplittedAsIsImageUrl[slashSplittedAsIsImageUrl.length - 1];
            URLCodec uc = new URLCodec();
            String decodedAsIsImageFileName = uc.decode(asIsImageFileName, "UTF-8");
            amazonS3.deleteObject(backendImageBucket, imageDirectoryPathAfterDomain + decodedAsIsImageFileName);
        }

        // S3 업로드
        amazonS3.putObject(new PutObjectRequest(
                backendImageBucket, imageDirectoryPathAfterDomain + toBeFile.getName(), toBeFile));

        // 업로드 후 메모리에 있는 이미지파일 삭제
        toBeFile.delete();

        return encodedToBeProfilePictureUrl;
    }

    public void deleteImageFromS3(String asIsImageUrl, String imageDirectoryPathAfterDomain) throws IOException, DecoderException {

        // TODO 삭제 오류시 익셉션 정의
        // 최초 등록이 아닐 경우 기존 이미지를 삭제한다.
        // 엔티티의 profilePictureUrl 필드는 뒤에서 덮어씌울(set) 것이므로 굳이 여기서 삭제하지는 않는다.
        if (!StringUtils.isBlank(asIsImageUrl)) {
            // URL에서 파일명 추출
            String[] slashSplittedAsIsImageUrl = asIsImageUrl.split("/");
            String asIsImageFileName = slashSplittedAsIsImageUrl[slashSplittedAsIsImageUrl.length - 1];

            URLCodec uc = new URLCodec();
            String decodedAsIsImageFileName = uc.decode(asIsImageFileName, "UTF-8");
            amazonS3.deleteObject(backendImageBucket, imageDirectoryPathAfterDomain + decodedAsIsImageFileName);
        }
    }

    public static boolean isUrlLengthAvailable(String fullUrl) {

        // Internet Explorer에서 입력 가능한 최대 URL 길이. (GET Method 사용)
        // 다른 브라우저는 이것보다 더 길기 때문에 가장 짧은 Internet Explorer를 기준으로 했다.
        final int MAX_URL_LENGTH_INTERNET_EXPLORER = 2048;
        if (fullUrl.length() <= MAX_URL_LENGTH_INTERNET_EXPLORER)
            return true;
        else
            return false;
    }

    public static String buildFullUrlWithEncodedFileName(String domain, String fileDirectoryPathAfterDomain, String fileName) throws UnsupportedEncodingException {
        URLCodec uc = new URLCodec();
        String encodedFileName = uc.encode(fileName, "UTF-8");
        return "https://" + domain + "/" + fileDirectoryPathAfterDomain + encodedFileName;
    }

    private static File convertMultipartFileToFileWithCustomName(MultipartFile multipartFile, String customName) throws IOException {

        File fileWrittenFromMultipartFile = new File(customName);
        fileWrittenFromMultipartFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(fileWrittenFromMultipartFile);
        fos.write(multipartFile.getBytes());
        fos.close();

        return fileWrittenFromMultipartFile;
    }

    private static File convertToFile(MultipartFile multipartFile) throws IOException {
        File fileWrittenFromMultipartFile = new File(multipartFile.getOriginalFilename());
        fileWrittenFromMultipartFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(fileWrittenFromMultipartFile);
        fos.write(multipartFile.getBytes());
        fos.close();
        return fileWrittenFromMultipartFile;
    }
}
