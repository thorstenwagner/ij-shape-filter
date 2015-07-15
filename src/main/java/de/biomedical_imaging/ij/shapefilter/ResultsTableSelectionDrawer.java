package de.biomedical_imaging.ij.shapefilter;
import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.filter.Analyzer;

import java.awt.Color;
import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
/**
 * This class implements an listener for interactively select a row in the results table and the corresponding blob in an image.
 **/
public class ResultsTableSelectionDrawer implements MouseListener {
	
	public int selectionStart = -1;
	public int selectionStop = -1;
	public ImagePlus imp;

	public ResultsTableSelectionDrawer(ImagePlus imp) {
		this.imp = imp;
	
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		update(IJ.getTextPanel().getSelectionStart(), IJ.getTextPanel().getSelectionEnd());
	}
	
	public void update(int start, int end){
		
		if(selectionStart!= start || selectionStop != end ){
			selectionStart = start;
			selectionStop = end;
			if(selectionStart>=0){
				showAsOverlay(selectionStart, selectionStop);
				
			}
		}
	}
	
	public void showAsOverlay(int start, int end){
		
		Overlay ov = imp.getOverlay();
		if(ov==null){
			ov = new Overlay();
			IJ.getImage().setOverlay(ov);
		}else{
			ov.clear();
		}
		int firstSlice = -1;
		
		for(int i = start; i <= end; i++) {
			
			int slice = (int)Analyzer.getResultsTable().getValueAsDouble(0, i);
			int bloblabel= (int)Analyzer.getResultsTable().getValueAsDouble(1, i);
			Blob b = Shape_Filter.getInstance().getBlobByFrameAndLabel(slice-1, bloblabel);

			PolygonRoi pr = new PolygonRoi(b.getOuterContour().xpoints.clone(),b.getOuterContour().ypoints.clone(),b.getOuterContour().npoints,Roi.TRACED_ROI);
			pr.setStrokeWidth(2);
			pr.setPosition(slice);
			ov.add(pr);
			Point[] mer = b.getMinimumBoundingRectangle();
			int[] xpoints = new int[mer.length];
			int[] ypoints = new int[mer.length];
			for(int j = 0; j < mer.length; j++){
				xpoints[j] = mer[j].x;
				ypoints[j] = mer[j].y;
			}
			PolygonRoi pr2 = new PolygonRoi(xpoints, ypoints, mer.length, Roi.POLYGON);
			pr2.setStrokeWidth(1);
			pr2.setStrokeColor(Color.red);
			pr2.setPosition(slice);
			ov.add(pr2);
			if(firstSlice==-1){
				firstSlice = slice;
			}

		}


		IJ.getImage().setSlice(firstSlice);
		IJ.getImage().repaintWindow();
		
		
		// setOverlay(ov);
	}

}
