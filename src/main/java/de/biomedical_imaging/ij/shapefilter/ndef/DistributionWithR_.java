package de.biomedical_imaging.ij.shapefilter.ndef;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import de.biomedical_imaging.ij.shapefilter.Shape_Filter;
import ij.IJ;
import ij.blob.Blob;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class DistributionWithR_ implements PlugIn{

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		Menu Results = ResultsTable.getResultsWindow().getMenuBar().getMenu(3);
		
		Results.getItem(2).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				IJ.run("R Histogram");
				
			}
		});
		Results.remove(1); // Remove Summarize
		Results.remove(2); // Remove Set Measurements Entry
	//	IJ.log();
		
		MenuItem removeParticle = new MenuItem("Remove Particle");
		removeParticle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int start = IJ.getTextPanel().getSelectionStart();
				int end = IJ.getTextPanel().getSelectionEnd();
				if(start!=-1){
					IJ.log("start " + start + " end " + end);
					if(start == end){
						
						int frame = Integer.parseInt(ResultsTable.getResultsTable().getStringValue(0, start));
						int label = Integer.parseInt(ResultsTable.getResultsTable().getStringValue(1, start));
						IJ.log("Frame " + frame + " Label " + label);
						Blob b = Shape_Filter.getInstance().getBlobByFrameAndLabel(frame-1, label);
						Shape_Filter.getInstance().getAllBlobs()[frame-1].remove(b);
						IJ.log("Counter "  + ResultsTable.getResultsTable().getCounter());
						ResultsTable.getResultsTable().deleteRow(start);
						IJ.log("Counter "  + ResultsTable.getResultsTable().getCounter());
						IJ.runMacro("updateResults();");
					}
				}
				
				
				
			}
		});
		
		Results.add(removeParticle);
		
	}

}
