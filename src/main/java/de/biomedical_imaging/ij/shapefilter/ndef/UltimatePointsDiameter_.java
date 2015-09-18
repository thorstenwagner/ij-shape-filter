package de.biomedical_imaging.ij.shapefilter.ndef;
import java.awt.Polygon;

import de.biomedical_imaging.ij.shapefilter.Shape_Filter;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.EDM;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class UltimatePointsDiameter_ implements PlugInFilter {
	private ManyBlobs[] shapeFilterBlobs;
	private boolean isBlackBackground;
	private Calibration cal = new Calibration();
	@Override
	public int setup(String arg, ImagePlus imp) {
		String[] openWindows = new String[WindowManager.getImageCount()];
		for(int i = 0; i < WindowManager.getImageCount(); i++){
			openWindows[i] = WindowManager.getImage(WindowManager.getIDList()[i]).getTitle();
		}
		GenericDialog gd = new GenericDialog("UltimatePointsDiamatter");
		gd.addCheckbox("Black background", false);
		gd.showDialog();
		
		ResultsTable rt = ResultsTable.getResultsTable();
		if(rt==null || Shape_Filter.getInstance() == null){
			IJ.error("Please run the Shape Filter Plugin at first!");
		}
		shapeFilterBlobs= Shape_Filter.getInstance().getAllBlobs();
		isBlackBackground = gd.getNextBoolean();
		
		if(Math.abs(cal.getX(1)-cal.getY(1))>0.01){
			IJ.log("Max. inscribed circle diameter only supports quadratic pixels, ignoring image calibration");
		}else{
			cal = imp.getCalibration();
		}
	
		return DOES_8G + DOES_STACKS;
	}

	@Override
	public void run(ImageProcessor ip) {
		
		//Findet die Maximas der EDM und weißt Sie Blobs zu. 
		
		if(!isBlackBackground){
			ip.invert();
		}
		EDM dm = new EDM();
		FloatProcessor fp = dm.makeFloatEDM (ip, 0, false);
		
		MaximumFinder mf  = new MaximumFinder();
		//ImagePlus fpp = new ImagePlus("asd",fp);
		//fpp.show();
		ByteProcessor bp = mf.findMaxima(fp, 0.5, ImageProcessor.NO_THRESHOLD, MaximumFinder.SINGLE_POINTS, false, true);
		Polygon pl = mf.getMaxima(bp, 0, true);
		
		
		ResultsTable rt = ResultsTable.getResultsTable();

		if(rt==null){
			IJ.error("No Results Table");
		}
		for(int i = 0; i < rt.getCounter(); i++){
			int bloblabel = Integer.parseInt(rt.getStringValue(1, i));
			int frame = Integer.parseInt(rt.getStringValue(0, i));
			if((frame)==ip.getSliceNumber()){
				Blob b = shapeFilterBlobs[frame-1].getBlobByLabel(bloblabel);
				for(int j = 0; j < pl.npoints; j++){
					if(b.getOuterContour().contains(pl.xpoints[j], pl.ypoints[j])){
						
						rt.setValue("Max. inscribed circle diameter", i, fp.getf(pl.xpoints[j], pl.ypoints[j])*2*cal.getX(1));
					}
				}
			}else if(frame>ip.getSliceNumber()){
				break;
			}
		}

		rt.show("Results");	
	}
}
