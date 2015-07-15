package de.biomedical_imaging.ij.shapefilter.ndef;

import java.awt.Window;

import de.biomedical_imaging.ij.shapefilter.ImageResultsTableSelector;
import de.biomedical_imaging.ij.shapefilter.Shape_Filter;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class RegisterImageToShapeFilter_ implements PlugIn{

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
		
		GenericDialog gd = new GenericDialog("Register Image to Shape Filter");
		gd.addChoice("Image: ", openWindows, openWindows[0]);
		gd.showDialog();
		
		int cIndex = gd.getNextChoiceIndex();
		String title = openWindows[cIndex];
		Window window = WindowManager.getWindow(title);
		ImagePlus image = WindowManager.getImage(title);
		boolean windowIsVisible = (window!=null);
		if(windowIsVisible){
			window.getComponent(0).addMouseListener(new ImageResultsTableSelector(image));
		}
		
		
		
	}

}
