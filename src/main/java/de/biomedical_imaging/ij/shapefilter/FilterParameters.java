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
import ij.blob.Blob;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


class FilterParameters {
	private HashMap<String, double[]> filterMethodMap;
	private HashMap<String, Object[]> filterMethodParameterMap;
	private int[] fractalBoxSizes;
	private boolean addToManager;
	private boolean fillResultsTable;
	private boolean drawHoles;
	private boolean drawConvexHull;
	private boolean drawLabel;
	private boolean blackBackground;
	private boolean showLabeledImage;
	
	/**
	 * Sets the fill results table property
	 * @param True if the results table should be filled
	 */
	public void setFillResultsTable(boolean v){
		fillResultsTable = v;
	}
	
	/**
	 * @return True if the results table should be filled.
	 */
	public boolean isFillResultsTable(){
		return fillResultsTable;
	}
	
	/**
	 * @return True if the background is black.
	 */
	public boolean isBlackBackground() {
		return blackBackground;
	}

	/**
	 * Sets the black background property .
	 * @param blackBackground True if the background is black
	 */
	public void setBlackBackground(boolean blackBackground) {
		this.blackBackground = blackBackground;
	}

	/**
	 * 
	 * @return True if the labeled image should be shown.
	 */
	public boolean isShowLabeledImage() {
		return showLabeledImage;
	}

	/**
	 * Sets the show labeled image property.
	 * @param showLabeledImage
	 */
	public void setShowLabeledImage(boolean showLabeledImage) {
		this.showLabeledImage = showLabeledImage;
	}

	/**
	 * 
	 * @return An ascending array of the fractal box sizes 
	 */
	public int[] getFractalBoxSizes() {
		return fractalBoxSizes;
	}

	/**
	 * Sets the fractal box sizes
	 * @param fractalBoxSizes
	 */
	public void setFractalBoxSizes(int[] fractalBoxSizes) {
		this.fractalBoxSizes = fractalBoxSizes;
	}

	/**
	 * @return True if the outer contour should be added to the ROI Manager
	 */
	public boolean isAddToManager() {
		return addToManager;
	}
	
	public void setAddToManager(boolean addToManager) {
		this.addToManager = addToManager;
	}
	
	/**
	 * 
	 * @return True if the holes of a blob should be outlined
	 */
	public int isDrawHoles(){
		if(drawHoles){
		return Blob.DRAW_HOLES;
		}
		return 0;
	}
	
	public void setDrawHoles(boolean drawHoles) {
		this.drawHoles = drawHoles;
	}
	
	/**
	 * 
	 * @return True if the convex hull of a blob should be outlined
	 */
	public int isDrawConvexHull() {
		if(drawConvexHull){
			return Blob.DRAW_CONVEX_HULL;
			}
			return 0;
	}
	
	public void setDrawConvexHull(boolean drawConvexHull) {
		this.drawConvexHull = drawConvexHull;
	}
	
	/**
	 * @return
	 */
	public int isDrawLabel() {
		if(drawLabel){
			return Blob.DRAW_LABEL;
			}
			return 0;
	}
	
	public void setDrawLabel(boolean drawLabel) {
		this.drawLabel = drawLabel;
	}

	public void addFilter(String featureMethod, double[] minxmax, Object... methodparams) {
		filterMethodMap.put(featureMethod, minxmax);
		filterMethodParameterMap.put(featureMethod, methodparams);
	}
	
	public double[] getFilterMethod(String blobfeature) {
		return filterMethodMap.get(blobfeature);
	}
	
	public Object[] getFilterMethodParameter(String blobfeature) {
		return filterMethodParameterMap.get(blobfeature);
	}
	
	public Iterator<Entry<String, double[]>> getFeatureIterator() {
		return filterMethodMap.entrySet().iterator();
	}
	
	public FilterParameters() {
		filterMethodMap = new HashMap<String, double[]>();
		filterMethodParameterMap = new HashMap<String, Object[]>();
	}

}
