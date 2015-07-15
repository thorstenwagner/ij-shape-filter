package de.biomedical_imaging.ij.shapefilter.ndef;

import java.awt.Rectangle;

import de.biomedical_imaging.ij.shapefilter.Shape_Filter;
import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class GreyscaleCharacteristicsToResultsTable_ implements PlugInFilter {
	private ManyBlobs[] shapeFilterBlobs;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		ResultsTable rt = ResultsTable.getResultsTable();
		if(rt==null || Shape_Filter.getInstance() == null){
			IJ.error("Please run the Shape Filter Plugin at first!");
		}
		shapeFilterBlobs= Shape_Filter.getInstance().getAllBlobs();
		
		return DOES_8G + DOES_STACKS;
	}

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		ResultsTable rt = ResultsTable.getResultsTable();

		if(rt==null){
			IJ.error("No Results Table");
		}
		IJ.run("Set Measurements...", "  mean standard modal min integrated median skewness kurtosis redirect=None decimal=3");

	
		for(int i = 0; i < rt.getCounter(); i++){
			int bloblabel = Integer.parseInt(rt.getStringValue(1, i));
			int frame = Integer.parseInt(rt.getStringValue(0, i));
			
			if((frame)==ip.getSliceNumber()){
				Blob b = shapeFilterBlobs[frame-1].getBlobByLabel(bloblabel);
				ip.setRoi(b.getOuterContour());
				int flags = ImageStatistics.MEDIAN+ImageStatistics.MEAN + ImageStatistics.SKEWNESS+ ImageStatistics.KURTOSIS +  ImageStatistics.STD_DEV;
				
				ImageStatistics is = ImageStatistics.getStatistics(ip, flags, null);

				rt.setValue("Grey-Mean", i, is.mean);
				rt.setValue("Grey-StdDev", i, is.stdDev);
				rt.setValue("Grey-Skewness", i, is.skewness);
				rt.setValue("Grey-Kurtosis", i, is.kurtosis);
				rt.setValue("Grey-Median", i, is.median);
				
			}
		}
		rt.show("Results");	
	}


}
