package com.lazyzxsoftware.zxspectrumide.emulator.ui;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class SpectrumRenderer {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;

    private final WritableImage image;
    private final PixelWriter pixelWriter;

    public SpectrumRenderer() {
        this.image = new WritableImage(WIDTH, HEIGHT);
        this.pixelWriter = image.getPixelWriter();
    }

    public WritableImage getImage() {
        return image;
    }

    // Ya no necesita lógica de renderizado, solo pintar lo que le da la ULA
    public void paintBuffer(int[] pixelData) {
        if (pixelData == null) return;

        pixelWriter.setPixels(0, 0, WIDTH, HEIGHT,
                javafx.scene.image.PixelFormat.getIntArgbInstance(),
                pixelData, 0, WIDTH);
    }

    // El método tickFlash ya no se usa aquí, lo gestiona Spectrum48k internamente
    public void tickFlash() {}
}