package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.SubmissionFileCreateDTO;
import org.example.backend.modules.edu.dto.SubmissionFilePageQueryDTO;
import org.example.backend.modules.edu.dto.SubmissionFileUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.SubmissionFile;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.example.backend.modules.edu.service.SubmissionFileService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submission-files")
public class SubmissionFileController {

    private final SubmissionFileService submissionFileService;
    private final StageSubmissionService stageSubmissionService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final ProjectGroupService projectGroupService;
    private final SysUserService sysUserService;

    @Value("${app.upload.base-dir}")
    private String uploadBaseDir;

    @Value("${app.upload.access-url-prefix:/uploads/}")
    private String uploadAccessUrlPrefix;

    @Value("${app.upload.max-file-size:10485760}")
    private long uploadMaxFileSize;

    @Value("${app.upload.allowed-extensions:pdf,doc,docx,xls,xlsx,ppt,pptx,zip,rar,7z,txt,md,png,jpg,jpeg,webp}")
    private String allowedExtensionsConfig;

    public SubmissionFileController(SubmissionFileService submissionFileService,
                                    StageSubmissionService stageSubmissionService,
                                    ProjectGroupMemberService projectGroupMemberService,
                                    ProjectGroupService projectGroupService,
                                    SysUserService sysUserService) {
        this.submissionFileService = submissionFileService;
        this.stageSubmissionService = stageSubmissionService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.projectGroupService = projectGroupService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<SubmissionFile>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<SubmissionFile> wrapper = Wrappers.lambdaQuery();
        applySubmissionFileViewScope(wrapper, currentUser);
        wrapper.orderByDesc(SubmissionFile::getCreateTime);
        return Result.success(submissionFileService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<SubmissionFile>> page(@Valid @ModelAttribute SubmissionFilePageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        Page<SubmissionFile> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SubmissionFile> wrapper = Wrappers.lambdaQuery();

        if (query.getSubmissionId() != null) {
            wrapper.eq(SubmissionFile::getSubmissionId, query.getSubmissionId());
        }
        if (StringUtils.hasText(query.getFileType())) {
            wrapper.eq(SubmissionFile::getFileType, query.getFileType());
        }
        if (StringUtils.hasText(query.getBizType())) {
            wrapper.eq(SubmissionFile::getBizType, query.getBizType());
        }
        if (query.getUploadUserId() != null) {
            wrapper.eq(SubmissionFile::getUploadUserId, query.getUploadUserId());
        }

        applySubmissionFileViewScope(wrapper, currentUser);
        wrapper.orderByDesc(SubmissionFile::getCreateTime);
        return Result.success(submissionFileService.page(page, wrapper));
    }

    @PostMapping("/upload")
    public Result<SubmissionFile> upload(@RequestParam Long submissionId,
                                         @RequestParam(required = false) String bizType,
                                         @RequestParam("file") MultipartFile file) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "上传文件不能为空");
        }

        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : null;
        if (!StringUtils.hasText(originalName)) {
            return Result.fail(400, "文件名不能为空");
        }
        String extension = StringUtils.getFilenameExtension(originalName);
        if (!StringUtils.hasText(extension)) {
            return Result.fail(400, "文件扩展名不能为空");
        }
        extension = extension.toLowerCase(Locale.ROOT);
        if (!getAllowedExtensions().contains(extension)) {
            return Result.fail(400, "当前文件类型不允许上传：" + extension);
        }
        if (file.getSize() > uploadMaxFileSize) {
            return Result.fail(400, "文件大小超过限制，单文件不能超过" + uploadMaxFileSize + "字节");
        }

        StageSubmission stageSubmission = stageSubmissionService.getById(submissionId);
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (!canAccessSubmission(currentUser, stageSubmission)) {
            return Result.fail(403, "当前用户无权为该阶段提交上传文件");
        }

