package org.ofbiz.common.image.scaler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ofbiz.base.util.Debug;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

/**
 * SCIPIO: Morten Nobel java-image-scaler image scaler implementation.
 * By default uses lanczos3 filter.
 * <p>
 * Supported scalingOptions:
 * <ul>
 * <li>filter (String) - "smooth" (default) or substitute (see {@link #filterMap} below for supported)</li>
 * </ul>
 * </p>
 * TODO: this is only using the ResampleOp for now; there is also MultiStepRescaleOp.
 * <p>
 * Added 2017-07-10.
 */
public class MnjimImageScaler extends AbstractImageScaler {

    public static final String module = MnjimImageScaler.class.getName();
    public static final String API_NAME = "mortennobel";
    
    /**
     * Maps <code>scalingOptions.filter</code> to ResampleFilter instances.
     */
    private static final Map<String, ResampleFilter> filterMap;
    static {
        Map<String, ResampleFilter> map = new HashMap<>();
        
        // GENERALIZED
        //map.put("areaaveraging", Image.SCALE_AREA_AVERAGING); // TODO
        //map.put("default", Image.SCALE_DEFAULT); // TODO
        //map.put("fast", Image.SCALE_FAST); // TODO
        //map.put("replicate", Image.SCALE_REPLICATE); // TODO
        map.put("smooth", ResampleFilters.getLanczos3Filter());
        
        // SPECIFIC ALGORITHMS
        map.put("lanczos3", ResampleFilters.getLanczos3Filter());
        map.put("bicubic", ResampleFilters.getBiCubicFilter());
        map.put("bicubichf", ResampleFilters.getBiCubicHighFreqResponse());
        map.put("mitchell", ResampleFilters.getMitchellFilter());
        map.put("hermite", ResampleFilters.getHermiteFilter());
        map.put("bspline", ResampleFilters.getBSplineFilter());
        map.put("triangle", ResampleFilters.getTriangleFilter());
        map.put("bell", ResampleFilters.getBellFilter());
        map.put("box", ResampleFilters.getBoxFilter());
        
        // API-SPECIFIC
        // (none)
        
        filterMap = Collections.unmodifiableMap(map);
        Debug.logInfo(AbstractImageScaler.getFilterMapLogRepr(API_NAME, map), module);
    }
    
    public static final Map<String, Object> DEFAULT_OPTIONS;
    static {
        Map<String, Object> options = new HashMap<>();
        putDefaultBufTypeOptions(options);
        options.put("filter", filterMap.get("smooth")); // String
        DEFAULT_OPTIONS = Collections.unmodifiableMap(options);
    }
    
    protected MnjimImageScaler(AbstractImageScalerFactory<MnjimImageScaler> factory, String name, Map<String, Object> confOptions) {
        super(factory, name, confOptions);
    }

    public static class Factory extends AbstractImageScalerFactory<MnjimImageScaler> {

        @Override
        public MnjimImageScaler getImageOpInstStrict(String name, Map<String, Object> defaultScalingOptions) {
            return new MnjimImageScaler(this, name, defaultScalingOptions);
        }

        @Override
        public Map<String, Object> makeValidOptions(Map<String, Object> options) {
            Map<String, Object> validOptions = new HashMap<>(options);
            putCommonBufTypeOptions(validOptions, options);
            putOption(validOptions, "filter", getFilter(options), options);
            return validOptions;
        }

        @Override protected String getApiName() { return API_NAME; }
        @Override public Map<String, Object> getDefaultOptions() { return DEFAULT_OPTIONS; }
    }
    
    @Override
    protected BufferedImage scaleImageCore(BufferedImage image, int targetWidth, int targetHeight,
            Map<String, Object> options) throws IOException {
        ResampleFilter filter = getFilter(options);
        
        // this appears to be pointless - morten already converts to a type that it likes - it's 
        // the result image type that matters to us...
//        // PRE-CONVERT: morten doesn't use the same defaults as us...
//        if (image.getType() == BufferedImage.TYPE_BYTE_BINARY ||
//                image.getType() == BufferedImage.TYPE_BYTE_INDEXED ||
//                        image.getType() == BufferedImage.TYPE_CUSTOM) {
//            Integer fallbacktype = image.getColorModel().hasAlpha() ? getFallbacktype(options) : getFallbacktypenoalpha(options);
//            image = ImageUtils.convert(image, fallbacktype);
//            // orig:
//            //image = ImageUtils.convert(image, image.getColorModel().hasAlpha() ?
//            //        BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
//        }
        
        ResampleOp op = new ResampleOp(targetWidth, targetHeight);
        if (filter != null) {
            op.setFilter(filter);
        }
        Integer destImgType = getTargetOrFallbackBufImgType(image, options, false);
        // WARN: this instantiation may lose parts of the ColorModel, but it's just what morten does internally anyway
        BufferedImage destImage = (destImgType != null) ? new BufferedImage(targetWidth, targetHeight, destImgType) : null;
        // NOTE: the morten lib doesn't support writing out to some types notably indexed, so something like this would fail for indexed types:
        //BufferedImage destImage = ImageTransform.createBufferedImage(targetWidth, targetHeight, image.getType(), image.getColorModel());
        return op.filter(image, destImage);
    }
    
    // NOTE: defaults are handled through the options merging with defaults
    protected static ResampleFilter getFilter(Map<String, Object> options) throws IllegalArgumentException {
        Object filterObj = options.get("filter");
        if (filterObj == null) return null;
        else if (filterObj instanceof ResampleFilter) return (ResampleFilter) filterObj;
        else {
            String filterName = (String) filterObj;
            if (filterName.isEmpty()) return null;
            if (!filterMap.containsKey(filterName)) throw new IllegalArgumentException("filter '" + filterName + "' not supported by " + API_NAME + " library");
            return filterMap.get(filterName);
        }
    }
}
