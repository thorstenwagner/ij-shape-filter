package de.biomedical_imaging.ij.shapefilter;

import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.filter.Analyzer;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
/**
 * This class implements an listener for interactive select a blob in an image and the corresponding row in the results table.
 **/
public class ImageResultsTableSelector implements MouseListener {
	
	private ImagePlus imp;
	public static boolean isParticleSelected;
	public ImageResultsTableSelector(ImagePlus imp) {
		// TODO Auto-generated constructor stub
		this.imp = imp;
		isParticleSelected = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		double mag = ((ImageCanvas)imp.getWindow().getComponent(0)).getMagnification();
		double x = (e.getX()*1.0/mag );
		double y = (e.getY()*1.0/mag);
		
		
		Overlay ov = imp.getOverlay();
		if(ov==null){
			ov = new Overlay();
			imp.setOverlay(ov);
		}else{
			ov.clear();
		}
		isParticleSelected = false;
		for(int i = 0; i < Analyzer.getResultsTable().getCounter(); i++){
			int slice = (int)Analyzer.getResultsTable().getValueAsDouble(0, i);
			if(slice==imp.getSlice()){
		
				int bloblabel= (int)Analyzer.getResultsTable().getValueAsDouble(1, i);
				Blob b = Shape_Filter.getInstance().getBlobByFrameAndLabel(slice-1, bloblabel);
			
				if(b.getOuterContour().contains(x, y)){
				
					IJ.getTextPanel().setSelection(i, i);
					
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
					IJ.getImage().repaintWindow();
					isParticleSelected = true;
				}
				
				
				

			}
			else if(slice >IJ.getImage().getSlice()){
				break;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
