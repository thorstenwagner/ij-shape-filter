package de.biomedical_imaging.ij.shapefilter;
import java.awt.Point;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class ManyBlobsMultiLabel extends ManyBlobs {
	
	private static final long serialVersionUID = 1L;
	private ImagePlus multiLabelImage = null;
	private ImagePlus labeledImage = null;
	Map<Integer, ManyBlobs> label_blobs_map = new HashMap<Integer, ManyBlobs>();
	
	public ManyBlobsMultiLabel(ImagePlus multiLabelImage) {
		this.multiLabelImage = multiLabelImage;
				
	}
	
	@Override
	public void setBackground(int val){
		IJ.log("Set background for multi label image not implemented");
	}
	
	@Override
	public void findConnectedComponents() {
		if(multiLabelImage==null){
			throw new RuntimeException("Cannot run findConnectedComponents: No input image specified");
		}
		ImagePlus helpimp = multiLabelImage.duplicate();
		helpimp.setCalibration(multiLabelImage.getCalibration());
		
		ImageStatistics stats = helpimp.getStatistics();
		int min_label = (int)stats.min;
		int max_label = (int)stats.max;
		for(int threshold = min_label; threshold < max_label; min_label++) {
			ImageProcessor hlp = helpimp.getProcessor().duplicate();
			hlp.setThreshold(threshold, threshold);
			ByteProcessor mask = hlp.createMask();
			ImagePlus mask_imp = new ImagePlus("", mask);
			mask_imp.setCalibration(multiLabelImage.getCalibration());
			
			ManyBlobs blobs_for_label = new ManyBlobs(mask_imp);
			blobs_for_label.setBackground(0);
			blobs_for_label.findConnectedComponents();
			label_blobs_map.put(threshold, blobs_for_label);
		}
	}
	
	@Override
	public ImagePlus getLabeledImage() {
		// TODO: NEEDS TO IMPLEMENTED....
		if(labeledImage == null) {
			ColorProcessor labledImageProc = new ColorProcessor(this.multiLabelImage.getWidth(), this.multiLabelImage.getHeight());
			int offset_labels=0;
			for (Map.Entry<Integer, ManyBlobs> entry : label_blobs_map.entrySet()) {
			    int lbl = entry.getKey();
			    ManyBlobs blobs = entry.getValue();
			    ImagePlus lbl_img = blobs.getLabeledImage();
			    ImageProcessor lbl_proc = lbl_img.getProcessor();
				int[] pixels = (int[]) lbl_proc.getPixels();
				int w = lbl_proc.getWidth();
				int h = lbl_proc.getHeight();
				int value;
				int max_value = 0;
				for (int i = 0; i < h; ++i) {
					int offset = i * w;
					for (int j = 0; j < w; ++j) {
						value = pixels[offset + j];
						
						if(value!=-1){
							pixels[offset + j] = 0;
							labledImageProc.set(j, i, offset_labels+value);
							if(value>max_value) {
								max_value = value;
							}
						}
					}
				}
				offset_labels = offset_labels + max_value;
				
			    
			    
			}
			labeledImage = new ImagePlus("Labeled image",labledImageProc);
		}
		return labeledImage;
	}
	
	@Override
	public Blob get(int index) {
		int size = 0;
		for (Map.Entry<Integer, ManyBlobs> entry : label_blobs_map.entrySet()) {
			if((size + entry.getValue().size())<index) {
				size += entry.getValue().size();
			}
			else {
				return entry.getValue().get(index-size);
			}
		}
		throw new ArrayIndexOutOfBoundsException(index);
	}
	
	@Override
	public int size() {
		int size = 0;
		for (Map.Entry<Integer, ManyBlobs> entry : label_blobs_map.entrySet()) {
			size += entry.getValue().size();
		}
		return size;
	}
	
	
	@Override
	public void setLabeledImage(ImagePlus p) {
		// TODO: NEEDS TO IMPLEMENTED....
		labeledImage = p;
	}
	

	/**
	 * Returns a specific {@link Blob} which encompasses a point
	 * @param x x coordinate of the point
	 * @param y y coordinate of the point
	 * @return The blob which contains the point, otherwise null
	 */
	@Override
	public Blob getSpecificBlob(int x, int y){
		for (Map.Entry<Integer, ManyBlobs> entry : label_blobs_map.entrySet()) {
		    int lbl = entry.getKey();
		    ManyBlobs blobs = entry.getValue();
		    for(int i = 0; i < this.size(); i++){
		       if(this.get(i).getOuterContour().contains(x, y)){
		    	   boolean not_in_inner_region = true;
		    	   for (Polygon inner : this.get(i).getInnerContours()) {
		    		   if(inner.contains(x,y)) {
		    			   not_in_inner_region = false;
		    		   }
		    	   }
		    	   if(not_in_inner_region) {
		    		   return this.get(i);
		    	   }
		       }
			}
		}
		
	   return null;
	}
	
	
	@Override
	public Blob getBlobByLabel(int id) {
		// TODO Auto-generated method stub
		// thats a problem... because its not unique in a multi label image...
		// I should recreate a map, that maps the id to specific blob. Howver, the getLabel function of the blob would then give wrong results...
		// probably its better to change the ijblob libary, so that it allows to change the label of a blob
		return super.getBlobByLabel(id);
	}


}
