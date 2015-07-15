package de.biomedical_imaging.ij.shapefilter.ndef;

import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseInAgglomerateListener implements MouseListener {
	
	ImagePlus deaggloImage;
	ImagePlus aggloImp;
	ManyBlobs agglomerates = null;
	int lastSlice = -1;
	
	public MouseInAgglomerateListener(ImagePlus deaggloImage, ImagePlus aggloImp) {
		this.deaggloImage = deaggloImage;
		this.aggloImp = aggloImp;
		updateAgglomerates();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		boolean rightButtonIsPressed = (e.getModifiers() & InputEvent.BUTTON3_MASK)==InputEvent.BUTTON3_MASK;
		if(e.isAltDown()&& !rightButtonIsPressed){
			double mag = ((ImageCanvas)deaggloImage.getWindow().getComponent(0)).getMagnification();
			double cX = deaggloImage.getCalibration().getX(1);
			double cY = deaggloImage.getCalibration().getY(1);
			double x = e.getX()*cX*1.0/mag; //deaggloImage.getCalibration().getX() ;
			double y = e.getY()*cY*1.0/mag; //deaggloImage.getCalibration().getY() ;
			
			Overlay ov = deaggloImage.getOverlay();
			if(ov == null){
				ov = new Overlay();
				deaggloImage.setOverlay(ov);
			}
			else{
				ov.clear();
			}
			updateAgglomerates();
			for(int i = 0; i < agglomerates.size(); i++)
			{
				Blob b = agglomerates.get(i);
				
				Polygon ch = b.getConvexHull();
				if(ch.contains(e.getX()*1.0/mag, e.getY()*1.0/mag)){
					PolygonRoi pr = new PolygonRoi(ch.xpoints.clone(),ch.ypoints.clone(),ch.npoints,Roi.TRACED_ROI);
					pr.setStrokeWidth(2);
					pr.setStrokeColor(Color.orange);
					pr.setPosition(deaggloImage.getCurrentSlice());
					ov.add(pr);
					
					if(e.getClickCount()==2){
						ResultsTable selectionRT = new ResultsTable();
						ResultsTable results = ResultsTable.getResultsTable();
						for(int j = 0; j < results.getCounter(); j++)
						{
							int frame = (int)results.getValue("Frame", j);
							int xb = (int)(results.getValue("X", j)/cX);
							int yb = (int)(results.getValue("Y", j)/cY);
	
							if((frame==deaggloImage.getCurrentSlice()) && ch.contains(xb, yb))
							{
								selectionRT.incrementCounter();
								for(int k = 0; k <= results.getLastColumn(); k++){
									double value = results.getValueAsDouble(k, j);
									String heading = results.getColumnHeading(k);
									
									selectionRT.addValue(heading, value);
								}
							}
							selectionRT.show("Agglomerat");
						}
					}
					break;
				}
			}
		}
		
	}
	
	private void updateAgglomerates(){
		
		if(agglomerates==null || (lastSlice !=deaggloImage.getCurrentSlice())){
			ImagePlus help = new ImagePlus("", aggloImp.getImageStack().getProcessor(deaggloImage.getCurrentSlice()));
			agglomerates = new ManyBlobs(help);
			if(AgglomerateManager_.instance.aggloHasBlackbackground){
				agglomerates.setBackground(0);
			}else{
				agglomerates.setBackground(1);
			}
			agglomerates.findConnectedComponents();
			lastSlice=deaggloImage.getCurrentSlice();
		}
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
		// TODO Auto-generated method stub
		
	}

}
