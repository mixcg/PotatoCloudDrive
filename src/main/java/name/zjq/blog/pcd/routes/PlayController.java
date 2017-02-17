package name.zjq.blog.pcd.routes;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bean.User;
import name.zjq.blog.pcd.interceptor.LoginUserAuth;
import name.zjq.blog.pcd.utils.CoderUtil;
import name.zjq.blog.pcd.utils.StrUtil;

@Controller
public class PlayController {
	private static final Log logger = LogFactory.getLog(PlayController.class);

	/**
	 * 视频播放
	 * 
	 * @return
	 */
	@RequestMapping(value = "/play/{path}")
	@ResponseBody
	public void filePlay(HttpServletRequest request, HttpServletResponse response, @PathVariable("path") String path,
			@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) {

		if (StrUtil.isNullOrEmpty(path)) {
			path = loginUser.getDirectory();
		} else {
			try {
				path = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(path), "utf-8");
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}
		try {
			Path video = Paths.get(path);
			int length = Math.abs((int) Files.size(video));
			int start = 0;
			int end = length - 1;

			String range = request.getHeader("Range");
			range = range == null ? "" : range;
			Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
			Matcher matcher = RANGE_PATTERN.matcher(range);

			if (matcher.matches()) {
				String startGroup = matcher.group("start");
				start = startGroup.isEmpty() ? start : Integer.valueOf(startGroup);
				start = start < 0 ? 0 : start;

				String endGroup = matcher.group("end");
				end = endGroup.isEmpty() ? end : Integer.valueOf(endGroup);
				end = end > length - 1 ? length - 1 : end;
			}

			int contentLength = end - start + 1;

			response.reset();
			response.setBufferSize(1024 * 16);
			response.setHeader("Content-Disposition", String.format("inline;filename=\"%s\"", video.getFileName()));
			response.setHeader("Accept-Ranges", "bytes");
			response.setDateHeader("Last-Modified", Files.getLastModifiedTime(video).toMillis());
			response.setDateHeader("Expires", System.currentTimeMillis() + 1000 * 60 * 60 * 24);
			response.setContentType(Files.probeContentType(video));
			response.setHeader("Content-Range", String.format("bytes %s-%s/%s", start, end, length));
			response.setHeader("Content-Length", String.format("%s", contentLength));
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

			int bytesRead;
			int bytesLeft = contentLength;
			ByteBuffer buffer = ByteBuffer.allocate(1024 * 16);

			SeekableByteChannel input = Files.newByteChannel(video, StandardOpenOption.READ);
			OutputStream output = response.getOutputStream();
			{
				input.position(start);
				while ((bytesRead = input.read(buffer)) != -1 && bytesLeft > 0) {
					buffer.clear();
					output.write(buffer.array(), 0, bytesLeft < bytesRead ? bytesLeft : bytesRead);
					bytesLeft -= bytesRead;
				}
			}
		} catch (Exception e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