        String fileName = UUID.randomUUID() + "." + extension;
        String relativePath = Paths.get("submission-files", String.valueOf(submissionId), fileName).toString().replace('\\', '/');
        Path targetPath = Paths.get(uploadBaseDir, relativePath);

        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException e) {
            return Result.fail(500, "文件保存失败：" + e.getMessage());
        }

        SubmissionFile submissionFile = new SubmissionFile();
        submissionFile.setSubmissionId(submissionId);
        submissionFile.setFileName(fileName);
        submissionFile.setOriginalName(originalName);
        submissionFile.setFileType(extension.toUpperCase(Locale.ROOT));
        submissionFile.setFileSize(file.getSize());
        submissionFile.setFilePath(relativePath);
        submissionFile.setFileUrl(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(normalizeAccessPrefix())
                .path(relativePath)
                .toUriString());
        submissionFile.setBizType(bizType);
        submissionFile.setUploadUserId(currentUser.getUserId());
        try {
            submissionFileService.save(submissionFile);
        } catch (Exception e) {
            deletePhysicalFile(submissionFile);
            return Result.fail(500, "文件记录保存失败：" + e.getMessage());
        }
        return Result.success(submissionFile);
    }

    @PostMapping
    public Result<SubmissionFile> create(@Valid @RequestBody SubmissionFileCreateDTO dto) {
        return Result.fail(400, "当前版本不支持手工创建文件记录，请使用上传接口");
    }

    @GetMapping("/{id}")
    public Result<SubmissionFile> findById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        SubmissionFile submissionFile = submissionFileService.getById(id);
        if (submissionFile == null) {
            return Result.fail(404, "提交文件不存在");
        }
        StageSubmission stageSubmission = stageSubmissionService.getById(submissionFile.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "提交文件关联的阶段提交不存在");
        }
        if (!canViewSubmission(currentUser, stageSubmission)) {
            return Result.fail(403, "当前用户无权查看该提交文件");
        }
        return Result.success(submissionFile);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) {
        return buildFileResponse(id, false);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable Long id) {
        return buildFileResponse(id, true);
    }

    @PutMapping
    public Result<SubmissionFile> update(@Valid @RequestBody SubmissionFileUpdateDTO dto) {
        return Result.fail(400, "当前版本不支持手工修改文件记录，请通过重新上传或删除后重传处理");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        SubmissionFile existing = submissionFileService.getById(id);
        if (existing == null) {
            return Result.fail(404, "提交文件不存在");
        }
        if (!canManageSubmissionFile(currentUser, existing)) {
            return Result.fail(403, "当前用户无权删除该提交文件");
        }
        boolean removed = submissionFileService.removeById(id);
        if (removed) {
            deletePhysicalFile(existing);
        }
        return Result.success(removed);
    }

    private void applySubmissionFileViewScope(LambdaQueryWrapper<SubmissionFile> wrapper,
                                              LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        List<Long> submissionIds = getViewableSubmissionIds(currentUser);
        if (submissionIds.isEmpty()) {
            wrapper.eq(SubmissionFile::getId, -1L);
            return;
        }
        wrapper.in(SubmissionFile::getSubmissionId, submissionIds);
    }

    private List<Long> getViewableSubmissionIds(LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> groupIds = projectGroupService.list(Wrappers.<ProjectGroup>lambdaQuery()
                    .eq(ProjectGroup::getTeacherId, currentUser.getUserId()))
                    .stream().map(ProjectGroup::getId).collect(Collectors.toList());
            if (groupIds.isEmpty()) {
                return List.of();
            }
            return stageSubmissionService.list(Wrappers.<StageSubmission>lambdaQuery()
                    .in(StageSubmission::getGroupId, groupIds))
                    .stream().map(StageSubmission::getId).collect(Collectors.toList());
        }
        List<Long> groupIds = projectGroupMemberService.list(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getUserId, currentUser.getUserId()))
                .stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toList());
        LambdaQueryWrapper<StageSubmission> wrapper = Wrappers.<StageSubmission>lambdaQuery()
                .eq(StageSubmission::getSubmitterId, currentUser.getUserId());
        if (!groupIds.isEmpty()) {
            wrapper.or(w -> w.in(StageSubmission::getGroupId, groupIds));
        }
        return stageSubmissionService.list(wrapper).stream().map(StageSubmission::getId).collect(Collectors.toList());
    }

    private boolean canViewSubmission(LoginUserPrincipal currentUser, StageSubmission stageSubmission) {
        if (currentUser == null || currentUser.getUserId() == null || stageSubmission == null) {
            return false;
        }
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (currentUser.getUserId().equals(stageSubmission.getSubmitterId())) {
            return true;
        }
        if (hasRole(currentUser, "TEACHER")) {
            ProjectGroup projectGroup = projectGroupService.getById(stageSubmission.getGroupId());
            return projectGroup != null && currentUser.getUserId().equals(projectGroup.getTeacherId());
        }
        return isGroupMember(stageSubmission.getGroupId(), currentUser.getUserId());
    }

    private boolean canAccessSubmission(LoginUserPrincipal currentUser, StageSubmission stageSubmission) {
        if (currentUser == null || currentUser.getUserId() == null || stageSubmission == null) {
            return false;
        }
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (currentUser.getUserId().equals(stageSubmission.getSubmitterId())) {
            return true;
        }
        return isGroupMember(stageSubmission.getGroupId(), currentUser.getUserId());
    }

    private boolean canManageSubmissionFile(LoginUserPrincipal currentUser, SubmissionFile submissionFile) {
        if (currentUser == null || currentUser.getUserId() == null || submissionFile == null) {
            return false;
        }
        return hasRole(currentUser, "ADMIN")
                || currentUser.getUserId().equals(submissionFile.getUploadUserId());
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        return projectGroupMemberService.count(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getGroupId, groupId)
                .eq(ProjectGroupMember::getUserId, userId)) > 0;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }

    private String normalizeAccessPrefix() {
        return uploadAccessUrlPrefix.endsWith("/") ? uploadAccessUrlPrefix : uploadAccessUrlPrefix + "/";
    }

    private Set<String> getAllowedExtensions() {
        return Arrays.stream(allowedExtensionsConfig.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private ResponseEntity<?> buildFileResponse(Long id, boolean inline) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Result.fail(401, "当前未登录"));
        }
        SubmissionFile submissionFile = submissionFileService.getById(id);
        if (submissionFile == null) {
            return ResponseEntity.status(404).body(Result.fail(404, "提交文件不存在"));
        }
        StageSubmission stageSubmission = stageSubmissionService.getById(submissionFile.getSubmissionId());
        if (stageSubmission == null) {
            return ResponseEntity.status(404).body(Result.fail(404, "提交文件关联的阶段提交不存在"));
        }
        if (!canViewSubmission(currentUser, stageSubmission)) {
            return ResponseEntity.status(403).body(Result.fail(403, "当前用户无权访问该提交文件"));
        }
        Path path = resolvePhysicalPath(submissionFile);
        if (path == null) {
            return ResponseEntity.status(400).body(Result.fail(400, "文件路径非法"));
        }
        if (!Files.exists(path) || Files.isDirectory(path)) {
            return ResponseEntity.status(404).body(Result.fail(404, "物理文件不存在"));
        }
        try {
            String filename = StringUtils.hasText(submissionFile.getOriginalName()) ? submissionFile.getOriginalName() : submissionFile.getFileName();
            ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                    .filename(filename, StandardCharsets.UTF_8)
                    .build();
            return ResponseEntity.ok()
                    .contentType(resolveMediaType(path))
                    .contentLength(Files.size(path))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(new InputStreamResource(Files.newInputStream(path)));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Result.fail(500, "文件读取失败：" + e.getMessage()));
        }
    }

    private Path resolvePhysicalPath(SubmissionFile submissionFile) {
        if (submissionFile == null || !StringUtils.hasText(submissionFile.getFilePath())) {
            return null;
        }
        Path basePath = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
        Path relativePath = Paths.get(submissionFile.getFilePath()).normalize();
        if (relativePath.isAbsolute()) {
            return null;
        }
        Path resolvedPath = basePath.resolve(relativePath).normalize();
        return resolvedPath.startsWith(basePath) ? resolvedPath : null;
    }

    private MediaType resolveMediaType(Path path) {
        try {
            String contentType = Files.probeContentType(path);
            return StringUtils.hasText(contentType) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private void deletePhysicalFile(SubmissionFile submissionFile) {
        if (submissionFile == null || !StringUtils.hasText(submissionFile.getFilePath())) {
            return;
        }
        try {
            Path path = resolvePhysicalPath(submissionFile);
            if (path != null) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
        }
    }
}