package com.web.gallery.policy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.web.gallery.config.PhotoConfig;
import com.web.gallery.domain.photo.ImageFile;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageFileValidationPolicyTest {
	@InjectMocks
	private ImageFileValidationPolicy imageFileValidationPolicy;

	@Mock
	private PhotoConfig photoConfig;

	/** JPEGの先頭バイト列 */
	private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};

	/** PNGの先頭バイト列 */
	private static final byte[] PNG_BYTES = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

	/** GIF87aの先頭バイト列 */
	private static final byte[] GIF_87A_BYTES = "GIF87a".getBytes();

	/** GIF89aの先頭バイト列 */
	private static final byte[] GIF_89A_BYTES = "GIF89a".getBytes();

	/** WebPの先頭バイト列 */
	private static final byte[] WEBP_BYTES = buildWebpBytes();

	/** 画像に偽装されたHTML（XSSペイロード想定）の先頭バイト列 */
	private static final byte[] HTML_BYTES = "<html><body><script>alert(1)</script></body></html>".getBytes();

	private static byte[] buildWebpBytes() {
		byte[] bytes = new byte[16];
		System.arraycopy("RIFF".getBytes(), 0, bytes, 0, 4);
		// 4〜7バイト目：ファイルサイズ（本検証では対象外のためダミー値のまま）
		System.arraycopy("WEBP".getBytes(), 0, bytes, 8, 4);
		return bytes;
	}

	@Test
	@Order(1)
	@DisplayName("正常系：Content-TypeがJPEG・PNG・GIF・WebPのいずれかの場合、trueを返すこと")
	void isAllowedContentType_allowed() {
		assertTrue(imageFileValidationPolicy.isAllowedContentType(
				new ImageFile(new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES))));
		assertTrue(imageFileValidationPolicy.isAllowedContentType(
				new ImageFile(new MockMultipartFile("file", "photo.png", "image/png", PNG_BYTES))));
		assertTrue(imageFileValidationPolicy.isAllowedContentType(
				new ImageFile(new MockMultipartFile("file", "photo.gif", "image/gif", GIF_89A_BYTES))));
		assertTrue(imageFileValidationPolicy.isAllowedContentType(
				new ImageFile(new MockMultipartFile("file", "photo.webp", "image/webp", WEBP_BYTES))));
	}

	@Test
	@Order(2)
	@DisplayName("異常系：Content-Typeが許可されていない（偽装された）場合、falseを返すこと")
	void isAllowedContentType_notAllowed() {
		ImageFile imageFile = new ImageFile(new MockMultipartFile("file", "photo.jpg", "text/html", HTML_BYTES));
		assertFalse(imageFileValidationPolicy.isAllowedContentType(imageFile));
	}

	@Test
	@Order(3)
	@DisplayName("異常系：Content-Typeがnullの場合、falseを返すこと")
	void isAllowedContentType_null() {
		ImageFile imageFile = new ImageFile(new MockMultipartFile("file", "photo.jpg", null, JPEG_BYTES));
		assertFalse(imageFileValidationPolicy.isAllowedContentType(imageFile));
	}

	@Test
	@Order(4)
	@DisplayName("正常系：マジックバイトがJPEG・PNG・GIF87a・GIF89a・WebPのシグネチャと一致する場合、trueを返すこと")
	void isValidSignature_knownFormats() {
		assertTrue(imageFileValidationPolicy.isValidSignature(
				new ImageFile(new MockMultipartFile("file", "a.jpg", "image/jpeg", JPEG_BYTES))));
		assertTrue(imageFileValidationPolicy.isValidSignature(
				new ImageFile(new MockMultipartFile("file", "a.png", "image/png", PNG_BYTES))));
		assertTrue(imageFileValidationPolicy.isValidSignature(
				new ImageFile(new MockMultipartFile("file", "a.gif", "image/gif", GIF_87A_BYTES))));
		assertTrue(imageFileValidationPolicy.isValidSignature(
				new ImageFile(new MockMultipartFile("file", "a.gif", "image/gif", GIF_89A_BYTES))));
		assertTrue(imageFileValidationPolicy.isValidSignature(
				new ImageFile(new MockMultipartFile("file", "a.webp", "image/webp", WEBP_BYTES))));
	}

	@Test
	@Order(5)
	@DisplayName("異常系：拡張子・Content-Typeを偽装したHTML（XSSペイロード）の場合、マジックバイト不一致でfalseを返すこと")
	void isValidSignature_spoofedFile() {
		ImageFile imageFile = new ImageFile(new MockMultipartFile("file", "photo.jpg", "image/jpeg", HTML_BYTES));
		assertFalse(imageFileValidationPolicy.isValidSignature(imageFile));
	}

	@Test
	@Order(6)
	@DisplayName("異常系：ファイルの読み込みに失敗した場合、falseを返すこと")
	void isValidSignature_ioException() throws IOException {
		MultipartFile multipartFile = mock(MultipartFile.class);
		doThrow(new IOException("read error")).when(multipartFile).getInputStream();

		ImageFile imageFile = new ImageFile(multipartFile);
		assertFalse(imageFileValidationPolicy.isValidSignature(imageFile));
	}

	@Test
	@Order(7)
	@DisplayName("正常系：ファイルサイズが上限以下の場合、falseを返すこと")
	void isSizeExceeded_withinLimit() {
		doReturn(5).when(photoConfig).getMaxFileSizeMb();

		ImageFile imageFile = new ImageFile(new MockMultipartFile("file", "a.jpg", "image/jpeg", JPEG_BYTES));
		assertFalse(imageFileValidationPolicy.isSizeExceeded(imageFile));
	}

	@Test
	@Order(8)
	@DisplayName("異常系：ファイルサイズが上限を超えている場合、trueを返すこと")
	void isSizeExceeded_overLimit() {
		doReturn(5).when(photoConfig).getMaxFileSizeMb();

		MultipartFile multipartFile = mock(MultipartFile.class);
		doReturn(5L * 1024 * 1024 + 1).when(multipartFile).getSize();

		ImageFile imageFile = new ImageFile(multipartFile);
		assertTrue(imageFileValidationPolicy.isSizeExceeded(imageFile));
	}

	@Test
	@Order(9)
	@DisplayName("正常系：ファイルサイズが上限ちょうどの場合、falseを返すこと")
	void isSizeExceeded_exactlyAtLimit() {
		doReturn(5).when(photoConfig).getMaxFileSizeMb();

		MultipartFile multipartFile = mock(MultipartFile.class);
		doReturn(5L * 1024 * 1024).when(multipartFile).getSize();

		ImageFile imageFile = new ImageFile(multipartFile);
		assertFalse(imageFileValidationPolicy.isSizeExceeded(imageFile));
	}
}
