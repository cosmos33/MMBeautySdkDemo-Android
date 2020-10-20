package com.cosmos.appbase.camera.algorithm

import android.hardware.Camera

@Suppress("DEPRECATION")
class RecorderSizeImpl : IRecorderSize {
    /**
     * Iterate over supported camera video sizes to see which one best fits the
     * dimensions of the given view while maintaining the aspect ratio. If none can,
     * be lenient with the aspect ratio.
     *
     * @param supportedVideoSizes Supported camera video sizes.
     * @param previewSizes        Supported camera preview sizes.
     * @param w                   The width of the view.
     * @param h                   The height of the view.
     * @return Best match camera video size to fit in the view.
     */
    override fun getOptimalVideoSize(
        supportedVideoSizes: List<Camera.Size>?,
        previewSizes: List<Camera.Size>, w: Int, h: Int
    ): Camera.Size? {
        // Use a very small tolerance because we want an exact match.
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = w.toDouble() / h

        // Supported video sizes list might be null, it means that we are allowed to use the preview
        // sizes
        val videoSizes: List<Camera.Size>
        videoSizes = supportedVideoSizes ?: previewSizes
        var optimalSize: Camera.Size? = null

        // Start with max value and refine as we iterate over available video sizes. This is the
        // minimum difference between view and camera height.
        var minDiff = Double.MAX_VALUE

        // Target view height

        // Try to find a video size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (size in videoSizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff && previewSizes.contains(
                    size
                )
            ) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        // Cannot find video size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in videoSizes) {
                if (Math.abs(size.height - h) < minDiff && previewSizes.contains(
                        size
                    )
                ) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }

    companion object {
        /**
         * Attempts to find a preview size that matches the provided width and height (which
         * specify the dimensions of the encoded video).  If it fails to find a match it just
         * uses the default preview size.
         *
         *
         * TODO: should do a best-fit match.
         */
        fun choosePreviewSize(
            parms: Camera.Parameters,
            width: Int,
            height: Int
        ) {
            // We should make sure that the requested MPEG size is less than the preferred
            // size, and has the same aspect ratio.
            val ppsfv = parms.preferredPreviewSizeForVideo
            for (size in parms.supportedPreviewSizes) {
                if (size.width == width && size.height == height) {
                    parms.setPreviewSize(width, height)
                    return
                }
            }
            if (ppsfv != null) {
                parms.setPreviewSize(ppsfv.width, ppsfv.height)
            }
        } //    private Size getProperPreviewSize(Size exceptSize) {
        //        // TODO: 2020/5/12 获取最佳预览大小
        //        int orientation = getActivity().getWindowManager().getDefaultDisplay().getOrientation();
        //        if (orientation == 0 || orientation == 180) {
        //
        //        } else {
        //
        //        }
        //        List<Camera.Size> supportedPreviewSizes = currentCamera.getParameters().getSupportedPreviewSizes();
        ////        Arrays.sort(objects);
        //        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
        //            if (exceptSize.getWidth() == supportedPreviewSize.width
        //                    && exceptSize.getHeight() == supportedPreviewSize.height) {
        //                return exceptSize;
        //            }
        //            if (exceptSize.getWidth() == supportedPreviewSize.width) {
        //                return exceptSize;
        //            }
        //        }
        //        return null;
        //    }
    }
}