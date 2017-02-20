package name.zjq.blog.pcd.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import name.zjq.blog.pcd.bean.CloudFile;
import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.utils.CoderUtil;
import name.zjq.blog.pcd.utils.StrUtil;

public class CloudFileService {
    public static final int DEFAULT_BUFFER_SIZE = 10485760;// 文件流读取默认缓存大小(默认10M)
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
    private static final Log LOGGER = LogFactory.getLog(CloudFileService.class);

    // 能够在线播放的文件类型
    private static final Set<String> CAN_PLAY_ONLINE_FILE_TYPES = new HashSet<String>();
    // 支持在线预览的文件类型
    private static final Set<String> CAN_PREVIEW_FILE_TYPES = new HashSet<String>();

    static {
        CAN_PLAY_ONLINE_FILE_TYPES.add("mp4");
        CAN_PLAY_ONLINE_FILE_TYPES.add("flv");

        CAN_PREVIEW_FILE_TYPES.add("bmp");
        CAN_PREVIEW_FILE_TYPES.add("jpg");
        CAN_PREVIEW_FILE_TYPES.add("gif");
        CAN_PREVIEW_FILE_TYPES.add("jpeg");
        CAN_PREVIEW_FILE_TYPES.add("png");
    }

    /**
     * 检查文件是否支持在线播放
     *
     * @param filetype
     * @return
     */
    public static boolean isCanPlayOnline(String filetype) {
        return CAN_PLAY_ONLINE_FILE_TYPES.contains(filetype);
    }

    /**
     * 检查文件是否支持预览
     *
     * @param filetype
     * @return
     */
    public static boolean isCanPreview(String filetype) {
        return CAN_PREVIEW_FILE_TYPES.contains(filetype);
    }

