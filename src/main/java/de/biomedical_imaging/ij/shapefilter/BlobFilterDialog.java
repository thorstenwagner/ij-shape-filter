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
    MER
TABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.biomedical_imaging.ij.shapefilter;
import java.awt.AWTEvent;
import java.util.StringTokenizer;

import ij.IJ;
import ij.blob.Blob;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilterRunner;


class BlobFilterDialog implements DialogListener {
	
	FilterParameters params;
	
	String DEFAULT_ZERO_INFINITY = "0-Infinity";
	String DEFAULT_ZERO_ONE = "0-1";
	
	/**
	 * shows the gui
	 * @param pfr
	 * @return -1 if canceled, 1 if not
	 */
	int showDialog(PlugInFilterRunner pfr) {
		
		GenericDialog gd = new GenericDialog("Shape Filter");
		
		//Features
		gd.addStringField("Area", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Area_Convex_Hull", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Perimeter", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Perimeter_Convex_Hull", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Feret_Diameter", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Min._Feret_Diameter", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Max_Inscr_Circle_Diameter", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Long_Side_Min._Bounding_Rect.", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Short_Side_Min._Bounding_Rect.", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Aspect_Ratio", "1-Infinity");
		gd.addStringField("Area_to_Perimeter_ratio", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Circularity", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Elongation", DEFAULT_ZERO_ONE);
		gd.addStringField("Convexity", DEFAULT_ZERO_ONE);
		gd.addStringField("Solidity", DEFAULT_ZERO_ONE);
		gd.addStringField("Num._of_Holes", DEFAULT_ZERO_INFINITY);
		gd.addStringField("Thinnes_ratio", DEFAULT_ZERO_ONE);
		gd.addStringField("Contour_Temperatur", DEFAULT_ZERO_ONE);
		gd.addStringField("Orientation", "0-180");
		gd.addStringField("Fractal_Box_Dimension", "0-2");
		gd.addStringField("Option->Box-Sizes:","2,3,4,6,8,12,16,32,64");

		gd.addHelp("http://code.google.com/p/ijblob/wiki/BasicFeatures");
		
		//Options
		String[] labels = new String[8];
		boolean[] states = new boolean[8];
		labels[0]="Add_to_Manager"; states[0]=true;
		labels[1]="Draw_Holes"; states[1]=true;
		labels[2]="Draw_Convex_Hull"; states[2]=false;
		labels[3]="Draw_Label"; states[3]=false;
		labels[4]="Black_Background"; states[4]=false;
		labels[5]="Show_Labeled_Image"; states[5]=false;
		labels[6]="Fill_Results_Table"; states[6]=true;
		labels[7]="Exclude_on_edges"; states[7]=true;
		gd.addCheckboxGroup(4, 2, labels, states);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		
		if (gd.wasCanceled()) {
			return -1;
		}
		
		updateParams(gd);
		Shape_Filter.getInstance().setParameters(params);
		
		return 1;
	}
	
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		updateParams(gd);
		Shape_Filter.getInstance().setParameters(params);
		Shape_Filter.getInstance().setIsPreview(gd.isPreviewActive());
		return true;
	}
	
	private void updateParams(GenericDialog gd){
		params = new FilterParameters();
		params.addFilter(Blob.GETENCLOSEDAREA, stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETAREACONVEXHULL , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETPERIMETER , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETPERIMETERCONVEXHULL , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETFERETDIAMETER , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETMINFERETDIAMETER , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETDIAMETERMAXIMUMINSCRIBEDCIRCLE, stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETLONGSIDEMBR , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETSHORTSIDEMBR , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETASPECTRATIO , stringIntervalToArray(gd.getNextString(),"1-Infinity"));
		params.addFilter(Blob.GETAREATOPERIMETERRATIO, stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETCIRCULARITY , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETELONGATION , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_ONE));
		params.addFilter(Blob.GETCONVEXITY, stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_ONE));
		params.addFilter(Blob.GETSOLIDITY, stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_ONE));
		params.addFilter(Blob.GETNUMBEROFHOLES , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_INFINITY));
		params.addFilter(Blob.GETTHINNESRATIO , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_ONE));
		params.addFilter(Blob.GETCONTOURTEMPERATURE , stringIntervalToArray(gd.getNextString(),DEFAULT_ZERO_ONE));
		params.addFilter(Blob.GETORIENTATIONMAJORAXIS, stringIntervalToArray(gd.getNextString(),"0-180"));
		params.addFilter(Blob.GETFRACTALBOXDIMENSION , stringIntervalToArray(gd.getNextString(),"0-2"), s2ints(gd.getNextString()));

		params.setAddToManager(gd.getNextBoolean());
		params.setDrawHoles(gd.getNextBoolean());
		params.setDrawConvexHull(gd.getNextBoolean());
		params.setDrawLabel(gd.getNextBoolean());
		params.setBlackBackground(gd.getNextBoolean());
		params.setShowLabeledImage(gd.getNextBoolean());
		params.setFillResultsTable(gd.getNextBoolean());
		params.setExcludeOnEdges(gd.getNextBoolean());
	}
	
	/**
	 * converts a tab oder ',' delimited string of numbers to a interger array
	 * @param s String of numbers delimited by ',' oder tab
	 */
	public int[] s2ints(String s) {
		StringTokenizer st = new StringTokenizer(s, ", \t");
		int nInts = st.countTokens();
		int[] ints = new int[nInts];
		for(int i=0; i<nInts; i++) {
			try {ints[i] = Integer.parseInt(st.nextToken());}
			catch (NumberFormatException e) {IJ.log(""+e); return null;}
		}
		return ints;
	}
	
	public FilterParameters getParams() {
		return params;
	}
	
	/**
	 * Splits an interval x-y (only - delimiter is supported)
	 * @param s Interval as string
	 * @param defaultvalue Return value if the splitting fails.
	 * @return [0] lower bound, [1] upper bound
	 */
	private double[] stringIntervalToArray(String s, String defaultvalue){
		double[] bounds = new double[2];
		String lim[] = s.split("-");
		if(lim.length < 2){
			lim = defaultvalue.split("-");
		}
		try{
		bounds[0] = Double.parseDouble(lim[0]);
		bounds[1] = Double.parseDouble(lim[1]);}
		catch(Exception e){
			lim = defaultvalue.split("-");
			bounds[0] = Double.parseDouble(lim[0]);
			bounds[1] = Double.parseDouble(lim[1]);
		}
		
		return bounds;
	}

	

}
