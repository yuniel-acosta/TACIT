package edu.usc.cssl.tacit.cluster.kmeans.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

import edu.uc.cssl.tacit.cluster.kmeans.services.KmeansClusterAnalysis;
import edu.usc.cssl.tacit.cluster.kmeans.ui.internal.IKmeansClusterViewConstants;
import edu.usc.cssl.tacit.cluster.kmeans.ui.internal.KmeansClusterViewImageRegistry;
import edu.usc.cssl.tacit.common.Preprocess;
import edu.usc.cssl.tacit.common.ui.composite.from.TacitFormComposite;
import edu.usc.cssl.tacit.common.ui.outputdata.OutputLayoutData;
import edu.usc.cssl.tacit.common.ui.outputdata.TableLayoutData;
import edu.usc.cssl.tacit.common.ui.utility.TacitUtil;
import edu.usc.cssl.tacit.common.ui.validation.OutputPathValidation;
import edu.usc.cssl.tacit.common.ui.views.ConsoleView;

public class KmeansClusterView extends ViewPart implements IKmeansClusterViewConstants {
	public static String ID = "edu.usc.cssl.tacit.cluster.kmeansui.view1";
	private ScrolledForm form;
	private FormToolkit toolkit;
	private Button preprocessEnabled;
	private TableLayoutData layData;
	private Text noClusterTxt;
	private OutputLayoutData layoutData;
	protected Job performCluster;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = createFormBodySection(parent);
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.EXPANDED);

		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(section);
		section.setExpanded(true);
		String description = "This section gives details about KMeans clustering";
		FormText descriptionFrm = toolkit.createFormText(section, false);
		descriptionFrm.setText("<form><p>" + description + "</p></form>", true, false);
		section.setDescriptionControl(descriptionFrm);
		ScrolledComposite sc = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(sc);
		TacitFormComposite.addErrorPopup(form.getForm(), toolkit);
		TacitFormComposite.createEmptyRow(toolkit, sc);
		Composite client = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		layData = TacitFormComposite.createTableSection(client, toolkit, layout, "Input Details",
				"Add File(s) and Folder(s) to include in analysis.", true, true, true,true);

		Composite compInput;
		compInput = layData.getSectionClient();
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(compInput);
		preprocessEnabled = TacitFormComposite.createPreprocessLink(compInput, toolkit);
		createAdditionalOptions(compInput);

		Composite client1 = toolkit.createComposite(form.getBody());
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1).applyTo(client1);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(client1);

		layoutData = TacitFormComposite.createOutputSection(toolkit, client1, form.getMessageManager());

		// we dont need stop word's as it will be taken from the preprocessor
		// settings

		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter());
		// form.setMessage("Invalid path", IMessageProvider.ERROR);
		this.setPartName("KMeans Cluster");
		addButtonsToToolBar();
		toolkit.paintBordersFor(form.getBody());

	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Job.class) {
			return performCluster;
		}
		return super.getAdapter(adapter);
	}

	private void createAdditionalOptions(Composite sectionClient) {
		Composite comp = toolkit.createComposite(sectionClient);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).applyTo(comp);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(comp);

		Label noClusterTxtLbl = toolkit.createLabel(comp, "Number of clusters:", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0).applyTo(noClusterTxtLbl);
		noClusterTxt = toolkit.createText(comp, "", SWT.BORDER);
		noClusterTxt.setText("1");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 0).applyTo(noClusterTxt);
	}

	private FormToolkit createFormBodySection(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		toolkit.decorateFormHeading(form.getForm());
		form.setText("KMeans Cluster"); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).applyTo(form.getBody());
		return toolkit;
	}

	private void addButtonsToToolBar() {
		IToolBarManager mgr = form.getToolBarManager();
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (KmeansClusterViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_LRUN_OBJ));
			}

			@Override
			public String getToolTipText() {
				return "Analyze";
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			@Override
			public void run() {
				if (!canProceedCluster()) {
					return;
				}
				TacitFormComposite.writeConsoleHeaderBegining("KMeans Clustering started ");
				final int noOfClusters = Integer.valueOf(noClusterTxt.getText()).intValue();
				final boolean isPreprocess = preprocessEnabled.getSelection();
				final List<String> selectedFiles = TacitUtil.refineInput(layData.getSelectedFiles());
				final String outputPath = layoutData.getOutputLabel().getText();
				performCluster = new Job("Clustering...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						TacitFormComposite.setConsoleViewInFocus();
						TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
						monitor.beginTask("TACIT started clustering...", 100);
						List<File> inputFiles = new ArrayList<File>();
						if (isPreprocess) {
							monitor.subTask("Preprocessing...");
							Preprocess preprocessTask = new Preprocess("KMeans");
							try {
								String dirPath = preprocessTask.doPreprocessing(selectedFiles, "");
								File[] inputFile = new File(dirPath).listFiles();
								for (File iFile : inputFile) {
									inputFiles.add(iFile);
								}

							} catch (IOException e) {
								e.printStackTrace();
								return Status.CANCEL_STATUS;
							} catch (NullPointerException e) {
								e.printStackTrace();
								return Status.CANCEL_STATUS;
							}

							monitor.worked(10);
						} else {
							for (String filepath : selectedFiles) {
								if ((new File(filepath).isDirectory())) {
									continue;
								}
								inputFiles.add(new File(filepath));
							}
							monitor.worked(10);
						}

						// kmeans processsing
						long startTime = System.currentTimeMillis();
						monitor.subTask("Clustering files...");
						Date dateObj = new Date();
						boolean isSuccessful = KmeansClusterAnalysis.runClustering(noOfClusters, inputFiles, outputPath, dateObj);
						if(!isSuccessful) 
							return Status.CANCEL_STATUS;
						
						monitor.worked(80);
						ConsoleView.printlInConsoleln("K-Means Clustering completed successfully in "
								+ (System.currentTimeMillis() - startTime) + " milliseconds.");

						if (monitor.isCanceled()) {
							TacitFormComposite.writeConsoleHeaderBegining("<terminated> k-Means clustering  ");
							throw new OperationCanceledException();
						}
						monitor.worked(10);
						monitor.done();

						TacitFormComposite.updateStatusMessage(getViewSite(), "k-Means clustering completed",
								IStatus.OK, form);
						TacitFormComposite.writeConsoleHeaderBegining("<terminated> k-Means clustering  ");
						return Status.OK_STATUS;
					}
				};
				performCluster.setUser(true);
				if (canProceedCluster()) {
					performCluster.schedule();
					performCluster.addJobChangeListener(new JobChangeAdapter() {

						public void done(IJobChangeEvent event) {
							if (!event.getResult().isOK()) {
								TacitFormComposite.writeConsoleHeaderBegining("Error: <terminated> Kmeans Clustering");
								ConsoleView.printlInConsoleln("Clustering terminated...");
								ConsoleView.printlInConsoleln(
										"Take appropriate action to resolve the issues and run again.");
							}
						}
					});

				} else {
					TacitFormComposite.updateStatusMessage(getViewSite(),
							"CLustering cannot be started. Please check the Form status to correct the errors",
							IStatus.ERROR, form);
				}

			}

		});
		Action helpAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (KmeansClusterViewImageRegistry.getImageIconFactory().getImageDescriptor(IMAGE_HELP_CO));
			}

			@Override
			public String getToolTipText() {
				return "Help";
			}

			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp("edu.usc.cssl.tacit.cluster.kmeans.ui.kmeans");
			};
		};
		mgr.add(helpAction);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(helpAction, "edu.usc.cssl.tacit.cluster.kmeans.ui.kmeans");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form, "edu.usc.cssl.tacit.cluster.kmeans.ui.kmeans");
		form.getToolBarManager().update(true);
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	private boolean canProceedCluster() {
		boolean canProceed = true;
		TacitFormComposite.updateStatusMessage(getViewSite(), null, null, form);
		form.getMessageManager().removeMessage("location");
		form.getMessageManager().removeMessage("input");
		form.getMessageManager().removeMessage("cluster");
		String message = OutputPathValidation.getInstance()
				.validateOutputDirectory(layoutData.getOutputLabel().getText(), "Output");
		if (message != null) {

			message = layoutData.getOutputLabel().getText() + " " + message;
			form.getMessageManager().addMessage("location", message, null, IMessageProvider.ERROR);
			canProceed = false;
		}

		// todo: check whether there is any real file - not just a folder name
		if (layData.getSelectedFiles().size() < 1) {
			form.getMessageManager().addMessage("input", "Select/Add atleast one input file", null,
					IMessageProvider.ERROR);
			canProceed = false;
		}
		int noClusters;
		try {
			noClusters = Integer.parseInt(noClusterTxt.getText());
			if (noClusters <= 0)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			form.getMessageManager().addMessage("cluster",
					"Number of clusters should be a valid integer greater than 0", null, IMessageProvider.ERROR);
			canProceed = false;
		} catch (NullPointerException e) {
			form.getMessageManager().addMessage("cluster",
					"Number of clusters should be a valid integer greater than 0", null, IMessageProvider.ERROR);
			canProceed = false;
		}

		return canProceed;
	}

}
