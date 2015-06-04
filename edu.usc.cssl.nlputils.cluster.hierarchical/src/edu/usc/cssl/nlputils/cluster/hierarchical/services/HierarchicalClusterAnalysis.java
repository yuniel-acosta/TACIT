package edu.usc.cssl.nlputils.cluster.hierarchical.services;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;

public class HierarchicalClusterAnalysis {
	public static String doClustering(List<File> inputFiles, String outputPath,
			boolean saveImg, SubProgressMonitor subProgressMonitor) {
		try {

			StringToWordVector filter = new StringToWordVector();
			HierarchicalClusterer aggHierarchical = new HierarchicalClusterer();

			FastVector atts = new FastVector(1);
			atts.addElement(new Attribute("text", (FastVector) null));

			Instances docs = new Instances("text_files", atts, 0);

			System.out.println(outputPath);

			for (int i = 0; i < inputFiles.size(); i++) {

				try {
					double[] newInst = new double[1];
					String content = new Scanner(inputFiles.get(i))
							.useDelimiter("\\Z").next();
					newInst[0] = (double) docs.attribute(0).addStringValue(
							content);
					docs.add(new Instance(1.0, newInst));
				} catch (Exception e) {
					System.out.println("Exception occurred in reading files"
							+ e);
				}
			}

			filter.setInputFormat(docs);
			Instances filteredData = Filter.useFilter(docs, filter);

			aggHierarchical.setNumClusters(1);
			aggHierarchical.setPrintNewick(true);
			subProgressMonitor.subTask("Building cluster");
			aggHierarchical.buildClusterer(filteredData);
			subProgressMonitor.worked(20);
			String g = aggHierarchical.graph();
			String output = formatGraph(g, inputFiles);
			System.out.println("Network " + output);
			subProgressMonitor.subTask("Foramting Image");
			aggHierarchical.linkTypeTipText();
			subProgressMonitor.worked(15);
			HierarchyVisualizer tv = new HierarchyVisualizer(output);

			tv.setSize(1024, 1024);
			JFrame f;
			f = new JFrame();
			JPanel container = new JPanel();
			JScrollPane scrPane = new JScrollPane(container);
			Container contentPane = f.getContentPane();
			contentPane.setLayout(new BorderLayout());
			f.getContentPane().add(scrPane);
			contentPane.add(tv, BorderLayout.CENTER);
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.setSize(1024, 1024);
			f.setVisible(true);
			tv.fitToScreen();

			if (saveImg) {
				try {
					BufferedImage image = new BufferedImage(
							contentPane.getWidth(), contentPane.getHeight(),
							BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics2D = image.createGraphics();
					contentPane.printAll(graphics2D);
					graphics2D.dispose();
					subProgressMonitor.subTask("Saving image @ " + outputPath
							+ File.separator
							+ "Hierarchical Clustering Output.jpeg");
					ImageIO.write(image, "jpeg", new File(outputPath
							+ File.separator
							+ "Hierarchical Clustering Output.jpeg"));
					subProgressMonitor.worked(10);
				} catch (Exception e) {
					System.out
							.println("Exception occurred in saving image of output "
									+ e);
				}
			}

			BufferedWriter buf = new BufferedWriter(new FileWriter(new File(
					outputPath + File.separator + "cluster.txt")));
			buf.write("Mapping of document ID to actual names\n");
			for (int i = 0; i < inputFiles.size(); i++) {
				buf.write((i + 1) + " " + inputFiles.get(i).getName() + "\n");
			}
			buf.write(output);
			buf.close();
			subProgressMonitor.done();
			return output;
		} catch (Exception e) {
			System.out
					.println("Exception occurred in Hierarchical Clustering  "
							+ e);
		}
		return null;
	}

	public static String formatGraph(String graph, List<File> files) {
		StringBuffer fgraph = new StringBuffer();

		String input = graph.substring(7);
		int i = 0, len = input.length();
		char c;
		int count = 0;

		fgraph.append(graph.substring(0, 7));
		System.out.println(graph);
		while (i < len) {
			c = input.charAt(i);
			if (c == '(') {
				fgraph.append(input.charAt(i++));
			} else if (c == ':') {
				if (input.charAt(i - 1) != ')') {
					fgraph.append(++count);

				}
				while (i < len
						&& (input.charAt(i) != ',' && input.charAt(i) != '(')) {
					fgraph.append(input.charAt(i++));
					// System.out.println(fgraph.toString());
				}
				if (i < len)
					fgraph.append(input.charAt(i++));
			} else {
				i++;
			}
		}
		System.out.println(fgraph.toString());
		return fgraph.toString();
	}

	public static String runClustering(List<File> listOfFiles,
			String fOutputDir, boolean fSaveImg,
			SubProgressMonitor subProgressMonitor) {

		List<File> inputFiles = new ArrayList<File>();
		for (File f : listOfFiles) {
			if (f.getAbsolutePath().contains("DS_Store"))
				continue;
			if (!f.isDirectory() && f.exists())
				inputFiles.add(f);
		}
		subProgressMonitor.beginTask("Running CLustering", 50);
		subProgressMonitor.subTask("Running Hierarchical Clustering...");
		System.out.println("Running Hierarchical Clustering...");
		String clusters = doClustering(inputFiles, fOutputDir, fSaveImg,
				new SubProgressMonitor(subProgressMonitor, 45));
		if (subProgressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (clusters == null) {
			return null;
		}
		System.out.println("Output for Hierarchical Clustering");
		System.out.println("Mapping of document ID to actual names");
		subProgressMonitor.subTask("Mapping of document ID to actual names");
		for (int i = 0; i < inputFiles.size(); i++) {
			System.out.println((i + 1) + " " + inputFiles.get(i).getName());
		}
		subProgressMonitor.worked(5);
		System.out.println("Clusters formed: \n");

		System.out.println(clusters);
		System.out.println("Saving the output to cluster.txt");
		subProgressMonitor.subTask("Saving the output to cluster.txt");
		System.out.println("\nDone Hierarchical Clustering...");
		subProgressMonitor.worked(5);
		subProgressMonitor.done();
		return clusters;
	}
}
