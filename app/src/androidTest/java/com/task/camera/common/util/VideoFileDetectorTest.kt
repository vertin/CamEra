package com.task.camera.common.util

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Tests for VideoFileDetector - class extracted for improved testability
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class VideoFileDetectorTest {

    private val detector = VideoFileDetector()

    @Test
    fun shouldDetectMp4FileExtension() {
        assertTrue("Should detect .mp4 extension", detector.isVideoFileExtension("video.mp4"))
        assertTrue("Should detect .MP4 extension", detector.isVideoFileExtension("video.MP4"))
        assertTrue("Should detect .Mp4 extension", detector.isVideoFileExtension("video.Mp4"))
    }

    @Test
    fun shouldDetectMkvFileExtension() {
        assertTrue("Should detect .mkv extension", detector.isVideoFileExtension("video.mkv"))
        assertTrue("Should detect .MKV extension", detector.isVideoFileExtension("video.MKV"))
    }

    @Test
    fun shouldDetectWebmFileExtension() {
        assertTrue("Should detect .webm extension", detector.isVideoFileExtension("video.webm"))
        assertTrue("Should detect .WEBM extension", detector.isVideoFileExtension("video.WEBM"))
    }

    @Test
    fun shouldDetect3gpFileExtension() {
        assertTrue("Should detect .3gp extension", detector.isVideoFileExtension("video.3gp"))
        assertTrue("Should detect .3GP extension", detector.isVideoFileExtension("video.3GP"))
    }

    @Test
    fun shouldNotDetectNonVideoFileExtensions() {
        assertFalse("Should not detect .jpg", detector.isVideoFileExtension("image.jpg"))
        assertFalse("Should not detect .png", detector.isVideoFileExtension("image.png"))
        assertFalse("Should not detect .txt", detector.isVideoFileExtension("document.txt"))
        assertFalse("Should not detect .pdf", detector.isVideoFileExtension("document.pdf"))
    }

    @Test
    fun shouldHandleFilePathWithMultipleDots() {
        assertTrue(
            "Should detect video extension in path with multiple dots",
            detector.isVideoFileExtension("/path/to/my.video.file.mp4")
        )
    }

    @Test
    fun shouldDetectVideoFileFromStringPathWithMp4Extension() {
        assertTrue(
            "Should detect video from string path with .mp4",
            detector.isVideoFile("/storage/video.mp4")
        )
    }

    @Test
    fun shouldDetectVideoFileFromStringWithContentUriAndVideoPath() {
        assertTrue(
            "Should detect video from content URI with /video/ path",
            detector.isVideoFile("content://media/external/video/media/123")
        )
    }

    @Test
    fun shouldNotDetectNonVideoFileFromStringPath() {
        assertFalse(
            "Should not detect video from string path with .jpg",
            detector.isVideoFile("/storage/image.jpg")
        )
    }

    @Test
    fun shouldHandleInvalidUriFormatInString() {
        assertFalse(
            "Should not detect video from invalid URI without extension",
            detector.isVideoFile("not a valid uri://format")
        )
    }

    @Test
    fun shouldDetectVideoFileFromUriWithVideoPath() {
        val uri = Uri.parse("content://media/external/video/media/123")
        assertTrue("Should detect video from Uri with /video/ path", detector.isVideoFile(uri))
    }

    @Test
    fun shouldDetectVideoFileFromUriWithContentScheme() {
        val uri = Uri.parse("content://media/external/images/media/123")
        assertTrue("Should detect video from content scheme Uri", detector.isVideoFile(uri))
    }

    @Test
    fun shouldNotDetectVideoFileFromHttpUri() {
        val uri = Uri.parse("http://example.com/video.mp4")
        assertFalse("Should not detect video from http Uri", detector.isVideoFile(uri))
    }

    @Test
    fun shouldNotDetectVideoFileFromHttpsUri() {
        val uri = Uri.parse("https://example.com/video.mp4")
        assertFalse("Should not detect video from https Uri", detector.isVideoFile(uri))
    }

    @Test
    fun shouldDetectVideoFileFromFileObjectWithMp4Extension() {
        val file = File("/storage/video.mp4")
        assertTrue("Should detect video from File with .mp4", detector.isVideoFile(file))
    }

    @Test
    fun shouldDetectVideoFileFromFileObjectWithMkvExtension() {
        val file = File("/storage/video.mkv")
        assertTrue("Should detect video from File with .mkv", detector.isVideoFile(file))
    }

    @Test
    fun shouldNotDetectVideoFileFromFileObjectWithNonVideoExtension() {
        val file = File("/storage/image.png")
        assertFalse("Should not detect video from File with .png", detector.isVideoFile(file))
    }

    @Test
    fun shouldNotDetectVideoFileFromUnsupportedType() {
        assertFalse("Should not detect video from Integer", detector.isVideoFile(12345))
        assertFalse("Should not detect video from Boolean", detector.isVideoFile(true))
    }
}
