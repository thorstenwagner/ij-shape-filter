package de.biomedical_imaging.ij.shapefilter.ndef;

import java.awt.Color;
import java.awt.Event;
import java.awt.MenuItem;
import java.awt.Polygon;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import de.biomedical_imaging.ij.shapefilter.ImageResultsTableSelector;
import de.biomedical_imaging.ij.shapefilter.Shape_Filter;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
/**
 * Dieses Plugin zeigt die Konturen eines binären Bildes als Overlay an.
 * @author Thorsten Wagner
 *
 */
public class ShowAsOverlay_ implements PlugIn{
	ImagePlus binaryImg;
	ImagePlus targetImg;
	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		if(Shape_Filter.getInstance()==null){
			throw new IllegalStateException("Shape Filter is not runnig!");
		}
		
		
		String[] openWindows = new String[WindowManager.getImageCount()];
		
		for(int i = 0; i < WindowManager.getImageCount(); i++){
			openWindows[i] = WindowManager.getImage(WindowManager.getIDList()[i]).getTitle();
		}
		
		GenericDialog gd = new GenericDialog("Show as overlay");
		gd.addChoice("Binary_image: ", openWindows, openWindows[0]);
		gd.addChoice("Target_image: ", openWindows, openWindows[1]);
		gd.showDialog();
		
		int binaryIndex = gd.getNextChoiceIndex();
		binaryImg = WindowManager.getImage(openWindows[binaryIndex]);
		
		int targetIndex = gd.getNextChoiceIndex();
		targetImg = WindowManager.getImage(openWindows[targetIndex]);

		//targetImg.getWindow().getComponent(0).addKeyListener(new RestorOverlayListener(this));
		ImageCanvas ic = (ImageCanvas)targetImg.getWindow().getComponent(0); 
		ic.disablePopupMenu(true);
		ic.addMouseListener(new ImagePopupListener(ic,this));
		showOverlay();
	}
	
	public void showOverlay(){
		Overlay ov = targetImg.getOverlay();
		if(ov == null){
			ov = new Overlay();
			targetImg.setOverlay(ov);
		}
		ov.clear();
		
		ManyBlobs[] allblobs = Shape_Filter.getInstance().getAllBlobs();

		for(int i = 0; i < allblobs.length; i++){

			for (int j = 0; j < allblobs[i].size(); j++) {
				Blob blob = allblobs[i].get(j);
				
				Polygon p = new Polygon(blob.getOuterContour().xpoints.clone(),blob.getOuterContour().ypoints.clone(), blob.getOuterContour().npoints);
				PolygonRoi pr = new PolygonRoi(p, Roi.TRACED_ROI);
				pr.setPosition(i+1);
				pr.setStrokeColor(Color.red);
				pr.setStrokeWidth(2);
				ov.add(pr);
			}
		}
		targetImg.repaintWindow();
	}

}

class ImagePopupListener implements MouseListener {

	ImageCanvas ic;
	ShowAsOverlay_ plugin;
	public ImagePopupListener(ImageCanvas ic,ShowAsOverlay_ plugin) {
		this.ic = ic;
		this.plugin = plugin;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
		if (e.isPopupTrigger()) {
			showPopupMenu(e.getX(), e.getY());
		}
		
	}
	
	public void showPopupMenu(int x, int y){
		PopupMenu popup = new PopupMenu("Particle Sizer Menu");
		
		MenuItem removeParticleItem = new MenuItem("Remove particle");
		removeParticleItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int start = IJ.getTextPanel().getSelectionStart();
				int end = IJ.getTextPanel().getSelectionEnd();
				if(start!=-1){

					if(start == end && ImageResultsTableSelector.isParticleSelected){
						
						int frame = Integer.parseInt(ResultsTable.getResultsTable().getStringValue(0, start));
						int label = Integer.parseInt(ResultsTable.getResultsTable().getStringValue(1, start));

						Blob b = Shape_Filter.getInstance().getBlobByFrameAndLabel(frame-1, label);
						Shape_Filter.getInstance().getAllBlobs()[frame-1].remove(b);

						ResultsTable.getResultsTable().deleteRow(start);
			
						Prefs.set("ndef.NumberOfParticles", ResultsTable.getResultsTable().getCounter());
						IJ.runMacro("updateResults();");
						ImageResultsTableSelector.isParticleSelected = false;
						plugin.showOverlay();
					}
				}
				
			}
		});
		
		removeParticleItem.setEnabled(ImageResultsTableSelector.isParticleSelected);
		
		MenuItem restorSelectionItem = new MenuItem("Restore particle selection");
		
		restorSelectionItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plugin.showOverlay();
				
			}
		});
		
		popup.add(removeParticleItem); 
		popup.add(restorSelectionItem); 
		
		
		ic.add(popup);

		popup.show(ic, x, y);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopupMenu(e.getX(), e.getY());
		}
		
	}
	
}