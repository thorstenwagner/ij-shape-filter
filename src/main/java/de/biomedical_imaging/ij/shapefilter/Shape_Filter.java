/*
    Shape Filter is a plugin for ImageJ to analyse and filter segmented images 
    by shape features.
    Copyright (C) 2012  Thorsten Wagner wagner@biomedical-imaging.de

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.biomedical_imaging.ij.shapefilter;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Polygon;
import java.awt.Window;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.util.Iterator;
import java.util.Map.Entry;
/*
 * Autor: Thorsten Wagner, wagner@biomedical-imaging.de
 */
public class Shape_Filter implements ExtendedPlugInFilter {
	/*
	 * This plugin filters binary objects using shape parameters.
	 * Needs ImageJ 1.47h 
	 */

	private ImagePlus imp;
	private ImageStack labeledImageStack;
	private ManyBlobs[] allBlobs; 	//For each slice one ManyBlobs object
	private ImageProcessor currentIP;
	private FilterParameters para;
	private ResultsTable rt;
	boolean processStack;
	boolean previewIsActive = false;
	private static Shape_Filter instance = null;
	private boolean registered;
	public Shape_Filter() {
		instance=this;
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		
		if (imp == null || imp.getType() != ImagePlus.GRAY8) {
			IJ.error("Binary Image is needed!");
			return DONE;
		}
		
		//Check if binary
		ImageStatistics stats = imp.getStatistics();
		float binaryRatio = ((float)(stats.histogram[0] + stats.histogram[255]))/stats.pixelCount;
		if (binaryRatio > 0.90 && ((int)binaryRatio) != 1) {
			IJ.log("Not really binary...(lossy Image format?) but more than " + IJ.d2s(binaryRatio*100, 0)  +"% of the image are black or white pixels. Converted to Binary!");
			imp.getProcessor().threshold(127);
			
		}
		else if(binaryRatio < 0.90 ) {
			IJ.error("Binary Image is needed!");
			return DONE;
		}
		
		this.imp = imp;
		labeledImageStack = new ImageStack(imp.getWidth(), imp.getHeight());

		allBlobs = new ManyBlobs[imp.getStackSize()];
	
		
		
		return DOES_8G;
	}
	
	@Override
	public void run(ImageProcessor ip) {
		currentIP = ip;
		
		ImagePlus helpimp = new ImagePlus("", ip);
		helpimp.setCalibration(imp.getCalibration());
		allBlobs[currentIP.getSliceNumber()-1] = new ManyBlobs(helpimp);
		
		//Set the background color used by Center Method blob and maximum
		if(para.isBlackBackground()){
			allBlobs[currentIP.getSliceNumber()-1].setBackground(0);
		}
		else{
			allBlobs[currentIP.getSliceNumber()-1].setBackground(1);
		}
		allBlobs[currentIP.getSliceNumber()-1].findConnectedComponents();
		IJ.showStatus("Component Labeling Done");
		
		calculateResultImage(para);
		
		if (para.isAddToManager() && previewIsActive==false) {
			addToManager(para);
		}
		
		if(para.isFillResultsTable()&& previewIsActive==false){
			fillResultTable(para);
		}
		
		if(para.isShowLabeledImage()&& previewIsActive==false){
			ManyBlobs fb = getFilteredBlobs(para);
			labeledImageStack.addSlice(fb.getLabeledImage().getProcessor());
		
			if(currentIP.getSliceNumber()==imp.getStackSize()){
				ImagePlus help = new ImagePlus("Labeled Image");
				help.setStack(labeledImageStack);
				help.show();
			}
		}
		
	}
	
	
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		BlobFilterDialog dialog = new BlobFilterDialog();
		
		if (dialog.showDialog(pfr)==-1){ 
			return DONE;
		}else{
			previewIsActive=false;
		}
		para = dialog.getParams();
		
		int flags = IJ.setupDialog(imp, DOES_8G);
		processStack = (flags&DOES_STACKS)!=0;
		
