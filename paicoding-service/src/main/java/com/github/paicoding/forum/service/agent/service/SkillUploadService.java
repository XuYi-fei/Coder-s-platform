package com.github.paicoding.forum.service.agent.service;

import com.github.paicoding.forum.api.model.vo.agent.SkillConfigReqVO;
import com.github.paicoding.forum.service.image.oss.ImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles skill zip upload:
 * 1. Accept a zip file (containing SKILL.md + optional resources/)
 * 2. Extract and parse the SKILL.md YAML frontmatter
 * 3. Store the zip in OSS via ImageUploader
 * 4. Return a populated SkillConfigReqVO for DB persistence
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillUploadService {

    private final ImageUploader imageUploader;

    /**
     * Parse the uploaded skill zip and build a SkillConfigReqVO.
     * The zip must contain a SKILL.md at its root (or one level deep).
     */
    public SkillUploadResult processUpload(MultipartFile file) throws IOException {
        byte[] zipBytes = file.getBytes();

        // 1. Extract SKILL.md from zip
        String skillMdContent = extractSkillMd(zipBytes);
        if (skillMdContent == null) {
            throw new IllegalArgumentException("上传的 zip 中未找到 SKILL.md 文件");
        }

        // 2. Parse YAML frontmatter
        Map<String, String> frontmatter = parseFrontmatter(skillMdContent);

        // 3. Upload zip to OSS
        String ossUrl = imageUploader.upload(new ByteArrayInputStream(zipBytes), "zip");

        // 4. Build request VO
        SkillConfigReqVO req = new SkillConfigReqVO();
        req.setSkillType("FILE_BASED");
        req.setName(frontmatter.getOrDefault("name",
                file.getOriginalFilename() != null
                        ? file.getOriginalFilename().replaceAll("\\.zip$", "")
                        : "unnamed_skill"));
        req.setDisplayName(frontmatter.getOrDefault("display_name",
                frontmatter.getOrDefault("name", req.getName())));
        req.setDescription(frontmatter.getOrDefault("description", ""));
        req.setContent(extractBodyAfterFrontmatter(skillMdContent));
        req.setVisibility(3); // PUBLIC by default

        SkillUploadResult result = new SkillUploadResult();
        result.setReqVO(req);
        result.setSkillMd(skillMdContent);
        result.setOssUrl(ossUrl);
        return result;
    }

    /** Locate SKILL.md inside the zip (root or one directory deep). */
    private String extractSkillMd(byte[] zipBytes) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                // Accept SKILL.md at root or one directory deep (e.g. skill-name/SKILL.md)
                if (!entry.isDirectory() && (name.equals("SKILL.md")
                        || name.matches("[^/]+/SKILL\\.md"))) {
                    return readEntryAsString(zis);
                }
                zis.closeEntry();
            }
        }
        return null;
    }

    private String readEntryAsString(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int len;
        while ((len = zis.read(chunk)) != -1) {
            buffer.write(chunk, 0, len);
        }
        return buffer.toString("UTF-8");
    }

    /**
     * Parse YAML frontmatter from a markdown file.
     * Frontmatter is the block between the first and second {@code ---} markers.
     * Only simple "key: value" lines are supported (no nested structures).
     */
    private Map<String, String> parseFrontmatter(String content) {
        Map<String, String> result = new LinkedHashMap<>();
        if (!content.startsWith("---")) {
            return result;
        }
        int start = content.indexOf('\n') + 1;
        int end = content.indexOf("\n---", start);
        if (end < 0) return result;

        String yamlBlock = content.substring(start, end);
        try (BufferedReader reader = new BufferedReader(new StringReader(yamlBlock))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int colon = line.indexOf(':');
                if (colon <= 0) continue;
                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();
                // Strip surrounding quotes if present
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                result.put(key, value);
            }
        } catch (IOException e) {
            log.warn("Frontmatter parse error", e);
        }
        return result;
    }

    /** Return the markdown body that follows the closing --- of the frontmatter. */
    private String extractBodyAfterFrontmatter(String content) {
        if (!content.startsWith("---")) return content;
        int end = content.indexOf("\n---", content.indexOf('\n') + 1);
        if (end < 0) return content;
        int bodyStart = content.indexOf('\n', end + 1);
        return bodyStart >= 0 ? content.substring(bodyStart + 1).trim() : "";
    }

    /** Carries parsed metadata back to the controller. */
    public static class SkillUploadResult {
        private SkillConfigReqVO reqVO;
        private String skillMd;
        private String ossUrl;

        public SkillConfigReqVO getReqVO() { return reqVO; }
        public void setReqVO(SkillConfigReqVO reqVO) { this.reqVO = reqVO; }
        public String getSkillMd() { return skillMd; }
        public void setSkillMd(String skillMd) { this.skillMd = skillMd; }
        public String getOssUrl() { return ossUrl; }
        public void setOssUrl(String ossUrl) { this.ossUrl = ossUrl; }
    }
}
