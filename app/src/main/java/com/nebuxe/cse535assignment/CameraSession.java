package com.nebuxe.cse535assignment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.nebuxe.cse535assignment.callbacks.CameraSessionListener;
import com.nebuxe.cse535assignment.callbacks.OnFrameListener;
import com.nebuxe.cse535assignment.utilities.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraSession {

    protected enum PreviewStatus {
        READY,
        ACTIVE,
        PAUSED,
        CLOSED
    }

    private PreviewStatus currentPreviewStatus = PreviewStatus.READY;

    protected PreviewStatus getCurrentPreviewStatus() {
        return this.currentPreviewStatus;
    }

    protected void setCurrentPreviewStatus(PreviewStatus previewStatus) {
        this.currentPreviewStatus = previewStatus;
    }


    private Context context;
    private TextureView textureView;

    private CameraDevice cameraDevice;
    private Size previewDimen;

    private CameraCaptureSession previewSession;
    MediaRecorder mediaRecorder;

    private File outputFile;

    private CameraSessionListener cameraSessionListener;
    private OnFrameListener onFrameListener;

    private boolean surfaceTextureAvailable = false;
    private final int scaleFactor = 4;

    public CameraSession(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;

        setEventListeners();
    }

    private void setEventListeners() {
        cameraSessionListener = (CameraSessionListener) context;
        onFrameListener = (OnFrameListener) context;
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                try {
                    surfaceTextureAvailable = true;
                    openCamera();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private String getRearFacingCameraID(CameraManager cameraManager) throws CameraAccessException {
        return cameraManager.getCameraIdList()[0];
    }

    private StreamConfigurationMap getStreamConfigurationMap() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraID = getRearFacingCameraID(cameraManager);
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        return streamConfigurationMap;
    }

    @SuppressLint("MissingPermission")
    protected void openCamera() throws CameraAccessException {
        if (!surfaceTextureAvailable) {
            return;
        }
        StreamConfigurationMap streamConfigurationMap = getStreamConfigurationMap();
        previewDimen = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];

        Log.d("CAMERA", "SIZE");
        Log.d("CAMERA", previewDimen.getWidth() + " " + previewDimen.getHeight());
        Log.d("CAMERA", textureView.getWidth() + " " + textureView.getHeight());

        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraID = getRearFacingCameraID(cameraManager);
        cameraManager.openCamera(cameraID, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                try {
                    startCameraPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                closeCamera();
                setCurrentPreviewStatus(PreviewStatus.CLOSED);
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                closeCamera();
                setCurrentPreviewStatus(PreviewStatus.CLOSED);
            }
        }, null);
    }

    private void setPreviewSession(CameraCaptureSession session, CaptureRequest.Builder captureRequestBuilder) throws CameraAccessException {
        previewSession = session;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        previewSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
    }

    private ImageReader imageReader;

    private Surface setupImageReader() {
        Log.d("CAMERA DIMEN", previewDimen.getWidth() + " " + previewDimen.getHeight());
        imageReader = ImageReader.newInstance(previewDimen.getWidth() / scaleFactor, previewDimen.getHeight() / scaleFactor, ImageFormat.YUV_420_888, 1);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                if (image != null) {
//                    ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
//                    ByteBuffer yClone = Utils.clone(image.getPlanes()[0].getBuffer());
//                    byteBuffer.clear();
//
//                    byteBuffer = image.getPlanes()[2].getBuffer();
//                    ByteBuffer vClone = Utils.clone(byteBuffer);
//                    byteBuffer.clear();
//
//                    onFrameListener.onNewFrameAvailable(new ByteBuffer[]{yClone, null, vClone});

                    Bitmap bitmap = textureView.getBitmap();
                    onFrameListener.onNewFrameAvailable(bitmap);
                    image.close();
                }
            }
        }, new Handler());

        return imageReader.getSurface();
    }

    private MediaRecorder setupMediaRecorder() {
        File mediaFile = Utils.getLocalFile(context, null);
        if (mediaFile == null) {
            Toast.makeText(context, "No file", Toast.LENGTH_SHORT).show();
            return null;
        }
        outputFile = mediaFile;

        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setOutputFile(mediaFile.getAbsolutePath());

//        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
//        mediaRecorder.setVideoFrameRate(camcorderProfile.videoFrameRate);
//        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
//        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
//        mediaRecorder.setProfile(camcorderProfile);

        return mediaRecorder;
    }

    private void startCameraPreview() throws CameraAccessException {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewDimen.getWidth() / scaleFactor, previewDimen.getHeight() / scaleFactor);
        Surface surface = new Surface(surfaceTexture);

        final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        List<Surface> cameraOutputs = new ArrayList<>();

        captureRequestBuilder.addTarget(surface);
        cameraOutputs.add(surface);

        cameraDevice.createCaptureSession(cameraOutputs,
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            setPreviewSession(session, captureRequestBuilder);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        closeCamera();
                    }
                }, null);
    }

    protected void startRecording() throws CameraAccessException, IOException {

        stopPreview();

        StreamConfigurationMap streamConfigurationMap = getStreamConfigurationMap();
        previewDimen = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];

        Size videoDimensions = streamConfigurationMap.getOutputSizes(MediaRecorder.class)[0];
        Log.d("CAMERA VIDEO DIMEN", videoDimensions.getWidth() + " " + videoDimensions.getHeight());
        Log.d("CAMERA VIDEO DIMEN", previewDimen.getWidth() + " " + previewDimen.getHeight());

//        mediaRecorder = setupMediaRecorder();
//        mediaRecorder.prepare();

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(videoDimensions.getWidth() / scaleFactor, videoDimensions.getHeight() / scaleFactor);

        final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range.create(22, 24));

        List<Surface> cameraOutputs = new ArrayList<>();

        Surface previewSurface = new Surface(surfaceTexture);
//        final Surface recorderSurface = mediaRecorder.getSurface();
        Surface imageReaderSurface = setupImageReader();

        captureRequestBuilder.addTarget(previewSurface);
//        captureRequestBuilder.addTarget(recorderSurface);
        captureRequestBuilder.addTarget(imageReaderSurface);

        cameraOutputs.add(previewSurface);
//        cameraOutputs.add(recorderSurface);
        cameraOutputs.add(imageReaderSurface);

        cameraDevice.createCaptureSession(cameraOutputs, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    setPreviewSession(session, captureRequestBuilder);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
//                mediaRecorder.start();
                cameraSessionListener.onRecordingStarted();
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, new Handler());
    }

    protected void closeCamera() {
        try {
            stopPreview();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        try {
            stopRecording();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (cameraDevice != null) {
            cameraDevice.close();
        }
        cameraDevice = null;

        setCurrentPreviewStatus(PreviewStatus.CLOSED);
        cameraSessionListener.onCameraClosed();
    }

    private void stopPreview() throws CameraAccessException {
        if (previewSession != null) {
            previewSession.stopRepeating();
            previewSession.close();
        }
        previewSession = null;
    }

    protected void stopRecording() throws CameraAccessException, IllegalStateException {
        if (previewSession != null) {
            previewSession.stopRepeating();
            previewSession.abortCaptures();
            previewSession.close();
        }
        previewSession = null;


        if (imageReader != null) {
            imageReader.close();
        }
        imageReader = null;

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        mediaRecorder = null;
    }
}