		if(para.isFillResultsTable()){
			IJ.getTextPanel().addMouseListener(new ResultsTableSelectionDrawer(imp));
			registerImage(imp.getTitle());
		}
		
		
		return flags;
	}

	@Override
	public void setNPasses(int nPasses) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Adds an ImageResultsTableSelector-Listener for the image with the name "title"
	 * @param title window name
	 */
	private void registerImage(String title){
		Window window = WindowManager.getWindow(title);
		ImagePlus image = WindowManager.getImage(title);
		boolean windowIsVisible = (window!=null);
		if(windowIsVisible){
			
			KeyListener[] allKeyListener = window.getComponent(0).getKeyListeners();
			boolean isClassListenerAdded = false;
			for(int i =0; i<allKeyListener.length; i++){
				if(allKeyListener[i] instanceof AddClassToResultsTableListener){
					String regTitle = ((AddClassToResultsTableListener)allKeyListener[i]).getImage().getTitle();
					if(regTitle==title){
						isClassListenerAdded=true;
					}
				}
			}
			
			window.getComponent(0).addMouseListener(new ImageResultsTableSelector(image));
			if(!isClassListenerAdded){
			window.getComponent(0).addKeyListener(new AddClassToResultsTableListener(image));
			}
		}
	}
	
	public static Shape_Filter getInstance(){
		return instance;
	}
 
	/**
	 * @return An array with ijblob manyblobs objects (for each slice one)
	 */
	public ManyBlobs[] getAllBlobs(){
		return allBlobs;
	}
	
	/**
	 * 
	 * @param frame
	 * @param label
	 * @return a blob with label 'label' in frame with index 'frame'
	 */
	public Blob getBlobByFrameAndLabel(int frame, int label){
		return allBlobs[frame].getBlobByLabel(label);
	}

	
	
	/**
	 * Fügt für alle Blobs, dessen Formparameter innerhalb der Schwellwerte liegen, die ermittelten Formparamter
	 * in die Result-Table ein
	 * @param params Schwellwerte der Formparamter
	 */
	private void fillResultTable(FilterParameters params) {
		rt = Analyzer.getResultsTable();

		if(rt==null)
		{
			 rt = new ResultsTable();	
			 Analyzer.setResultsTable(rt);
		}

		ManyBlobs fb = getFilteredBlobs(params);
		for (int i = 0; i < fb.size(); i++) {
				rt.incrementCounter();
				rt.addValue("Frame", processStack?currentIP.getSliceNumber():imp.getCurrentSlice());
				rt.addValue("Label", fb.get(i).getLabel());
				Point2D cog = fb.get(i).getCenterOfGravity();
				rt.addValue("X", cog.getX());
				rt.addValue("Y", cog.getY());
				rt.addValue("Area", fb.get(i).getEnclosedArea());
				rt.addValue("Area Conv. Hull", fb.get(i).getAreaConvexHull());
				rt.addValue("Peri.", fb.get(i).getPerimeter());
				rt.addValue("Peri. Conv. Hull", fb.get(i)
						.getPerimeterConvexHull());
				rt.addValue("Feret", fb.get(i).getFeretDiameter());
				rt.addValue("Min. Feret", fb.get(i).getMinFeretDiameter());
				rt.addValue("Long Side Length MBR", fb.get(i).getLongSideMBR());
				rt.addValue("Short Side Length MBR", fb.get(i).getShortSideMBR());
				rt.addValue("Aspect Ratio", fb.get(i).getAspectRatio());
				rt.addValue("Area/Peri.", fb.get(i)
						.getAreaToPerimeterRatio());
				rt.addValue("Circ.", fb.get(i).getCircularity());
				rt.addValue("Elong.", fb.get(i).getElongation());
				rt.addValue("Convexity", fb.get(i).getConvexity());
				rt.addValue("Solidity", fb.get(i).getSolidity());
				rt.addValue("Num. of Holes", fb.get(i)
						.getNumberofHoles());
				rt.addValue("Thinnes Rt.", fb.get(i).getThinnesRatio());
				rt.addValue("Contour Temp.", fb.get(i)
						.getContourTemperature());
				rt.addValue("Fract. Dim.", fb.get(i)
						.getFractalBoxDimension(params.getFractalBoxSizes()));
				rt.addValue("Fract. Dim. Goodness", fb.get(i)
						.getFractalDimensionGoodness());
		}

		rt.show("Results");
	}
	
	
	private void calculateResultImage(FilterParameters params) {
		ManyBlobs fb = getFilteredBlobs(params);
		if(params.isBlackBackground()){
			currentIP.setColor(Color.black);
			Blob.setDefaultColor(Color.white);
		}
		else
		{
			currentIP.setColor(Color.white);
			Blob.setDefaultColor(Color.black);
		}
		currentIP.fill();
		for (int i = 0; i < fb.size(); i++) {
			IJ.showStatus("Feature Calculation");
			IJ.showProgress(i + 1, fb.size());
			fb.get(i).draw(currentIP, params.isDrawHoles() | params.isDrawConvexHull() | params.isDrawLabel());
		}
	}
	
	private void addToManager(FilterParameters params) {
		Frame frame = WindowManager.getFrame("ROI Manager");
		if (frame == null)
			IJ.run("ROI Manager...");
		frame = WindowManager.getFrame("ROI Manager");
		RoiManager roiManager = (RoiManager) frame;

		ManyBlobs fb = getFilteredBlobs(params);
		for (int i = 0; i < fb.size(); i++) {
				Polygon p = fb.get(i).getOuterContour();
				int n = p.npoints;
				float[] x = new float[p.npoints];
				float[] y = new float[p.npoints];
				
				for (int j=0; j<n; j++) {
				     x[j] = p.xpoints[j]+0.5f;
				     y[j] = p.ypoints[j]+0.5f;
				}
				
				Roi roi = new PolygonRoi(x,y,n,Roi.TRACED_ROI);
				
				Roi.setColor(Color.green);
				roiManager.add(imp, roi, i);
				
				
		}
	}
	
	private ManyBlobs getFilteredBlobs(FilterParameters params){
		ManyBlobs fb = new ManyBlobs();
		Iterator<Entry<String, double[]>> it = params.getFeatureIterator();
		Entry<String, double[]> pairs = it.next();
	
		fb = allBlobs[currentIP.getSliceNumber()-1].filterBlobs((double[])pairs.getValue(), pairs.getKey(),params.getFilterMethodParameter(pairs.getKey()));
		while(it.hasNext()) {
			pairs = it.next();
			fb = fb.filterBlobs((double[])pairs.getValue(), pairs.getKey(),params.getFilterMethodParameter(pairs.getKey()));
		}
		return fb;
		
	}
	
	/**
	 * Sets the the preview mode property.
	 * @param ispreview true, if the preview mode is active
	 */
	public void setIsPreview(boolean ispreview){
		this.previewIsActive = ispreview;
	}
	
	/**
	 * Sets the used shape parameter configuration
	 * @param parameter configuration
	 */
	public void setParameters(FilterParameters para){
		this.para = para;
	}

	
}