    /**
     * 获取文件列表
     *
     * @param filepath
     * @return
     * @throws CustomLogicException
     * @throws IOException
     */
    public static List<CloudFile> getFileList(String maindir, String filepath, String accepttype,
                                              final String filterfile) throws CustomLogicException, IOException {
        if (filterfile != null) {
            Path filterFilePath = Paths.get(filterfile);
            if (!Files.exists(filterFilePath, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
                throw new CustomLogicException(400, "指定的过滤文件不存在", null);
            }
        }
        List<CloudFile> filelsit = new ArrayList<CloudFile>();
        List<CloudFile> dirlsit = new ArrayList<CloudFile>();
        Files.walkFileTree(Paths.get(filepath), new HashSet<FileVisitOption>(), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (filterfile != null) {
                    String thisfilepath = file.toAbsolutePath().toString();
                    if (thisfilepath.indexOf(filterfile) > -1) {
                        return FileVisitResult.CONTINUE;
                    }
                }

                CloudFile df = new CloudFile(maindir, file, attrs);
                if (attrs.isDirectory()) {
                    dirlsit.add(df);
                } else {
                    filelsit.add(df);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (accepttype == null) {
            dirlsit.addAll(filelsit);
            return dirlsit;
        } else if (accepttype.equals("file")) {
            return filelsit;
        } else {
            return dirlsit;
        }
    }

    /**
     * 删除文件
     *
     * @param filepath
     * @return
     * @throws FileNotFoundException
     */
    public static boolean delFile(String filepath) throws FileNotFoundException {
        Path file = Paths.get(filepath);
        boolean pathExists = Files.exists(file, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (!pathExists) {
            throw new FileNotFoundException("文件不存在");
        } else {
            try {
                Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LOGGER.error(e);
                return false;
            }
            return true;
        }
    }

    /**
     * 文件重命名
     *
     * @param filepath
     * @param newfilename
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws FileAlreadyExistsException
     */
    public static boolean renameFile(String filepath, String newfilename)
            throws FileNotFoundException, IOException, FileAlreadyExistsException {
        newfilename = new String(CoderUtil.decoderURLBASE64(newfilename), "utf-8");
        Path source = Paths.get(filepath);
        boolean pathExists = Files.exists(source, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (!pathExists) {
            throw new FileNotFoundException("原文件不存在");
        } else {
            Path target = source.resolveSibling(newfilename);
            if (source.equals(target)) {
                throw new FileAlreadyExistsException("新文件命名与旧文件命名相同，已取消重命名操作");
            }
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                LOGGER.error(e);
                return false;
            }
        }

    }

    /**
     * 创建新文件
     *
     * @param filepath
     * @param newfilename
     * @return
     * @throws IOException
     */
    public static boolean createNewFile(String filepath, String newfilename)
            throws IOException, FileAlreadyExistsException {
        newfilename = new String(CoderUtil.decoderURLBASE64(newfilename), "utf-8");
        Path source = Paths.get(filepath, newfilename);
        boolean pathExists = Files.exists(source, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (pathExists) {
            throw new FileAlreadyExistsException("文件已存在！");
        } else {
            try {
                Files.createFile(source);
            } catch (IOException e) {
                LOGGER.error(e);
                return false;
            }
            return true;
        }
    }

    /**
     * 创建新文件夹
     *
     * @param filepath
     * @param newfilename
     * @return
     * @throws IOException
     */
    public static boolean createNewDir(String filepath, String newfilename)
            throws IOException, FileAlreadyExistsException {
        newfilename = new String(CoderUtil.decoderURLBASE64(newfilename), "utf-8");
        Path source = Paths.get(filepath, newfilename);
        boolean pathExists = Files.exists(source, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (pathExists) {
            throw new FileAlreadyExistsException("文件已存在！");
        } else {
            try {
                Files.createDirectory(source);
            } catch (IOException e) {
                LOGGER.error(e);
                return false;
            }
            return true;
        }
    }

    /**
     * 计算文件大小
     *
     * @param fileSize 单位byte
     * @return 文件描述大小
     */
    public static final String calculateDescSize(long fileSize) {
        BigDecimal big1 = new BigDecimal(fileSize);
        int[] sizes = {1073741824, 1024 * 1024, 1024};
        String[] units = {"GB", "MB", "KB"};
        for (int i = 0; i < sizes.length; i++) {
            if (fileSize > sizes[i]) {
                return big1.divide(new BigDecimal(sizes[i]), 2, BigDecimal.ROUND_HALF_EVEN).toString() + units[i];
            }
        }
        return "0KB";
    }

    /**
     * 移动文件
     *
     * @param sourceFile 原文件
     * @param targetFile 目标目录
     * @throws IOException
     * @throws CustomLogicException
     */
    public static void moveFile(Path sourceFile, Path targetFile) throws IOException, CustomLogicException {
        if (!Files.exists(sourceFile, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            throw new CustomLogicException(404, "要移动的文件不存在", null);
        }
        if (!Files.exists(targetFile, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            throw new CustomLogicException(404, "指定的存储目录不存在", null);
        }
        if (!Files.isDirectory(targetFile, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            throw new CustomLogicException(400, "不能将一个文件或文件夹移动到一个文件里", null);
        }
        targetFile = targetFile.resolve(sourceFile.getFileName());
        String targetFilePath = targetFile.toAbsolutePath().toString();
        String sourceFilePath = sourceFile.toAbsolutePath().toString();
        if (targetFilePath.indexOf(sourceFilePath) > -1 || targetFilePath.equals(sourceFilePath)) {
            throw new CustomLogicException(400, "不能将文件移动到自身或其子文件夹下", null);
        }
        if (Files.exists(targetFile, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            throw new CustomLogicException(400, "指定的新存储目录中已包含名称相同的文件或文件夹", null);
        }
        Files.move(sourceFile, targetFile);
    }

    /**
     * 文件播放或下载
     *
     * @param file
     * @param request
     * @param response
     * @throws CustomLogicException
     * @throws IOException
     */
    public static void filePlayOrDownload(boolean playFlag, Path file, HttpServletRequest request, HttpServletResponse response)
            throws CustomLogicException, IOException {
        if (!Files.exists(file, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            throw new CustomLogicException(500, "文件不存在", null);
        }

        long fileSize = file.toFile().length();

        long[] pointArray = CloudFileService.getStartEndPointFromHeaderRange(fileSize, request);

        response.reset();
        if (!StrUtil.isNullOrEmpty(request.getHeader("Range"))) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range",
                    String.format("bytes %s-%s/%s", pointArray[0], pointArray[1], fileSize));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        response.setBufferSize(10485760);
        response.setHeader("Content-Disposition",
                String.format("inline;filename=\"%s\"", file.getFileName().toString()));
        response.setHeader("Accept-Ranges", "bytes");
        response.setDateHeader("Last-Modified", Files.getLastModifiedTime(file).toMillis());
        response.setDateHeader("Expires", System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        response.setContentType(playFlag ? Files.probeContentType(file) : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Length", String.format("%s", pointArray[1] - pointArray[0] + 1));
        SeekableByteChannel input = null;
        OutputStream output = null;
        try {
            input = Files.newByteChannel(file, StandardOpenOption.READ);
            output = response.getOutputStream();
            ByteBuffer buffer = ByteBuffer.allocate(10485760);
            input.position(pointArray[0]);
            int hasRead;
            while ((hasRead = input.read(buffer)) != -1) {
                buffer.clear();
                output.write(buffer.array(), 0, hasRead);
            }
            response.flushBuffer();
        } catch (IllegalStateException e) {
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.flush();
                output.close();
            }
        }
    }

    /**
     * 从请求头Range中获取开始和结束点
     *
     * @param request
     * @return
     */
    public static long[] getStartEndPointFromHeaderRange(long fileSize, HttpServletRequest request) {
        long[] pointArray = new long[2];
        // 开始点
        long startPoint = 0;
        // 结束点
        long endPoint = fileSize - 1;
        String range = request.getHeader("Range");
        range = StrUtil.isNullOrEmpty(range) ? "" : range;
        Matcher matcher = RANGE_PATTERN.matcher(range);
        if (matcher.matches()) {
            String startGroup = matcher.group("start");
            startPoint = StrUtil.isNullOrEmpty(startGroup) ? startPoint : Integer.valueOf(startGroup);
            startPoint = startPoint < 0 ? 0 : startPoint;

            String endGroup = matcher.group("end");
            endPoint = StrUtil.isNullOrEmpty(endGroup) ? endPoint : Integer.valueOf(endGroup);
            endPoint = endPoint > fileSize - 1 ? fileSize - 1 : endPoint;

        }
        pointArray[0] = startPoint;
        pointArray[1] = endPoint;
        return pointArray;
    }

    /**
     * 视频播放
     *
     * @param file
     * @param request
     * @param response
     */
    public static void filePlay(Path file, HttpServletRequest request, HttpServletResponse response) {

    }
}
