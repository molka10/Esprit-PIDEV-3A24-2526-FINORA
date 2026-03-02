package com.example.finora.utils;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CameraService {

    private Webcam webcam;
    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    // Store the last raw BufferedImage so captureSnapshot() can use it directly
    private final AtomicReference<BufferedImage> lastFrame = new AtomicReference<>();

    public CameraService() {
        webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.setViewSize(WebcamResolution.VGA.getSize());
        }
    }

    public void start() {
        if (webcam == null) return;

        webcam.open();
        running.set(true);

        Thread captureThread = new Thread(() -> {
            while (running.get()) {
                if (webcam.isOpen()) {
                    BufferedImage bf = webcam.getImage();
                    if (bf != null) {
                        // Store a COPY as TYPE_INT_RGB (safe for ImageIO.write)
                        BufferedImage copy = new BufferedImage(
                                bf.getWidth(), bf.getHeight(), BufferedImage.TYPE_INT_RGB);
                        copy.getGraphics().drawImage(bf, 0, 0, null);

                        lastFrame.set(copy);
                        Image img = SwingFXUtils.toFXImage(copy, null);
                        imageProperty.set(img);
                    }
                }
                try {
                    Thread.sleep(50); // ~20 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public void stop() {
        running.set(false);
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    public ObjectProperty<Image> imageProperty() {
        return imageProperty;
    }

    /**
     * Returns the last captured BufferedImage directly — no re-capture needed.
     * This avoids webcam conflicts and ensures ImageIO.write produces valid JPEG bytes.
     */
    public BufferedImage captureSnapshot() {
        return lastFrame.get();
    }

    public Webcam getWebcam() {
        return webcam;
    }
}