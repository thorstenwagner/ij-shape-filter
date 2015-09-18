package de.biomedical_imaging.ij.shapefilter;

import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.gui.GenericDialog;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.plugin.filter.Analyzer;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Diese Klasse ermöglicht es, einzelnen Blobs Klassen zuzuordnen. 
 * @author Thorsten Wagner (wagner@biomedical-imaging.de)
 *
 */
public class AddClassToResultsTableListener implements KeyListener {

	private ImagePlus imp;
	public AddClassToResultsTableListener(ImagePlus imp) {
		// TODO Auto-generated constructor stub
		this.imp = imp;
	}
	
	public ImagePlus getImage(){
		return imp;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
		
		
	}
	private int getRowToBlob(Blob b){
		for( int i = 0; i < Analyzer.getResultsTable().getCounter(); i++){
			if(((int)Analyzer.getResultsTable().getValueAsDouble(1, i))==b.getLabel()){
				return i;
			}
		}
		return 0;
		
	}
	private Blob getBlob(int x, int y){
		for(int j = 0; j < Analyzer.getResultsTable().getCounter(); j++){
			int slice = (int)Analyzer.getResultsTable().getValueAsDouble(0, j);
			if(slice==IJ.getImage().getSlice()){
				int bloblabel= (int)Analyzer.getResultsTable().getValueAsDouble(1, j);
				Blob b = Shape_Filter.getInstance().getBlobByFrameAndLabel(slice-1, bloblabel);
				if(b.getOuterContour().contains(x, y)){
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		IJ.log("EVENT");
		Roi r = imp.getRoi();
		if(r!= null && r.getType()==Roi.POINT && arg0.getKeyChar()=='c'){

			PointRoi pr = (PointRoi)r;
			GenericDialog gd = new GenericDialog("Input Classname");
			gd.addStringField("Classname", "A");
			gd.showDialog();
			
			String classname = gd.getNextString();//arg0.getKeyChar();
			int[] x = pr.getPolygon().xpoints;
			int[] y = pr.getPolygon().ypoints;
			
			for(int i = 0; i < pr.getPolygon().npoints; i++){
				IJ.log("x " + x[i] + " y " + y[i]);
				Blob b = getBlob(x[i],y[i]);
				if(b != null){
						Analyzer.getResultsTable().setValue("Class", getRowToBlob(b), classname);

						Analyzer.getResultsTable().show("Results");
				}
			}
		
		}
		else if(r!= null && r.getType()==Roi.RECTANGLE && arg0.getKeyChar()=='c'){
			
			GenericDialog gd = new GenericDialog("Input Classname");
			gd.addStringField("Classname", "A");
			gd.showDialog();
			String classname = gd.getNextString();//arg0.getKeyChar();
			for(int j = 0; j < Analyzer.getResultsTable().getCounter(); j++){
				int slice = (int)Analyzer.getResultsTable().getValueAsDouble(0, j);
				if(slice==IJ.getImage().getSlice()){
					int bloblabel= (int)Analyzer.getResultsTable().getValueAsDouble(1, j);
					Blob b = Shape_Filter.getInstance().getBlobByFrameAndLabel(slice-1, bloblabel);
					if(r.getBoundingRect().contains(b.getCenterOfGravity())){
						Analyzer.getResultsTable().setValue("Class", getRowToBlob(b), classname);

						Analyzer.getResultsTable().show("Results");
					}
				}
			}
			
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
